package com.matcher.platform.service;

import com.matcher.platform.dto.response.CompanyMatchResponse;
import com.matcher.platform.entity.*;
import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import com.matcher.platform.entity.enums.MatchType;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.entity.enums.SkillProficiency;
import com.matcher.platform.repository.HiringCriteriaRepository;
import com.matcher.platform.repository.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingEngineServiceTest {

    @Mock
    private HiringCriteriaRepository hiringCriteriaRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @InjectMocks
    private MatchingEngineService matchingEngineService;

    private StudentProfile studentProfile;
    private HiringCriteria strictFitCriteria;
    private HiringCriteria deficitCriteria;

    @BeforeEach
    void setUp() {
        User studentUser = new User("student@university.edu", RoleType.ROLE_STUDENT);
        studentProfile = StudentProfile.builder()
                .id(1L)
                .user(studentUser)
                .fullName("Alex Morgan")
                .rollNumber("CS-2026-089")
                .academicRecords(new ArrayList<>())
                .skills(new ArrayList<>())
                .build();

        // Skill: Java (ADVANCED)
        Skill javaSkill = new Skill(1L, "Java", "Programming");
        studentProfile.getSkills().add(new StudentSkill(studentProfile, javaSkill, SkillProficiency.ADVANCED, 2.0));

        // Academic Record: DSA (65.0 - UNVERIFIED)
        studentProfile.getAcademicRecords().add(StudentAcademicRecord.builder()
                .student(studentProfile)
                .subjectName("Data Structures & Algorithms")
                .selfReportedMarks(65.0)
                .isVerified(false)
                .build());

        // Academic Record: OS (85.0 - VERIFIED)
        studentProfile.getAcademicRecords().add(StudentAcademicRecord.builder()
                .student(studentProfile)
                .subjectName("Operating Systems")
                .selfReportedMarks(85.0)
                .verifiedMarks(85.0)
                .isVerified(true)
                .build());

        // Criteria 1: Strict fit (requires OS >= 70, Java >= INTERMEDIATE)
        CompanyProfile company1 = CompanyProfile.builder()
                .id(101L)
                .companyName("Cloud Corp")
                .verificationStatus(CompanyVerificationStatus.VERIFIED)
                .build();

        strictFitCriteria = HiringCriteria.builder()
                .id(1L)
                .company(company1)
                .roleTitle("Junior Cloud Engineer")
                .minOverallPercentage(60.0)
                .isActive(true)
                .requiredSkills(List.of(new CriteriaRequiredSkill(1L, null, javaSkill, SkillProficiency.INTERMEDIATE, true, 1.0)))
                .subjectCutoffs(List.of(new CriteriaSubjectCutoff(1L, null, "Operating Systems", 70.0, true)))
                .build();

        // Criteria 2: Deficit fit (requires DSA >= 75, so deficit is 10.0 marks)
        CompanyProfile company2 = CompanyProfile.builder()
                .id(102L)
                .companyName("Data Systems Inc")
                .verificationStatus(CompanyVerificationStatus.VERIFIED)
                .build();

        deficitCriteria = HiringCriteria.builder()
                .id(2L)
                .company(company2)
                .roleTitle("Algorithms Engineer")
                .minOverallPercentage(70.0)
                .isActive(true)
                .requiredSkills(List.of(new CriteriaRequiredSkill(2L, null, javaSkill, SkillProficiency.ADVANCED, true, 1.0)))
                .subjectCutoffs(List.of(new CriteriaSubjectCutoff(2L, null, "Data Structures & Algorithms", 75.0, true)))
                .build();
    }

    @Test
    @DisplayName("Should evaluate strict match and fallback to weighted match with deficit gap remarks when strict count < 3")
    void testDualModeMatchingWithGapsAndVerificationRemark() {
        when(studentProfileRepository.findWithDetailsByEmail("student@university.edu"))
                .thenReturn(Optional.of(studentProfile));
        when(hiringCriteriaRepository.findAllActiveWithDetails())
                .thenReturn(List.of(strictFitCriteria, deficitCriteria));

        List<CompanyMatchResponse> matches = matchingEngineService.calculateMatchesForStudent("student@university.edu");

        assertThat(matches).hasSize(2);

        // Strict Match Verification
        CompanyMatchResponse strictMatch = matches.stream()
                .filter(m -> m.getCompanyId().equals(101L))
                .findFirst()
                .orElseThrow();
        assertThat(strictMatch.getMatchType()).isEqualTo(MatchType.STRICT);
        assertThat(strictMatch.getMatchScore()).isEqualTo(100.0);
        assertThat(strictMatch.getIsVerificationPending()).isTrue();
        assertThat(strictMatch.getVerificationRemark()).isEqualTo("Verification by Teacher is Required");

        // Relaxed Weighted Match with Deficit Feedback
        CompanyMatchResponse relaxedMatch = matches.stream()
                .filter(m -> m.getCompanyId().equals(102L))
                .findFirst()
                .orElseThrow();
        assertThat(relaxedMatch.getMatchType()).isEqualTo(MatchType.RELAXED_WEIGHTED);
        assertThat(relaxedMatch.getSubjectGaps()).isNotEmpty();
        assertThat(relaxedMatch.getSubjectGaps().get(0).getScoreDeficit()).isEqualTo(10.0);
        assertThat(relaxedMatch.getAcademicGapSummary()).contains("A 10.0 score more is required in Data Structures & Algorithms");
        assertThat(relaxedMatch.getVerificationRemark()).isEqualTo("Verification by Teacher is Required");
    }
}
