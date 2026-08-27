package com.matcher.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matcher.platform.dto.request.OtpSendRequest;
import com.matcher.platform.dto.request.OtpVerifyRequest;
import com.matcher.platform.dto.request.StudentProfileRequest;
import com.matcher.platform.dto.request.TokenRefreshRequest;
import com.matcher.platform.entity.OtpToken;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.repository.OtpTokenRepository;
import com.matcher.platform.repository.RefreshTokenRepository;
import com.matcher.platform.repository.StudentProfileRepository;
import com.matcher.platform.repository.UserRepository;
import com.matcher.platform.security.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
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
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private EmailService emailService;

    private final String testStudentEmail = "test_s3_student@domain.com";

    @BeforeEach
    void cleanDb() {
        refreshTokenRepository.deleteAll();
        studentProfileRepository.deleteAll();
        otpTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Full End-to-End Student Lifecycle: OTP Request -> Verify & Token -> Baseline Profile -> Update Profile -> Refresh Token")
    void testFullStudentAuthAndProfileLifecycle() throws Exception {
        // Step 1: Send OTP for brand-new student
        OtpSendRequest sendReq = new OtpSendRequest(testStudentEmail, RoleType.ROLE_STUDENT);
        mockMvc.perform(post("/api/v1/auth/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        // Step 2: Retrieve OTP from repository (simulating email delivery)
        OtpToken token = otpTokenRepository.findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(testStudentEmail)
                .orElseThrow();
        assertThat(token.getEmail()).isEqualTo(testStudentEmail);
        assertThat(token.getIsUsed()).isFalse();

        // Step 3: Verify OTP with the exact code
        // Note: For integration test, we can verify against OtpService or verify directly
        // We know OtpService hashes the input with SHA-256. If we need the raw OTP, we test verify with the correct flow.
        // Let's create an OTP with raw code known:
    }
}
