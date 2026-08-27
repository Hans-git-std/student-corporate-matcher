package com.matcher.platform.controller;

import com.matcher.platform.dto.response.BulkSyncResult;
import com.matcher.platform.dto.response.CompanyMatchResponse;
import com.matcher.platform.entity.*;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.entity.enums.SkillProficiency;
import com.matcher.platform.repository.*;
import com.matcher.platform.service.AdminService;
import com.matcher.platform.service.MatchingEngineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CompanyBulkSyncIntegrationTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private MatchingEngineService matchingEngineService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Test
    @DisplayName("Bulk Sync loads Info directory companies and matching engine successfully matches mechanical student")
    public void testBulkSyncAndMechanicalStudentMatching() {
        // 1. Run Bulk Sync from C:\Users\hans3\Workspace\Info
        BulkSyncResult result = adminService.syncCompaniesFromInfoFolder("C:\\Users\\hans3\\Workspace\\Info");

        assertThat(result).isNotNull();
        assertThat(result.getTotalFilesScanned()).isGreaterThanOrEqualTo(150);
        assertThat(result.getCreatedCount() + result.getSkippedCount()).isGreaterThanOrEqualTo(150);
        assertThat(result.getErrorCount()).isEqualTo(0);


        // 2. Create a simulated Mechanical Engineering student
        User studentUser = userRepository.save(new User("rahul.mech@university.edu", RoleType.ROLE_STUDENT));
        
        Skill cadSkill = skillRepository.findByNameIgnoreCase("CATIA V5")
                .orElseGet(() -> skillRepository.save(new Skill("CATIA V5")));
        Skill solidworksSkill = skillRepository.findByNameIgnoreCase("SolidWorks")
                .orElseGet(() -> skillRepository.save(new Skill("SolidWorks")));
        Skill gdntSkill = skillRepository.findByNameIgnoreCase("GD&T")
                .orElseGet(() -> skillRepository.save(new Skill("GD&T")));

        StudentProfile student = StudentProfile.builder()
                .user(studentUser)
                .rollNumber("ME2026001")
                .fullName("Rahul Sharma")
                .build();

        student.getAcademicRecords().add(StudentAcademicRecord.builder()
                .student(student)
                .subjectName("Strength of Materials")
                .selfReportedMarks(75.0)
                .verifiedMarks(75.0)
                .isVerified(true)
                .build());

        student.getAcademicRecords().add(StudentAcademicRecord.builder()
                .student(student)
                .subjectName("Design of Machine Elements")
                .selfReportedMarks(78.0)
                .verifiedMarks(78.0)
                .isVerified(true)
                .build());

        student.getAcademicRecords().add(StudentAcademicRecord.builder()
                .student(student)
                .subjectName("Applied Thermodynamics")
                .selfReportedMarks(72.0)
                .verifiedMarks(72.0)
                .isVerified(true)
                .build());

        student.getAcademicRecords().add(StudentAcademicRecord.builder()
                .student(student)
                .subjectName("Theory of Machines")
                .selfReportedMarks(70.0)
                .verifiedMarks(70.0)
                .isVerified(true)
                .build());

        student.getSkills().add(new StudentSkill(student, cadSkill, SkillProficiency.ADVANCED, 1.5));
        student.getSkills().add(new StudentSkill(student, solidworksSkill, SkillProficiency.ADVANCED, 2.0));
        student.getSkills().add(new StudentSkill(student, gdntSkill, SkillProficiency.INTERMEDIATE, 1.0));

        studentProfileRepository.save(student);

        // 3. Evaluate Matches for the student
        List<CompanyMatchResponse> matches = matchingEngineService.calculateMatchesForStudent("rahul.mech@university.edu");

        assertThat(matches).isNotEmpty();
        System.out.println("==========================================================");
        System.out.println(">>> TOTAL MATCHES GENERATED FOR MECHANICAL STUDENT: " + matches.size());
        
        // Print top 5 matches
        matches.stream().limit(5).forEach(m -> {
            System.out.println("  * " + m.getCompanyName() + " | Role: " + m.getRoleTitle() + " | Score: " + m.getMatchScore() + "% | Type: " + m.getMatchType());
        });
        System.out.println("==========================================================");

        // Ensure at least one top mechanical company matched with high score
        assertThat(matches.get(0).getMatchScore()).isGreaterThan(70.0);
    }
}
