package com.matcher.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matcher.platform.config.MethodSecurityConfig;
import com.matcher.platform.config.SecurityConfig;
import com.matcher.platform.dto.request.TeacherMarkVerificationEntry;
import com.matcher.platform.dto.request.VerifyMarksRequest;
import com.matcher.platform.dto.response.SubjectMarkResponse;
import com.matcher.platform.dto.response.TeacherProfileResponse;
import com.matcher.platform.exception.GlobalExceptionHandler;
import com.matcher.platform.security.CustomUserDetailsService;
import com.matcher.platform.security.JwtAuthenticationFilter;
import com.matcher.platform.security.JwtService;
import com.matcher.platform.security.RateLimitingFilter;
import com.matcher.platform.security.SecurityGuard;
import com.matcher.platform.service.TeacherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeacherController.class)
@Import({SecurityConfig.class, MethodSecurityConfig.class, JwtAuthenticationFilter.class, RateLimitingFilter.class, GlobalExceptionHandler.class})
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean(name = "securityGuard")
    private SecurityGuard securityGuard;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(securityGuard.isValidTeacher(org.mockito.ArgumentMatchers.any()))
                .thenReturn(true);
    }

    @Test
    @WithMockUser(username = "turing@faculty.edu", roles = "TEACHER")
    @DisplayName("GET /api/v1/teachers/profile - Success with ROLE_TEACHER")
    void getProfile_AuthorizedTeacher_Success() throws Exception {
        TeacherProfileResponse teacher = TeacherProfileResponse.builder()
                .id(1L)
                .email("turing@faculty.edu")
                .fullName("Dr. Alan Turing")
                .employeeId("EMP-FAC-1002")
                .department("Computer Science & Engineering")
                .build();

        when(teacherService.getProfile("turing@faculty.edu")).thenReturn(teacher);

        mockMvc.perform(get("/api/v1/teachers/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeId").value("EMP-FAC-1002"))
                .andExpect(jsonPath("$.data.fullName").value("Dr. Alan Turing"));
    }

    @Test
    @WithMockUser(username = "student@university.edu", roles = "STUDENT")
    @DisplayName("POST /api/v1/teachers/students/CS-2026-089/marks/verify - 403 Forbidden for Student role")
    void verifyMarks_ForbiddenForStudent() throws Exception {
        VerifyMarksRequest request = VerifyMarksRequest.builder()
                .verifiedMarks(List.of(
                        new TeacherMarkVerificationEntry("Data Structures & Algorithms", 95.0, "Semester 4", "Verified")
                ))
                .build();

        mockMvc.perform(post("/api/v1/teachers/students/CS-2026-089/marks/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "turing@faculty.edu", roles = "TEACHER")
    @DisplayName("POST /api/v1/teachers/students/CS-2026-089/marks/verify - Success for Teacher")
    void verifyMarks_SuccessForTeacher() throws Exception {
        VerifyMarksRequest request = VerifyMarksRequest.builder()
                .verifiedMarks(List.of(
                        new TeacherMarkVerificationEntry("Data Structures & Algorithms", 95.0, "Semester 4", "Official records confirmed")
                ))
                .build();

        List<SubjectMarkResponse> verifiedList = List.of(
                SubjectMarkResponse.builder()
                        .subjectName("Data Structures & Algorithms")
                        .verifiedMarks(95.0)
                        .isVerified(true)
                        .semester("Semester 4")
                        .verifiedByTeacherId("EMP-FAC-1002")
                        .verifiedByTeacherName("Dr. Alan Turing")
                        .verifiedAt(Instant.now())
                        .verificationRemark("Officially Verified")
                        .build()
        );

        when(teacherService.verifyStudentMarks(eq("turing@faculty.edu"), eq("CS-2026-089"), any(VerifyMarksRequest.class)))
                .thenReturn(verifiedList);

        mockMvc.perform(post("/api/v1/teachers/students/CS-2026-089/marks/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].isVerified").value(true))
                .andExpect(jsonPath("$.data[0].verifiedMarks").value(95.0))
                .andExpect(jsonPath("$.data[0].verificationRemark").value("Officially Verified"));
    }
}
