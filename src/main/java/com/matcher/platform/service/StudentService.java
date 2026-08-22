package com.matcher.platform.service;

import com.matcher.platform.dto.request.SelfReportMarksRequest;
import com.matcher.platform.dto.request.StudentProfileRequest;
import com.matcher.platform.dto.request.StudentSkillRequest;
import com.matcher.platform.dto.request.SubjectMarkEntry;
import com.matcher.platform.dto.response.StudentProfileResponse;
import com.matcher.platform.dto.response.StudentSkillResponse;
import com.matcher.platform.dto.response.SubjectMarkResponse;
import com.matcher.platform.entity.Skill;
import com.matcher.platform.entity.StudentAcademicRecord;
import com.matcher.platform.entity.StudentProfile;
import com.matcher.platform.entity.StudentSkill;
import com.matcher.platform.entity.User;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.exception.BadRequestException;
import com.matcher.platform.exception.ResourceNotFoundException;
import com.matcher.platform.security.XssSanitizer;
import com.matcher.platform.repository.SkillRepository;
import com.matcher.platform.repository.StudentAcademicRecordRepository;
import com.matcher.platform.repository.StudentProfileRepository;
import com.matcher.platform.repository.StudentSkillRepository;
import com.matcher.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final StudentAcademicRecordRepository academicRecordRepository;
    private final SkillRepository skillRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final UserRepository userRepository;

    public StudentService(
            StudentProfileRepository studentProfileRepository,
            StudentAcademicRecordRepository academicRecordRepository,
            SkillRepository skillRepository,
            StudentSkillRepository studentSkillRepository,
            UserRepository userRepository
    ) {
        this.studentProfileRepository = studentProfileRepository;
        this.academicRecordRepository = academicRecordRepository;
        this.skillRepository = skillRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(String email) {
        StudentProfile profile = studentProfileRepository.findWithDetailsByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "email", email));
        return mapToProfileResponse(profile);
    }

    public StudentProfileResponse updateOrCreateProfile(String email, StudentProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(new User(email, RoleType.ROLE_STUDENT)));

        Optional<StudentProfile> existingOpt = studentProfileRepository.findByUserEmail(email);

        StudentProfile profile;
        if (existingOpt.isPresent()) {
            profile = existingOpt.get();
            // Check if roll number changed and conflicts
            if (!profile.getRollNumber().equalsIgnoreCase(request.getRollNumber()) &&
                    studentProfileRepository.existsByRollNumber(request.getRollNumber())) {
                throw new BadRequestException("Roll number '" + request.getRollNumber() + "' is already in use by another student");
            }
        } else {
            if (studentProfileRepository.existsByRollNumber(request.getRollNumber())) {
                throw new BadRequestException("Roll number '" + request.getRollNumber() + "' is already in use by another student");
            }
            profile = new StudentProfile();
            profile.setUser(user);
        }

        profile.setFullName(XssSanitizer.sanitize(request.getFullName()));
        profile.setRollNumber(XssSanitizer.sanitize(request.getRollNumber()));
        profile.setPhoneNumber(XssSanitizer.sanitize(request.getPhoneNumber()));
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(XssSanitizer.sanitize(request.getGender()));
        profile.setAddress(XssSanitizer.sanitize(request.getAddress()));
        profile.setBio(XssSanitizer.sanitize(request.getBio()));
        profile.setGithubUrl(XssSanitizer.sanitize(request.getGithubUrl()));
        profile.setLinkedinUrl(XssSanitizer.sanitize(request.getLinkedinUrl()));

        StudentProfile saved = studentProfileRepository.save(profile);
        return mapToProfileResponse(saved);
    }

    public List<SubjectMarkResponse> selfReportMarks(String email, SelfReportMarksRequest request) {
        StudentProfile profile = studentProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "email", email));

        List<SubjectMarkResponse> results = new ArrayList<>();
        for (SubjectMarkEntry entry : request.getMarks()) {
            Optional<StudentAcademicRecord> existingRecordOpt =
                    academicRecordRepository.findByStudentIdAndSubjectNameIgnoreCase(profile.getId(), entry.getSubjectName().trim());

            StudentAcademicRecord record;
            if (existingRecordOpt.isPresent()) {
                record = existingRecordOpt.get();
                record.setSelfReportedMarks(entry.getMarksObtained());
                record.setSemester(entry.getSemester());
            } else {
                record = StudentAcademicRecord.builder()
                        .student(profile)
                        .subjectName(entry.getSubjectName().trim())
                        .selfReportedMarks(entry.getMarksObtained())
                        .isVerified(false)
                        .semester(entry.getSemester())
                        .build();
            }
            StudentAcademicRecord saved = academicRecordRepository.save(record);
            results.add(mapToMarkResponse(saved));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<SubjectMarkResponse> getAcademicMarks(String email) {
        StudentProfile profile = studentProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "email", email));

        List<StudentAcademicRecord> records = academicRecordRepository.findByStudentId(profile.getId());
        return records.stream().map(this::mapToMarkResponse).toList();
    }

    public void addOrUpdateSkill(String email, StudentSkillRequest request) {
        StudentProfile profile = studentProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "email", email));

        String normalizedSkillName = request.getSkillName().trim();
        Skill skill = skillRepository.findByNameIgnoreCase(normalizedSkillName)
                .orElseGet(() -> skillRepository.save(new Skill(normalizedSkillName)));

        Optional<StudentSkill> existingOpt = studentSkillRepository.findByStudentIdAndSkillName(profile.getId(), normalizedSkillName);
        if (existingOpt.isPresent()) {
            StudentSkill existing = existingOpt.get();
            existing.setProficiency(request.getProficiency());
            existing.setYearsOfExperience(request.getYearsOfExperience());
            studentSkillRepository.save(existing);
        } else {
            StudentSkill studentSkill = new StudentSkill(profile, skill, request.getProficiency(), request.getYearsOfExperience());
            studentSkillRepository.save(studentSkill);
        }
    }

    public void deleteSkill(String email, String skillName) {
        StudentProfile profile = studentProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "email", email));
        studentSkillRepository.deleteByStudentIdAndSkillName(profile.getId(), skillName.trim());
    }

    public StudentProfileResponse mapToProfileResponse(StudentProfile profile) {
        List<SubjectMarkResponse> markResponses = new ArrayList<>();
        double totalMarks = 0.0;
        int markCount = 0;
        boolean allVerified = true;

        if (profile.getAcademicRecords() != null && !profile.getAcademicRecords().isEmpty()) {
            for (StudentAcademicRecord r : profile.getAcademicRecords()) {
                markResponses.add(mapToMarkResponse(r));
                totalMarks += r.getEffectiveMark();
                markCount++;
                if (!Boolean.TRUE.equals(r.getIsVerified())) {
                    allVerified = false;
                }
            }
        } else {
            allVerified = false;
        }

        List<StudentSkillResponse> skillResponses = new ArrayList<>();
        if (profile.getSkills() != null) {
            for (StudentSkill ss : profile.getSkills()) {
                skillResponses.add(new StudentSkillResponse(
                        ss.getId(),
                        ss.getSkill().getName(),
                        ss.getProficiency(),
                        ss.getYearsOfExperience()
                ));
            }
        }

        double aggregate = markCount > 0 ? (Math.round((totalMarks / markCount) * 10.0) / 10.0) : 0.0;

        return StudentProfileResponse.builder()
                .id(profile.getId())
                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
                .fullName(profile.getFullName())
                .rollNumber(profile.getRollNumber())
                .phoneNumber(profile.getPhoneNumber())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .address(profile.getAddress())
                .bio(profile.getBio())
                .githubUrl(profile.getGithubUrl())
                .linkedinUrl(profile.getLinkedinUrl())
                .aggregatePercentage(aggregate)
                .allMarksVerified(allVerified)
                .verificationRemark(allVerified ? "All Marks Officially Verified" : "Verification by Teacher is Required")
                .academicMarks(markResponses)
                .skills(skillResponses)
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
