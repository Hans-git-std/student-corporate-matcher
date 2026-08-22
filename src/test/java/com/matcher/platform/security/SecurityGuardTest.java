package com.matcher.platform.security;

import com.matcher.platform.entity.CompanyProfile;
import com.matcher.platform.entity.HiringCriteria;
import com.matcher.platform.entity.StudentProfile;
import com.matcher.platform.entity.TeacherProfile;
import com.matcher.platform.entity.User;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.repository.CompanyProfileRepository;
import com.matcher.platform.repository.HiringCriteriaRepository;
import com.matcher.platform.repository.StudentProfileRepository;
import com.matcher.platform.repository.TeacherProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Principal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityGuardTest {

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private CompanyProfileRepository companyProfileRepository;

    @Mock
    private HiringCriteriaRepository hiringCriteriaRepository;

    @Mock
    private TeacherProfileRepository teacherProfileRepository;

    @InjectMocks
    private SecurityGuard securityGuard;

    private Principal studentPrincipal;
    private Principal attackerPrincipal;

    @BeforeEach
    void setUp() {
        studentPrincipal = () -> "student@university.edu";
        attackerPrincipal = () -> "attacker@university.edu";
        ReflectionTestUtils.setField(securityGuard, "masterAdminEmail", "admin@studentmatcher.com");
    }

    @Test
    @DisplayName("Should strictly allow only the designated single Master Admin")
    void testIsMasterAdmin() {
        Principal masterAdmin = () -> "admin@studentmatcher.com";
        Principal fakeAdmin = () -> "hacker@evil.com";

        assertThat(securityGuard.isMasterAdmin(masterAdmin)).isTrue();
        assertThat(securityGuard.isMasterAdmin(fakeAdmin)).isFalse();
    }

    @Test
    @DisplayName("Should verify that Teacher has an active provisioned profile")
    void testIsValidTeacher() {
        TeacherProfile teacherProfile = TeacherProfile.builder()
                .id(1L)
                .employeeId("EMP-101")
                .approvalStatus(com.matcher.platform.entity.enums.ApprovalStatus.APPROVED)
                .build();

        when(teacherProfileRepository.findByUserEmail("turing@faculty.edu"))
                .thenReturn(Optional.of(teacherProfile));
        when(teacherProfileRepository.findByUserEmail("student@university.edu"))
                .thenReturn(Optional.empty());

        Principal validTeacher = () -> "turing@faculty.edu";
        Principal invalidTeacher = () -> "student@university.edu";

        assertThat(securityGuard.isValidTeacher(validTeacher)).isTrue();
        assertThat(securityGuard.isValidTeacher(invalidTeacher)).isFalse();
    }

    @Test
    @DisplayName("Should permit student profile access only for actual owner (IDOR defense)")
    void testIsStudentOwner() {
        User user = new User("student@university.edu", RoleType.ROLE_STUDENT);
        StudentProfile profile = StudentProfile.builder()
                .id(1L)
                .user(user)
                .fullName("Alex Morgan")
                .build();

        when(studentProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        assertThat(securityGuard.isStudentOwner(studentPrincipal, 1L)).isTrue();
        assertThat(securityGuard.isStudentOwner(attackerPrincipal, 1L)).isFalse();
    }

    @Test
    @DisplayName("Should permit criteria mutation only for company owner (IDOR defense)")
    void testIsCompanyCriteriaOwner() {
        User companyUser = new User("recruiting@acme.com", RoleType.ROLE_COMPANY);
        CompanyProfile company = CompanyProfile.builder()
                .id(10L)
                .user(companyUser)
                .companyName("Acme Corp")
                .build();

        HiringCriteria criteria = HiringCriteria.builder()
                .id(100L)
                .company(company)
                .roleTitle("Backend Engineer")
                .build();

        when(hiringCriteriaRepository.findById(100L)).thenReturn(Optional.of(criteria));

        Principal companyPrincipal = () -> "recruiting@acme.com";
        Principal competitorPrincipal = () -> "evil@competitor.com";

        assertThat(securityGuard.isCompanyCriteriaOwner(companyPrincipal, 100L)).isTrue();
        assertThat(securityGuard.isCompanyCriteriaOwner(competitorPrincipal, 100L)).isFalse();
    }
}
