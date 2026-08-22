package com.matcher.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matcher.platform.config.SecurityConfig;
import com.matcher.platform.dto.request.OtpSendRequest;
import com.matcher.platform.dto.request.OtpVerifyRequest;
import com.matcher.platform.dto.response.AuthResponse;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.exception.GlobalExceptionHandler;
import com.matcher.platform.security.CustomUserDetailsService;
import com.matcher.platform.security.JwtAuthenticationFilter;
import com.matcher.platform.security.RateLimitingFilter;
import com.matcher.platform.security.JwtService;
import com.matcher.platform.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RateLimitingFilter.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/v1/auth/otp/send - Success with valid email")
    void sendOtp_Success() throws Exception {
        OtpSendRequest request = new OtpSendRequest("test@university.edu");

        mockMvc.perform(post("/api/v1/auth/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("OTP generated successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/otp/verify - Success with valid 6 digit OTP")
    void verifyOtp_Success() throws Exception {
        OtpVerifyRequest request = new OtpVerifyRequest("test@university.edu", "123456");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(900L)
                .email("test@university.edu")
                .role(RoleType.ROLE_STUDENT)
                .build();

        when(authService.verifyOtp(any(OtpVerifyRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.data.email").value("test@university.edu"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/otp/send - Validation Failure on malformed email")
    void sendOtp_InvalidEmail_ThrowsValidationError() throws Exception {
        OtpSendRequest request = new OtpSendRequest("invalid-email-format");

        mockMvc.perform(post("/api/v1/auth/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/otp/verify - Validation Failure on short OTP")
    void verifyOtp_InvalidOtpLength_ThrowsValidationError() throws Exception {
        OtpVerifyRequest request = new OtpVerifyRequest("test@university.edu", "123"); // Needs 6 digits

        mockMvc.perform(post("/api/v1/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("otp"));
    }
}
