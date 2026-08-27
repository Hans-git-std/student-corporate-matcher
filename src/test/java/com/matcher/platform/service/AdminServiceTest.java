package com.matcher.platform.service;

import com.matcher.platform.dto.request.CompanyStatusUpdateRequest;
import com.matcher.platform.dto.request.CompanyWithCriteriaRequest;
import com.matcher.platform.dto.request.CreateTeacherRequest;
import com.matcher.platform.dto.request.HiringCriteriaRequest;
import com.matcher.platform.dto.response.AdminDashboardStatsResponse;
import com.matcher.platform.dto.response.CompanyProfileResponse;
import com.matcher.platform.dto.response.HiringCriteriaResponse;
import com.matcher.platform.dto.response.TeacherProfileResponse;
import com.matcher.platform.entity.CompanyProfile;
import com.matcher.platform.entity.HiringCriteria;
import com.matcher.platform.entity.TeacherProfile;
import com.matcher.platform.entity.User;
import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.repository.*;
import com.matcher.platform.security.MailQuotaAndRateLimiter;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private CompanyProfileRepository companyProfileRepository;

    @Mock
    private TeacherProfileRepository teacherProfileRepository;

    @Mock
    private TeacherSubjectRepository teacherSubjectRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private StudentAcademicRecordRepository academicRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HiringCriteriaRepository hiringCriteriaRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private TeacherService teacherService;

    @Mock
    private StudentService studentService;

    @Mock
    private CompanyService companyService;

    @Mock
    private MailQuotaAndRateLimiter mailQuotaAndRateLimiter;

    @InjectMocks
    private AdminService adminService;

    private CompanyProfile company;

    @BeforeEach
    void setUp() {
        User user = new User("corp@enterprise.com", RoleType.ROLE_COMPANY);
        company = CompanyProfile.builder()
                .id(1L)
                .user(user)
                .companyName("Enterprise Ltd")
                .verificationStatus(CompanyVerificationStatus.NOT_VERIFIED)
                .hiringCriteria(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should approve company verification status")
    void testUpdateVerificationStatus() {
        CompanyStatusUpdateRequest request = new CompanyStatusUpdateRequest(CompanyVerificationStatus.VERIFIED, "Reviewed and verified");

        when(companyProfileRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyProfileRepository.save(any(CompanyProfile.class))).thenAnswer(i -> i.getArgument(0));

        CompanyProfileResponse response = adminService.updateVerificationStatus(1L, request);

        assertThat(response.getVerificationStatus()).isEqualTo(CompanyVerificationStatus.VERIFIED);
        assertThat(response.getVerificationBadge()).isEqualTo("Verified");
    }

    @Test
    @DisplayName("Should provision new teacher account with assigned subjects")
    void testCreateTeacher() {
        CreateTeacherRequest request = CreateTeacherRequest.builder()
                .email("newteacher@faculty.edu")
                .fullName("Prof. John Von Neumann")
                .employeeId("EMP-FAC-1005")
                .department("Computer Systems")
                .assignedSubjects(List.of("Computer Architecture", "Automata Theory"))
                .build();

        User user = new User("newteacher@faculty.edu", RoleType.ROLE_TEACHER);
        when(userRepository.existsByEmail("newteacher@faculty.edu")).thenReturn(false);
        when(teacherProfileRepository.existsByEmployeeId("EMP-FAC-1005")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(teacherProfileRepository.save(any(TeacherProfile.class))).thenAnswer(i -> {
            TeacherProfile t = i.getArgument(0);
            t.setId(10L);
            return t;
        });

        TeacherProfileResponse mockResponse = TeacherProfileResponse.builder()
                .id(10L)
                .email("newteacher@faculty.edu")
                .fullName("Prof. John Von Neumann")
                .employeeId("EMP-FAC-1005")
                .department("Computer Systems")
                .assignedSubjects(List.of("Computer Architecture", "Automata Theory"))
                .build();

        when(teacherService.mapToProfileResponse(any(TeacherProfile.class))).thenReturn(mockResponse);

        TeacherProfileResponse response = adminService.createTeacher(request);

        assertThat(response.getEmail()).isEqualTo("newteacher@faculty.edu");
        assertThat(response.getEmployeeId()).isEqualTo("EMP-FAC-1005");
        assertThat(response.getAssignedSubjects()).containsExactly("Computer Architecture", "Automata Theory");
    }

    @Test
    @DisplayName("Should calculate system wide statistics")
    void testGetDashboardStats() {
        when(studentProfileRepository.count()).thenReturn(100L);
        when(teacherProfileRepository.count()).thenReturn(10L);
        when(companyProfileRepository.count()).thenReturn(20L);
        when(companyProfileRepository.countByVerificationStatus(CompanyVerificationStatus.VERIFIED)).thenReturn(15L);
        when(companyProfileRepository.countByVerificationStatus(CompanyVerificationStatus.NOT_VERIFIED)).thenReturn(5L);
        when(academicRecordRepository.countByIsVerifiedFalse()).thenReturn(35L);

        AdminDashboardStatsResponse stats = adminService.getDashboardStats();

        assertThat(stats.getTotalStudents()).isEqualTo(100L);
        assertThat(stats.getTotalTeachers()).isEqualTo(10L);
        assertThat(stats.getTotalCompanies()).isEqualTo(20L);
        assertThat(stats.getVerifiedCompanies()).isEqualTo(15L);
        assertThat(stats.getPendingCompanyVerifications()).isEqualTo(5L);
        assertThat(stats.getPendingMarksVerifications()).isEqualTo(35L);
    }

    @Test
    @DisplayName("Should return list of pending companies requiring verification")
    void testGetPendingCompanies() {
        when(companyProfileRepository.findByVerificationStatus(CompanyVerificationStatus.NOT_VERIFIED))
                .thenReturn(List.of(company));

        List<CompanyProfileResponse> result = adminService.getPendingCompanies();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCompanyName()).isEqualTo("Enterprise Ltd");
        assertThat(result.get(0).getVerificationStatus()).isEqualTo(CompanyVerificationStatus.NOT_VERIFIED);
    }

    @Test
    @DisplayName("Should get criteria for specific company")
    void testGetCompanyCriteria() {
        HiringCriteria hc = HiringCriteria.builder()
                .id(101L)
                .company(company)
                .roleTitle("Backend Engineer")
                .minOverallPercentage(75.0)
                .build();

        HiringCriteriaResponse mockResp = HiringCriteriaResponse.builder()
                .id(101L)
                .roleTitle("Backend Engineer")
                .minOverallPercentage(75.0)
                .build();

        when(companyProfileRepository.findById(1L)).thenReturn(Optional.of(company));
        when(hiringCriteriaRepository.findByCompanyId(1L)).thenReturn(List.of(hc));
        when(companyService.mapToCriteriaResponse(hc)).thenReturn(mockResp);

        List<HiringCriteriaResponse> list = adminService.getCompanyCriteria(1L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getRoleTitle()).isEqualTo("Backend Engineer");
    }

    @Test
    @DisplayName("Should create company with criteria in composite operation")
    void testCreateCompanyWithCriteria() {
        CompanyWithCriteriaRequest request = CompanyWithCriteriaRequest.builder()
                .email("newcorp@hiring.com")
                .companyName("Tech Innovators")
                .industry("IT")
                .websiteUrl("https://techinnovators.com")
                .location("Bangalore")
                .description("Cloud software")
                .hiringCriteria(List.of(
                        HiringCriteriaRequest.builder()
                                .roleTitle("Full Stack Dev")
                                .minOverallPercentage(70.0)
                                .build()
                ))
                .build();

        User user = new User("newcorp@hiring.com", RoleType.ROLE_COMPANY);
        CompanyProfile savedComp = CompanyProfile.builder()
                .id(50L)
                .user(user)
                .companyName("Tech Innovators")
                .verificationStatus(CompanyVerificationStatus.VERIFIED)
                .hiringCriteria(new ArrayList<>())
                .build();

        when(userRepository.existsByEmail("newcorp@hiring.com")).thenReturn(false);
        when(companyProfileRepository.existsByCompanyNameIgnoreCase("Tech Innovators")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(companyProfileRepository.save(any(CompanyProfile.class))).thenReturn(savedComp);
        when(companyProfileRepository.findById(50L)).thenReturn(Optional.of(savedComp));

        HiringCriteria hc = HiringCriteria.builder()
                .id(201L)
                .company(savedComp)
                .roleTitle("Full Stack Dev")
                .minOverallPercentage(70.0)
                .build();

        when(hiringCriteriaRepository.save(any(HiringCriteria.class))).thenReturn(hc);
        when(companyService.mapToCriteriaResponse(any(HiringCriteria.class))).thenReturn(
                HiringCriteriaResponse.builder().id(201L).roleTitle("Full Stack Dev").build()
        );

        CompanyProfileResponse response = adminService.createCompanyWithCriteria(request);

        assertThat(response.getCompanyName()).isEqualTo("Tech Innovators");
        assertThat(response.getVerificationStatus()).isEqualTo(CompanyVerificationStatus.VERIFIED);
    }
}
