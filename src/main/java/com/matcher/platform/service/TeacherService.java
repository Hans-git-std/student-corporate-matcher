package com.matcher.platform.service;

import com.matcher.platform.dto.request.TeacherMarkVerificationEntry;
import com.matcher.platform.dto.request.TeacherProfileRequest;
import com.matcher.platform.dto.request.TeacherRegisterRequest;
import com.matcher.platform.dto.request.VerifyMarksRequest;
import com.matcher.platform.dto.response.SubjectMarkResponse;
import com.matcher.platform.dto.response.TeacherProfileResponse;
import com.matcher.platform.entity.StudentAcademicRecord;
import com.matcher.platform.entity.StudentProfile;
import com.matcher.platform.entity.TeacherProfile;
import com.matcher.platform.entity.TeacherSubject;
import com.matcher.platform.entity.User;
import com.matcher.platform.entity.enums.ApprovalStatus;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.exception.BadRequestException;
import com.matcher.platform.exception.ResourceNotFoundException;
import com.matcher.platform.repository.StudentAcademicRecordRepository;
import com.matcher.platform.repository.StudentProfileRepository;
import com.matcher.platform.repository.TeacherProfileRepository;
import com.matcher.platform.repository.TeacherSubjectRepository;
import com.matcher.platform.repository.UserRepository;
import com.matcher.platform.security.XssSanitizer;
import com.matcher.platform.util.StringNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TeacherService {

    private final TeacherProfileRepository teacherProfileRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentAcademicRecordRepository academicRecordRepository;
    private final UserRepository userRepository;

    public TeacherService(
            TeacherProfileRepository teacherProfileRepository,
            TeacherSubjectRepository teacherSubjectRepository,
            StudentProfileRepository studentProfileRepository,
            StudentAcademicRecordRepository academicRecordRepository,
            UserRepository userRepository
    ) {
        this.teacherProfileRepository = teacherProfileRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.academicRecordRepository = academicRecordRepository;
        this.userRepository = userRepository;
    }

    public TeacherProfileResponse registerTeacher(TeacherRegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String employeeId = request.getEmployeeId().trim();

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new BadRequestException("An account with email '" + normalizedEmail + "' already exists.");
        }

        if (teacherProfileRepository.existsByEmployeeId(employeeId)) {
            throw new BadRequestException("Employee ID '" + employeeId + "' is already registered.");
        }

        User user = new User(normalizedEmail, RoleType.ROLE_TEACHER);
        User savedUser = userRepository.save(user);

        TeacherProfile profile = TeacherProfile.builder()
                .user(savedUser)
                .fullName(XssSanitizer.sanitize(request.getFullName()))
                .employeeId(XssSanitizer.sanitize(employeeId))
                .department(XssSanitizer.sanitize(request.getDepartment()))
                .designation(XssSanitizer.sanitize(request.getDesignation()))
                .phoneNumber(XssSanitizer.sanitize(request.getPhoneNumber()))
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        TeacherProfile savedProfile = teacherProfileRepository.save(profile);

        if (request.getAssignedSubjects() != null && !request.getAssignedSubjects().isEmpty()) {
            List<TeacherSubject> subjects = new ArrayList<>();
            for (String sub : request.getAssignedSubjects()) {
                if (sub != null && !sub.trim().isEmpty()) {
                    String normSub = StringNormalizer.normalize(sub);
                    subjects.add(new TeacherSubject(savedProfile, XssSanitizer.sanitize(normSub)));
                }
            }
            teacherSubjectRepository.saveAll(subjects);
            savedProfile.setAssignedSubjects(subjects);
        }

        return mapToProfileResponse(savedProfile);
    }

    @Transactional(readOnly = true)
    public TeacherProfileResponse getProfile(String email) {
        TeacherProfile profile = teacherProfileRepository.findWithSubjectsByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherProfile", "email", email));
        return mapToProfileResponse(profile);
    }

    public TeacherProfileResponse updateOrCreateProfile(String email, TeacherProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(new User(email, RoleType.ROLE_TEACHER)));

        Optional<TeacherProfile> existingOpt = teacherProfileRepository.findByUserEmail(email);
        TeacherProfile profile;

        if (existingOpt.isPresent()) {
            profile = existingOpt.get();
            if (!profile.getEmployeeId().equalsIgnoreCase(request.getEmployeeId()) &&
                    teacherProfileRepository.existsByEmployeeId(request.getEmployeeId())) {
                throw new BadRequestException("Employee ID '" + request.getEmployeeId() + "' is already registered");
            }
        } else {
            if (teacherProfileRepository.existsByEmployeeId(request.getEmployeeId())) {
                throw new BadRequestException("Employee ID '" + request.getEmployeeId() + "' is already registered");
            }
            profile = new TeacherProfile();
            profile.setUser(user);
        }

        profile.setFullName(XssSanitizer.sanitize(request.getFullName()));
        profile.setEmployeeId(XssSanitizer.sanitize(request.getEmployeeId()));
        profile.setDepartment(XssSanitizer.sanitize(request.getDepartment()));
        profile.setPhoneNumber(XssSanitizer.sanitize(request.getPhoneNumber()));

        TeacherProfile saved = teacherProfileRepository.save(profile);

        // Update assigned subjects
        if (request.getAssignedSubjects() != null) {
            teacherSubjectRepository.deleteByTeacherId(saved.getId());
            List<TeacherSubject> subjects = new ArrayList<>();
            for (String sub : request.getAssignedSubjects()) {
                if (sub != null && !sub.trim().isEmpty()) {
                    String normSub = StringNormalizer.normalize(sub);
                    subjects.add(new TeacherSubject(saved, normSub));
                }
            }
            teacherSubjectRepository.saveAll(subjects);
            saved.setAssignedSubjects(subjects);
        }

        return mapToProfileResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SubjectMarkResponse> getStudentMarksByRollNumber(String rollNumber) {
        StudentProfile student = studentProfileRepository.findByRollNumber(rollNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "rollNumber", rollNumber));

        List<StudentAcademicRecord> records = academicRecordRepository.findByStudentId(student.getId());
        return records.stream().map(this::mapToMarkResponse).toList();
    }

    public List<SubjectMarkResponse> verifyStudentMarks(String teacherEmail, String rollNumber, VerifyMarksRequest request) {
        TeacherProfile teacher = teacherProfileRepository.findWithSubjectsByEmail(teacherEmail)
                .or(() -> teacherProfileRepository.findByUserEmail(teacherEmail))
                .orElseThrow(() -> new ResourceNotFoundException("TeacherProfile", "email", teacherEmail));

        if (teacher.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new BadRequestException("Teacher account is not approved for official academic verification.");
        }

        StudentProfile student = studentProfileRepository.findByRollNumber(rollNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "rollNumber", rollNumber));

        List<TeacherSubject> assignedSubjects = teacher.getAssignedSubjects();
        boolean hasSubjectRestrictions = assignedSubjects != null && !assignedSubjects.isEmpty();

        List<SubjectMarkResponse> results = new ArrayList<>();
        List<StudentAcademicRecord> existingStudentRecords = academicRecordRepository.findByStudentId(student.getId());

        for (TeacherMarkVerificationEntry entry : request.getVerifiedMarks()) {
            String rawSubject = entry.getSubjectName();
            String normalizedSubject = StringNormalizer.normalize(rawSubject);

            // Safety guard: Enforce teacher subject verification authority if assigned subjects exist
            if (hasSubjectRestrictions) {
                boolean isAuthorized = assignedSubjects.stream().anyMatch(as ->
                        StringNormalizer.isFuzzyMatch(as.getSubjectName(), normalizedSubject));

                if (!isAuthorized) {
                    List<String> authorizedNames = assignedSubjects.stream().map(TeacherSubject::getSubjectName).toList();
                    throw new BadRequestException(String.format(
                            "Faculty member '%s' is not authorized to verify marks for subject '%s'. Authorized assigned subjects: %s",
                            teacher.getFullName(), rawSubject, authorizedNames));
                }
            }

            // Find existing record: exact ignore-case lookup first, then fuzzy match to avoid duplicates on minor typos
            Optional<StudentAcademicRecord> existingOpt = existingStudentRecords.stream()
                    .filter(r -> r.getSubjectName().equalsIgnoreCase(normalizedSubject))
                    .findFirst()
                    .or(() -> existingStudentRecords.stream()
                            .filter(r -> StringNormalizer.isFuzzyMatch(r.getSubjectName(), normalizedSubject))
                            .findFirst());

            StudentAcademicRecord record;
            if (existingOpt.isPresent()) {
                record = existingOpt.get();
                record.setVerifiedMarks(entry.getVerifiedMarks());
                record.setIsVerified(true);
                record.setVerifiedByTeacher(teacher);
                record.setVerifiedAt(Instant.now());
                if (entry.getSemester() != null) record.setSemester(StringNormalizer.normalize(entry.getSemester()));
                if (entry.getRemarks() != null) record.setRemarks(entry.getRemarks());
            } else {
                record = StudentAcademicRecord.builder()
                        .student(student)
                        .subjectName(normalizedSubject)
                        .selfReportedMarks(entry.getVerifiedMarks())
                        .verifiedMarks(entry.getVerifiedMarks())
                        .isVerified(true)
                        .semester(entry.getSemester() != null ? StringNormalizer.normalize(entry.getSemester()) : null)
                        .verifiedByTeacher(teacher)
                        .verifiedAt(Instant.now())
                        .remarks(entry.getRemarks())
                        .build();
            }
            StudentAcademicRecord saved = academicRecordRepository.save(record);
            results.add(mapToMarkResponse(saved));
        }

        return results;
    }

    public TeacherProfileResponse mapToProfileResponse(TeacherProfile profile) {
        List<String> subjects = new ArrayList<>();
        if (profile.getAssignedSubjects() != null) {
            for (TeacherSubject ts : profile.getAssignedSubjects()) {
                subjects.add(ts.getSubjectName());
            }
        }

        return TeacherProfileResponse.builder()
                .id(profile.getId())
                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
                .fullName(profile.getFullName())
                .employeeId(profile.getEmployeeId())
                .department(profile.getDepartment())
                .designation(profile.getDesignation())
                .phoneNumber(profile.getPhoneNumber())
                .approvalStatus(profile.getApprovalStatus())
                .rejectionReason(profile.getRejectionReason())
                .verifiedByAdminAt(profile.getVerifiedByAdminAt())
                .createdAt(profile.getCreatedAt())
                .assignedSubjects(subjects)
                .build();
    }

    private SubjectMarkResponse mapToMarkResponse(StudentAcademicRecord r) {
        boolean isVer = Boolean.TRUE.equals(r.getIsVerified());
        return SubjectMarkResponse.builder()
                .id(r.getId())
                .subjectName(r.getSubjectName())
                .selfReportedMarks(r.getSelfReportedMarks())
                .verifiedMarks(r.getVerifiedMarks())
                .isVerified(isVer)
                .semester(r.getSemester())
                .verifiedByTeacherId(r.getVerifiedByTeacher() != null ? r.getVerifiedByTeacher().getEmployeeId() : null)
                .verifiedByTeacherName(r.getVerifiedByTeacher() != null ? r.getVerifiedByTeacher().getFullName() : null)
                .verifiedAt(r.getVerifiedAt())
                .verificationRemark(isVer ? "Officially Verified" : "Verification by Teacher is Required")
                .build();
    }
}
