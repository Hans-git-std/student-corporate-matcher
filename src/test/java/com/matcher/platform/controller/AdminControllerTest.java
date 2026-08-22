package com.matcher.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matcher.platform.config.MethodSecurityConfig;
import com.matcher.platform.config.SecurityConfig;
import com.matcher.platform.dto.request.CompanyStatusUpdateRequest;
import com.matcher.platform.dto.request.CreateTeacherRequest;
import com.matcher.platform.dto.response.AdminDashboardStatsResponse;
import com.matcher.platform.dto.response.CompanyProfileResponse;
import com.matcher.platform.dto.response.TeacherProfileResponse;
import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import com.matcher.platform.exception.GlobalExceptionHandler;
import com.matcher.platform.security.CustomUserDetailsService;
import com.matcher.platform.security.JwtAuthenticationFilter;
import com.matcher.platform.security.JwtService;
import com.matcher.platform.security.RateLimitingFilter;
import com.matcher.platform.security.SecurityGuard;
import com.matcher.platform.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, MethodSecurityConfig.class, JwtAuthenticationFilter.class, RateLimitingFilter.class, GlobalExceptionHandler.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean(name = "securityGuard")
    private SecurityGuard securityGuard;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(securityGuard.isMasterAdmin(org.mockito.ArgumentMatchers.any()))
                .thenReturn(true);
    }

    @Test
    @WithMockUser(username = "student@university.edu", roles = "STUDENT")
    @DisplayName("GET /api/v1/admin/stats - 403 Forbidden for Non-Admin roles")
    void getStats_ForbiddenForStudent() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@platform.com", roles = "ADMIN")
    @DisplayName("GET /api/v1/admin/stats - Success for Admin")
    void getStats_SuccessForAdmin() throws Exception {
        AdminDashboardStatsResponse stats = AdminDashboardStatsResponse.builder()
                .totalStudents(1450L)
                .totalTeachers(52L)
                .totalCompanies(98L)
                .verifiedCompanies(84L)
                .pendingCompanyVerifications(14L)
                .pendingMarksVerifications(120L)
                .totalSuccessfulMatches(4210L)
                .build();

        when(adminService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.totalStudents").value(1450));
    }

    @Test
    @WithMockUser(username = "admin@platform.com", roles = "ADMIN")
    @DisplayName("PATCH /api/v1/admin/companies/1/status - Admin approves company verification")
    void updateCompanyStatus_Success() throws Exception {
        CompanyStatusUpdateRequest request = CompanyStatusUpdateRequest.builder()
                .status(CompanyVerificationStatus.VERIFIED)
                .adminRemarks("All registration documents verified")
                .build();

        CompanyProfileResponse response = CompanyProfileResponse.builder()
                .id(1L)
                .companyName("Enterprise Co.")
                .verificationStatus(CompanyVerificationStatus.VERIFIED)
                .verificationBadge("Verified")
                .build();

        when(adminService.updateVerificationStatus(eq(1L), any(CompanyStatusUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/companies/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.verificationBadge").value("Verified"));
    }

    @Test
    @WithMockUser(username = "admin@platform.com", roles = "ADMIN")
    @DisplayName("POST /api/v1/admin/teachers - Provision new faculty account")
    void createTeacher_Success() throws Exception {
        CreateTeacherRequest request = CreateTeacherRequest.builder()
                .email("hopper@faculty.edu")
                .fullName("Dr. Grace Hopper")
                .employeeId("EMP-FAC-1003")
                .department("Computer Science")
                .phoneNumber("+1234567899")
                .assignedSubjects(List.of("Compilers", "Computer Systems"))
                .build();

        TeacherProfileResponse response = TeacherProfileResponse.builder()
                .id(20L)
                .email(request.getEmail())
                .fullName(request.getFullName())
                .employeeId(request.getEmployeeId())
                .department(request.getDepartment())
                .phoneNumber(request.getPhoneNumber())
                .assignedSubjects(request.getAssignedSubjects())
                .build();

        when(adminService.createTeacher(any(CreateTeacherRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fullName").value("Dr. Grace Hopper"));
    }

    @Test
    @WithMockUser(username = "admin@platform.com", roles = "ADMIN")
    @DisplayName("DELETE /api/v1/admin/companies/10 - Admin deletes company")
    void deleteCompany_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/companies/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Company ID 10 has been deleted"));
    }
}
