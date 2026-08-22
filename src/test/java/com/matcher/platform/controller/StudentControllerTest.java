package com.matcher.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matcher.platform.config.MethodSecurityConfig;
import com.matcher.platform.config.SecurityConfig;
import com.matcher.platform.dto.request.SelfReportMarksRequest;
import com.matcher.platform.dto.request.SubjectMarkEntry;
import com.matcher.platform.dto.response.CompanyMatchResponse;
import com.matcher.platform.dto.response.StudentProfileResponse;
import com.matcher.platform.dto.response.SubjectMarkResponse;
import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import com.matcher.platform.entity.enums.MatchType;
import com.matcher.platform.exception.GlobalExceptionHandler;
import com.matcher.platform.security.CustomUserDetailsService;
import com.matcher.platform.security.JwtAuthenticationFilter;
import com.matcher.platform.security.RateLimitingFilter;
import com.matcher.platform.security.JwtService;
import com.matcher.platform.service.MatchingEngineService;
import com.matcher.platform.service.StudentService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
@Import({SecurityConfig.class, MethodSecurityConfig.class, JwtAuthenticationFilter.class, RateLimitingFilter.class, GlobalExceptionHandler.class})
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    @MockBean
    private MatchingEngineService matchingEngineService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("GET /api/v1/students/profile - 401 Unauthorized without auth")
    void getProfile_Unauthenticated_ForbiddenOrUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/students/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@university.edu", roles = "STUDENT")
    @DisplayName("GET /api/v1/students/profile - Success with ROLE_STUDENT")
    void getProfile_AuthorizedStudent_Success() throws Exception {
        StudentProfileResponse profile = StudentProfileResponse.builder()
                .id(1L)
                .email("student@university.edu")
                .fullName("Alex Morgan")
                .rollNumber("CS-2026-089")
                .verificationRemark("Verification by Teacher is Required")
                .build();

        when(studentService.getProfile("student@university.edu")).thenReturn(profile);

        mockMvc.perform(get("/api/v1/students/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.email").value("student@university.edu"))
                .andExpect(jsonPath("$.data.verificationRemark").value("Verification by Teacher is Required"));
    }

    @Test
    @WithMockUser(username = "student@university.edu", roles = "STUDENT")
    @DisplayName("POST /api/v1/students/marks/self-report - Success with valid marks list")
    void selfReportMarks_Success() throws Exception {
        SelfReportMarksRequest request = SelfReportMarksRequest.builder()
                .marks(List.of(
                        new SubjectMarkEntry("Data Structures & Algorithms", 88.0, "Semester 4"),
                        new SubjectMarkEntry("Database Management Systems", 92.5, "Semester 4")
                ))
                .build();

        List<SubjectMarkResponse> reportedMarks = List.of(
                SubjectMarkResponse.builder()
                        .subjectName("Data Structures & Algorithms")
                        .selfReportedMarks(88.0)
                        .isVerified(false)
                        .verificationRemark("Verification by Teacher is Required")
                        .build()
        );

        when(studentService.selfReportMarks(eq("student@university.edu"), any(SelfReportMarksRequest.class)))
                .thenReturn(reportedMarks);

        mockMvc.perform(post("/api/v1/students/marks/self-report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].selfReportedMarks").value(88.0))
                .andExpect(jsonPath("$.data[0].isVerified").value(false))
                .andExpect(jsonPath("$.data[0].verificationRemark").value("Verification by Teacher is Required"));
    }

    @Test
    @WithMockUser(username = "student@university.edu", roles = "STUDENT")
    @DisplayName("POST /api/v1/students/marks/self-report - Validation Error if mark exceeds 100")
    void selfReportMarks_InvalidScore_ThrowsValidationError() throws Exception {
        SelfReportMarksRequest request = SelfReportMarksRequest.builder()
                .marks(List.of(
                        new SubjectMarkEntry("Mathematics", 105.0, "Semester 2") // Invalid: > 100
                ))
                .build();

        mockMvc.perform(post("/api/v1/students/marks/self-report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(username = "student@university.edu", roles = "STUDENT")
    @DisplayName("GET /api/v1/students/matches - Returns matches with verification reminder and gap feedback")
    void getMatches_Success() throws Exception {
        CompanyMatchResponse match = CompanyMatchResponse.builder()
                .companyId(101L)
                .companyName("Acme Cloud Corp")
                .companyVerificationStatus(CompanyVerificationStatus.VERIFIED)
                .roleTitle("Associate Backend Engineer")
                .matchScore(92.0)
                .matchType(MatchType.STRICT)
                .isVerificationPending(true)
                .verificationRemark("Verification by Teacher is Required")
                .build();

        when(matchingEngineService.calculateMatchesForStudent("student@university.edu"))
                .thenReturn(List.of(match));

        mockMvc.perform(get("/api/v1/students/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].companyName").value("Acme Cloud Corp"))
                .andExpect(jsonPath("$.data[0].verificationRemark").value("Verification by Teacher is Required"));
    }
}
