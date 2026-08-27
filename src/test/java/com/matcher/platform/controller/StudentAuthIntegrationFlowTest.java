package com.matcher.platform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matcher.platform.dto.request.OtpSendRequest;
import com.matcher.platform.dto.request.OtpVerifyRequest;
import com.matcher.platform.dto.request.StudentProfileRequest;
import com.matcher.platform.dto.request.TokenRefreshRequest;
import com.matcher.platform.entity.User;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.repository.OtpTokenRepository;
import com.matcher.platform.repository.RefreshTokenRepository;
import com.matcher.platform.repository.StudentProfileRepository;
import com.matcher.platform.repository.UserRepository;
import com.matcher.platform.security.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StudentAuthIntegrationFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private com.matcher.platform.repository.CompanyProfileRepository companyProfileRepository;

    @Autowired
    private com.matcher.platform.repository.HiringCriteriaRepository hiringCriteriaRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private EmailService emailService;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    private final String testStudentEmail = "test_s3_student@domain.com";

    @BeforeEach
    void cleanDb() {
        new org.springframework.transaction.support.TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            refreshTokenRepository.deleteAll();
            studentProfileRepository.deleteAll();
            hiringCriteriaRepository.deleteAll();
            companyProfileRepository.deleteAll();
            userRepository.findAll().stream()
                    .filter(u -> !"admin@studentmatcher.com".equalsIgnoreCase(u.getEmail()))
                    .forEach(userRepository::delete);
        });
    }

    @Test
    @DisplayName("Full End-to-End Student Lifecycle: OTP Request -> Verify & Token -> Baseline Profile -> Update Profile -> Refresh Token")
    void testFullStudentAuthAndProfileLifecycle() throws Exception {
        // -------------------------------------------------------------
        // Step 1: Request OTP for new student account
        // -------------------------------------------------------------
        OtpSendRequest sendReq = new OtpSendRequest(testStudentEmail, RoleType.ROLE_STUDENT);
        mockMvc.perform(post("/api/v1/auth/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        // Capture the exact 6-digit OTP code dispatched to EmailService
        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendOtpEmail(eq(testStudentEmail), otpCaptor.capture());
        String generatedOtp = otpCaptor.getValue();
        assertThat(generatedOtp).matches("^[0-9]{6}$");

        // -------------------------------------------------------------
        // Step 2: Verify OTP and exchange for Access & Refresh Tokens
        // -------------------------------------------------------------
        OtpVerifyRequest verifyReq = new OtpVerifyRequest(testStudentEmail, generatedOtp);
        MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.email").value(testStudentEmail))
                .andExpect(jsonPath("$.data.role").value("ROLE_STUDENT"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode authData = objectMapper.readTree(verifyResult.getResponse().getContentAsString()).get("data");
        String accessToken = authData.get("accessToken").asText();
        String refreshToken = authData.get("refreshToken").asText();

        // -------------------------------------------------------------
        // Step 3: Access Student Profile using JWT (Baseline uninitialized profile)
        // -------------------------------------------------------------
        mockMvc.perform(get("/api/v1/students/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.email").value(testStudentEmail))
                .andExpect(jsonPath("$.data.aggregatePercentage").value(0.0))
                .andExpect(jsonPath("$.data.verificationRemark").value("Profile Setup Required"));

        // -------------------------------------------------------------
        // Step 4: Populate & Save Student Profile (Roll Number, Full Name, Bio)
        // -------------------------------------------------------------
        StudentProfileRequest profileReq = StudentProfileRequest.builder()
                .fullName("Sarah Connor")
                .rollNumber("CS-2026-S3")
                .phoneNumber("+1234567890")
                .dateOfBirth(LocalDate.of(2003, 7, 21))
                .gender("Female")
                .address("100 University Plaza, Tech City")
                .bio("CS Junior specializing in backend and machine learning")
                .githubUrl("https://github.com/sarahconnor")
                .linkedinUrl("https://linkedin.com/in/sarahconnor")
                .build();

        mockMvc.perform(put("/api/v1/students/profile")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.fullName").value("Sarah Connor"))
                .andExpect(jsonPath("$.data.rollNumber").value("CS-2026-S3"));

        // -------------------------------------------------------------
        // Step 5: Refresh Access Token with Refresh Token Rotation
        // -------------------------------------------------------------
        TokenRefreshRequest refreshReq = new TokenRefreshRequest(refreshToken);
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("Matching Engine End-to-End: Tester C1 Company Criteria ('Testing' min 55) matches S3 Student (Score 85) without 500 MultipleBagFetchException")
    void testCompanyMatchingForTesterS3() throws Exception {
        // 1. Authenticate / Setup Student S3
        OtpSendRequest sendReq = new OtpSendRequest("s3_tester@university.edu", RoleType.ROLE_STUDENT);
        mockMvc.perform(post("/api/v1/auth/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendReq)))
                .andExpect(status().isOk());

        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, atLeastOnce()).sendOtpEmail(eq("s3_tester@university.edu"), otpCaptor.capture());
        String otp = otpCaptor.getValue();

        MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OtpVerifyRequest("s3_tester@university.edu", otp))))
                .andExpect(status().isOk())
                .andReturn();

        String studentToken = objectMapper.readTree(verifyResult.getResponse().getContentAsString())
                .get("data").get("accessToken").asText();

        // 2. Setup Student Profile & Self-Report 85 marks in 'Testing'
        StudentProfileRequest profileReq = StudentProfileRequest.builder()
                .fullName("Tester S3")
                .rollNumber("ROLL-S3-2026")
                .build();
        mockMvc.perform(put("/api/v1/students/profile")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isOk());

        var marksReq = new com.matcher.platform.dto.request.SelfReportMarksRequest(List.of(
                new com.matcher.platform.dto.request.SubjectMarkEntry("Testing", 85.0, "Semester 5")
        ));
        mockMvc.perform(post("/api/v1/students/marks/self-report")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(marksReq)))
                .andExpect(status().isOk());

        // 3. Register Company C1 and create Criteria ('Testing' cutoff 55.0)
        User companyUser = userRepository.save(new User("c1_company@domain.com", RoleType.ROLE_COMPANY));
        var companyProfile = companyProfileRepository.save(com.matcher.platform.entity.CompanyProfile.builder()
                .user(companyUser)
                .companyName("C1 Testing Technologies")
                .verificationStatus(com.matcher.platform.entity.enums.CompanyVerificationStatus.VERIFIED)
                .build());

        var criteria = com.matcher.platform.entity.HiringCriteria.builder()
                .company(companyProfile)
                .roleTitle("QA Test Engineer")
                .minOverallPercentage(50.0)
                .isActive(true)
                .build();
        criteria.getSubjectCutoffs().add(new com.matcher.platform.entity.CriteriaSubjectCutoff(criteria, "Testing", 55.0, true));
        hiringCriteriaRepository.save(criteria);

        // 4. Call GET /api/v1/students/matches to verify successful execution without 500 error
        mockMvc.perform(get("/api/v1/students/matches")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].companyName").value("C1 Testing Technologies"))
                .andExpect(jsonPath("$.data[0].roleTitle").value("QA Test Engineer"))
                .andExpect(jsonPath("$.data[0].matchScore").value(100.0))
                .andExpect(jsonPath("$.data[0].isVerificationPending").value(true));
    }
}
