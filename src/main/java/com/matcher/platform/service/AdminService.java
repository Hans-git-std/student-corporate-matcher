package com.matcher.platform.service;

import com.matcher.platform.dto.request.*;
import com.matcher.platform.dto.response.*;
import com.matcher.platform.entity.*;
import com.matcher.platform.entity.enums.ApprovalStatus;
import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.exception.BadRequestException;
import com.matcher.platform.exception.ResourceNotFoundException;
import com.matcher.platform.repository.*;
import com.matcher.platform.security.MailQuotaAndRateLimiter;
import com.matcher.platform.security.XssSanitizer;
import com.matcher.platform.util.StringNormalizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminService {

    private final CompanyProfileRepository companyProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentAcademicRecordRepository academicRecordRepository;
    private final UserRepository userRepository;
    private final HiringCriteriaRepository hiringCriteriaRepository;
    private final SkillRepository skillRepository;
    private final TeacherService teacherService;
    private final StudentService studentService;
    private final CompanyService companyService;
    private final MailQuotaAndRateLimiter mailQuotaAndRateLimiter;

    @Value("${app.security.admin.email:admin@studentmatcher.com}")
    private String masterAdminEmail;

    @Value("${app.security.admin.recovery-email:hans31144@gmail.com}")
    private String adminRecoveryEmail;

    public AdminService(
            CompanyProfileRepository companyProfileRepository,
            TeacherProfileRepository teacherProfileRepository,
            TeacherSubjectRepository teacherSubjectRepository,
            StudentProfileRepository studentProfileRepository,
            StudentAcademicRecordRepository academicRecordRepository,
            UserRepository userRepository,
            HiringCriteriaRepository hiringCriteriaRepository,
            SkillRepository skillRepository,
            TeacherService teacherService,
            StudentService studentService,
            CompanyService companyService,
            MailQuotaAndRateLimiter mailQuotaAndRateLimiter
    ) {
        this.companyProfileRepository = companyProfileRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.academicRecordRepository = academicRecordRepository;
        this.userRepository = userRepository;
        this.hiringCriteriaRepository = hiringCriteriaRepository;
        this.skillRepository = skillRepository;
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.companyService = companyService;
        this.mailQuotaAndRateLimiter = mailQuotaAndRateLimiter;
    }

    // ==========================================
    // Teacher Lifecycle & Management
    // ==========================================

    @Transactional(readOnly = true)
    public List<TeacherProfileResponse> getPendingTeachers() {
        return teacherProfileRepository.findByApprovalStatus(ApprovalStatus.PENDING)
                .stream()
                .map(teacherService::mapToProfileResponse)
                .toList();
    }

    public TeacherProfileResponse approveTeacher(Long id) {
        TeacherProfile teacher = teacherProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherProfile", "id", id));

        teacher.setApprovalStatus(ApprovalStatus.APPROVED);
        teacher.setVerifiedByAdminAt(Instant.now());
        teacher.setRejectionReason(null);

        TeacherProfile saved = teacherProfileRepository.save(teacher);
        return teacherService.mapToProfileResponse(saved);
    }

    public TeacherProfileResponse rejectTeacher(Long id, TeacherRejectRequest request) {
        TeacherProfile teacher = teacherProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherProfile", "id", id));

        teacher.setApprovalStatus(ApprovalStatus.REJECTED);
        teacher.setRejectionReason(request.getReason() != null ? request.getReason().trim() : "Application rejected by administrator.");

        TeacherProfile saved = teacherProfileRepository.save(teacher);
        return teacherService.mapToProfileResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TeacherProfileResponse> getAllTeachers() {
        return teacherProfileRepository.findAll()
                .stream()
                .map(teacherService::mapToProfileResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeacherProfileResponse getTeacherById(Long id) {
        TeacherProfile teacher = teacherProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherProfile", "id", id));
        return teacherService.mapToProfileResponse(teacher);
    }

    public TeacherProfileResponse createTeacher(CreateTeacherRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }
        if (teacherProfileRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException("Employee ID '" + request.getEmployeeId() + "' already exists");
        }

        User user = userRepository.save(new User(request.getEmail(), RoleType.ROLE_TEACHER));

        TeacherProfile teacher = TeacherProfile.builder()
                .user(user)
                .fullName(request.getFullName().trim())
                .employeeId(request.getEmployeeId().trim())
                .department(request.getDepartment().trim())
                .phoneNumber(request.getPhoneNumber())
                .approvalStatus(ApprovalStatus.APPROVED)
                .verifiedByAdminAt(Instant.now())
                .build();

        TeacherProfile savedTeacher = teacherProfileRepository.save(teacher);

        if (request.getAssignedSubjects() != null) {
            List<TeacherSubject> subjects = new ArrayList<>();
            for (String sub : request.getAssignedSubjects()) {
                if (sub != null && !sub.trim().isEmpty()) {
                    String normSub = StringNormalizer.normalize(sub);
                    subjects.add(new TeacherSubject(savedTeacher, normSub));
                }
            }
            teacherSubjectRepository.saveAll(subjects);
            savedTeacher.setAssignedSubjects(subjects);
        }

        return teacherService.mapToProfileResponse(savedTeacher);
    }

    public TeacherProfileResponse updateTeacher(Long id, TeacherProfileRequest request) {
        TeacherProfile profile = teacherProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherProfile", "id", id));

        String empId = request.getEmployeeId().trim();
        if (!profile.getEmployeeId().equalsIgnoreCase(empId) &&
                teacherProfileRepository.existsByEmployeeId(empId)) {
            throw new BadRequestException("Employee ID '" + empId + "' is already registered");
        }

        profile.setFullName(XssSanitizer.sanitize(request.getFullName().trim()));
        profile.setEmployeeId(XssSanitizer.sanitize(empId));
        profile.setDepartment(XssSanitizer.sanitize(request.getDepartment().trim()));
        profile.setPhoneNumber(XssSanitizer.sanitize(request.getPhoneNumber()));

        if (request.getAssignedSubjects() != null) {
            profile.getAssignedSubjects().clear();
            for (String sub : request.getAssignedSubjects()) {
                if (sub != null && !sub.trim().isEmpty()) {
                    String normSub = StringNormalizer.normalize(sub);
                    profile.getAssignedSubjects().add(new TeacherSubject(profile, normSub));
                }
            }
        }

        TeacherProfile saved = teacherProfileRepository.save(profile);
        return teacherService.mapToProfileResponse(saved);
    }

    public void deleteTeacher(Long id) {
        TeacherProfile teacher = teacherProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherProfile", "id", id));
        User user = teacher.getUser();
        teacherProfileRepository.delete(teacher);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    // ==========================================
    // Student Management
    // ==========================================

    @Transactional(readOnly = true)
    public List<StudentProfileResponse> getAllStudents() {
        return studentProfileRepository.findAllWithDetails()
                .stream()
                .map(studentService::mapToProfileResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getStudentById(Long id) {
        StudentProfile student = studentProfileRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "id", id));
        return studentService.mapToProfileResponse(student);
    }

    public StudentProfileResponse updateStudent(Long id, StudentProfileRequest request) {
        StudentProfile profile = studentProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "id", id));

        String normalizedRoll = request.getRollNumber().trim().toUpperCase();
        String rawPhone = request.getPhoneNumber();
        String normalizedPhone = (rawPhone != null && !rawPhone.trim().isEmpty()) ? rawPhone.trim() : null;

        if ((profile.getRollNumber() == null || !profile.getRollNumber().equalsIgnoreCase(normalizedRoll)) &&
                studentProfileRepository.existsByRollNumber(normalizedRoll)) {
            throw new BadRequestException("Roll number '" + normalizedRoll + "' is already in use by another student");
        }

        profile.setFullName(XssSanitizer.sanitize(request.getFullName().trim()));
        profile.setRollNumber(XssSanitizer.sanitize(normalizedRoll));
        profile.setPhoneNumber(normalizedPhone != null ? XssSanitizer.sanitize(normalizedPhone) : null);
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(XssSanitizer.sanitize(request.getGender()));
        profile.setAddress(XssSanitizer.sanitize(request.getAddress()));
        profile.setBio(XssSanitizer.sanitize(request.getBio()));
        profile.setGithubUrl(XssSanitizer.sanitize(request.getGithubUrl()));
        profile.setLinkedinUrl(XssSanitizer.sanitize(request.getLinkedinUrl()));

        StudentProfile saved = studentProfileRepository.save(profile);
        return studentService.mapToProfileResponse(saved);
    }

    public void deleteStudent(Long id) {
        StudentProfile student = studentProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "id", id));
        User user = student.getUser();
        studentProfileRepository.delete(student);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    // ==========================================
    // Company Management
    // ==========================================

    @Transactional(readOnly = true)
    public List<CompanyProfileResponse> getAllCompanies() {
        List<CompanyProfile> list = companyProfileRepository.findAllWithCriteria();
        return list.stream().map(this::mapToCompanyResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CompanyProfileResponse> getPendingCompanies() {
        return companyProfileRepository.findByVerificationStatus(CompanyVerificationStatus.NOT_VERIFIED)
                .stream()
                .map(this::mapToCompanyResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyProfileResponse getCompanyById(Long id) {
        CompanyProfile company = companyProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyProfile", "id", id));
        return mapToCompanyResponse(company);
    }

    public CompanyProfileResponse createCompany(CompanyRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }
        if (companyProfileRepository.existsByCompanyNameIgnoreCase(request.getCompanyName())) {
            throw new BadRequestException("Company name '" + request.getCompanyName() + "' already exists");
        }

        User user = userRepository.save(new User(request.getEmail(), RoleType.ROLE_COMPANY));

        CompanyProfile profile = CompanyProfile.builder()
                .user(user)
                .companyName(request.getCompanyName().trim())
                .industry(request.getIndustry())
                .websiteUrl(request.getWebsiteUrl())
                .location(request.getLocation())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl() != null ? request.getLogoUrl().trim() : null)
                .verificationStatus(CompanyVerificationStatus.VERIFIED)
                .adminRemarks("Created directly and pre-verified by administrator")
                .build();

        CompanyProfile saved = companyProfileRepository.save(profile);
        return mapToCompanyResponse(saved);
    }

    public CompanyProfileResponse updateCompany(Long id, CompanyProfileRequest request) {
        CompanyProfile company = companyProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));

        if (!company.getCompanyName().equalsIgnoreCase(request.getCompanyName().trim()) &&
                companyProfileRepository.existsByCompanyNameIgnoreCase(request.getCompanyName().trim())) {
            throw new BadRequestException("Company name '" + request.getCompanyName() + "' is already in use");
        }

        company.setCompanyName(request.getCompanyName().trim());
        company.setIndustry(request.getIndustry());
        company.setWebsiteUrl(request.getWebsiteUrl());
        company.setLocation(request.getLocation());
        company.setDescription(request.getDescription());
        company.setLogoUrl(request.getLogoUrl());

        CompanyProfile saved = companyProfileRepository.save(company);
        return mapToCompanyResponse(saved);
    }

    public void deleteCompany(Long id) {
        CompanyProfile company = companyProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        User user = company.getUser();
        companyProfileRepository.delete(company);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    public CompanyProfileResponse updateVerificationStatus(Long id, CompanyStatusUpdateRequest request) {
        CompanyProfile company = companyProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));

        company.setVerificationStatus(request.getStatus());
        company.setAdminRemarks(request.getAdminRemarks());

        CompanyProfile saved = companyProfileRepository.save(company);
        return mapToCompanyResponse(saved);
    }

    public HiringCriteriaResponse addCompanyCriteria(Long companyId, HiringCriteriaRequest request) {
        CompanyProfile company = companyProfileRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));

        HiringCriteria criteria = HiringCriteria.builder()
                .company(company)
                .roleTitle(XssSanitizer.sanitize(request.getRoleTitle()))
                .jobDescription(XssSanitizer.sanitize(request.getJobDescription()))
                .minOverallPercentage(request.getMinOverallPercentage())
                .isActive(true)
                .build();

        HiringCriteria savedCriteria = hiringCriteriaRepository.save(criteria);

        if (request.getRequiredSkills() != null) {
            for (RequiredSkillEntry skillEntry : request.getRequiredSkills()) {
                String skillName = StringNormalizer.normalize(skillEntry.getSkillName());
                Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                        .orElseGet(() -> skillRepository.save(new Skill(skillName)));

                savedCriteria.getRequiredSkills().add(new CriteriaRequiredSkill(
                        savedCriteria,
                        skill,
                        skillEntry.getMinProficiency(),
                        skillEntry.getIsMandatory(),
                        skillEntry.getWeightage()
                ));
            }
        }

        if (request.getSubjectCutoffs() != null) {
            for (SubjectCutoffEntry cutoffEntry : request.getSubjectCutoffs()) {
                String subjectName = StringNormalizer.normalize(cutoffEntry.getSubjectName());
                savedCriteria.getSubjectCutoffs().add(new CriteriaSubjectCutoff(
                        savedCriteria,
                        subjectName,
                        cutoffEntry.getMinMarksCutoff(),
                        cutoffEntry.getIsMandatory()
                ));
            }
        }

        HiringCriteria finalSaved = hiringCriteriaRepository.save(savedCriteria);
        return companyService.mapToCriteriaResponse(finalSaved);
    }

    public HiringCriteriaResponse updateCompanyCriteria(Long companyId, Long criteriaId, HiringCriteriaRequest request) {
        CompanyProfile company = companyProfileRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));

        HiringCriteria criteria = hiringCriteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResourceNotFoundException("HiringCriteria", "id", criteriaId));

        if (!criteria.getCompany().getId().equals(company.getId())) {
            throw new BadRequestException("Hiring criteria ID " + criteriaId + " does not belong to company ID " + companyId);
        }

        criteria.setRoleTitle(XssSanitizer.sanitize(request.getRoleTitle()));
        criteria.setJobDescription(XssSanitizer.sanitize(request.getJobDescription()));
        criteria.setMinOverallPercentage(request.getMinOverallPercentage());

        criteria.getRequiredSkills().clear();
        if (request.getRequiredSkills() != null) {
            for (RequiredSkillEntry skillEntry : request.getRequiredSkills()) {
                String skillName = StringNormalizer.normalize(skillEntry.getSkillName());
                Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                        .orElseGet(() -> skillRepository.save(new Skill(skillName)));

                criteria.getRequiredSkills().add(new CriteriaRequiredSkill(
                        criteria,
                        skill,
                        skillEntry.getMinProficiency(),
                        skillEntry.getIsMandatory(),
                        skillEntry.getWeightage()
                ));
            }
        }

        criteria.getSubjectCutoffs().clear();
        if (request.getSubjectCutoffs() != null) {
            for (SubjectCutoffEntry cutoffEntry : request.getSubjectCutoffs()) {
                String subjectName = StringNormalizer.normalize(cutoffEntry.getSubjectName());
                criteria.getSubjectCutoffs().add(new CriteriaSubjectCutoff(
                        criteria,
                        subjectName,
                        cutoffEntry.getMinMarksCutoff(),
                        cutoffEntry.getIsMandatory()
                ));
            }
        }

        HiringCriteria saved = hiringCriteriaRepository.save(criteria);
        return companyService.mapToCriteriaResponse(saved);
    }

    public void deleteCompanyCriteria(Long companyId, Long criteriaId) {
        CompanyProfile company = companyProfileRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));

        HiringCriteria criteria = hiringCriteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResourceNotFoundException("HiringCriteria", "id", criteriaId));

        if (!criteria.getCompany().getId().equals(company.getId())) {
            throw new BadRequestException("Hiring criteria ID " + criteriaId + " does not belong to company ID " + companyId);
        }

        hiringCriteriaRepository.delete(criteria);
    }

    // ==========================================
    // System Diagnostics & Dashboard Stats
    // ==========================================

    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getDashboardStats() {
        long totalStudents = studentProfileRepository.count();
        long totalTeachers = teacherProfileRepository.count();
        long totalCompanies = companyProfileRepository.count();
        long verifiedCompanies = companyProfileRepository.countByVerificationStatus(CompanyVerificationStatus.VERIFIED);
        long pendingCompanyVerifications = companyProfileRepository.countByVerificationStatus(CompanyVerificationStatus.NOT_VERIFIED);
        long pendingMarksVerifications = academicRecordRepository.countByIsVerifiedFalse();

        return AdminDashboardStatsResponse.builder()
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .totalCompanies(totalCompanies)
                .verifiedCompanies(verifiedCompanies)
                .pendingCompanyVerifications(pendingCompanyVerifications)
                .pendingMarksVerifications(pendingMarksVerifications)
                .totalSuccessfulMatches(totalStudents * Math.max(1, verifiedCompanies))
                .build();
    }

    @Transactional(readOnly = true)
    public SystemDiagnosticsResponse getSystemDiagnostics() {
        Runtime runtime = Runtime.getRuntime();
        long totalMem = runtime.totalMemory();
        long freeMem = runtime.freeMemory();
        long maxMem = runtime.maxMemory();
        long usedMem = totalMem - freeMem;

        Map<String, Object> memoryMap = new LinkedHashMap<>();
        memoryMap.put("usedMemoryMb", usedMem / (1024 * 1024));
        memoryMap.put("freeMemoryMb", freeMem / (1024 * 1024));
        memoryMap.put("totalAllocatedMemoryMb", totalMem / (1024 * 1024));
        memoryMap.put("maxAvailableHeapMb", maxMem / (1024 * 1024));
        memoryMap.put("jvmAvailableProcessors", runtime.availableProcessors());

        Map<String, Object> dbStats = new LinkedHashMap<>();
        dbStats.put("totalUsers", userRepository.count());
        dbStats.put("totalStudents", studentProfileRepository.count());
        dbStats.put("totalTeachers", teacherProfileRepository.count());
        dbStats.put("totalCompanies", companyProfileRepository.count());
        dbStats.put("pendingTeacherApprovals", teacherProfileRepository.countByApprovalStatus(ApprovalStatus.PENDING));

        Map<String, Object> mailStats = new LinkedHashMap<>();
        mailStats.put("dailyDispatchesCount", mailQuotaAndRateLimiter.getDailyDispatchCount());
        mailStats.put("dailyQuotaLimit", mailQuotaAndRateLimiter.getDailyQuotaLimit());
        mailStats.put("remainingDailyQuota", mailQuotaAndRateLimiter.getRemainingDailyQuota());

        return SystemDiagnosticsResponse.builder()
                .serverStatus("HEALTHY")
                .jvmVersion(System.getProperty("java.version"))
                .uptimeSeconds(ManagementFactory.getRuntimeMXBean().getUptime() / 1000)
                .memoryUsage(memoryMap)
                .databaseStats(dbStats)
                .mailQuotaStats(mailStats)
                .adminEmail(masterAdminEmail)
                .adminRecoveryEmail(adminRecoveryEmail)
                .build();
    }

    private CompanyProfileResponse mapToCompanyResponse(CompanyProfile company) {
        List<HiringCriteriaResponse> criteriaList = new ArrayList<>();
        if (company.getHiringCriteria() != null) {
            for (HiringCriteria hc : company.getHiringCriteria()) {
                criteriaList.add(HiringCriteriaResponse.builder()
                        .id(hc.getId())
                        .roleTitle(hc.getRoleTitle())
                        .jobDescription(hc.getJobDescription())
                        .minOverallPercentage(hc.getMinOverallPercentage())
                        .build());
            }
        }

        CompanyVerificationStatus status = company.getVerificationStatus() != null ? company.getVerificationStatus() : CompanyVerificationStatus.NOT_VERIFIED;
        String badge = status == CompanyVerificationStatus.VERIFIED ? "Verified" : "Not Verified";

        return CompanyProfileResponse.builder()
                .id(company.getId())
                .email(company.getUser() != null ? company.getUser().getEmail() : null)
                .companyName(company.getCompanyName())
                .industry(company.getIndustry())
                .websiteUrl(company.getWebsiteUrl())
                .location(company.getLocation())
                .description(company.getDescription())
                .logoUrl(company.getLogoUrl())
                .verificationStatus(status)
                .verificationBadge(badge)
                .hiringCriteria(criteriaList)
                .build();
    }
}
