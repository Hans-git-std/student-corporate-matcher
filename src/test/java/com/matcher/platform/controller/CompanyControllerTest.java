package com.matcher.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matcher.platform.config.MethodSecurityConfig;
import com.matcher.platform.config.SecurityConfig;
import com.matcher.platform.dto.request.CompanyRegisterRequest;
import com.matcher.platform.dto.response.CompanyProfileResponse;
import com.matcher.platform.dto.response.CompanyPublicResponse;
import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import com.matcher.platform.exception.GlobalExceptionHandler;
import com.matcher.platform.security.CustomUserDetailsService;
import com.matcher.platform.security.JwtAuthenticationFilter;
import com.matcher.platform.security.RateLimitingFilter;
import com.matcher.platform.security.JwtService;
import com.matcher.platform.service.CompanyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
@Import({SecurityConfig.class, MethodSecurityConfig.class, JwtAuthenticationFilter.class, RateLimitingFilter.class, GlobalExceptionHandler.class})
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("GET /api/v1/companies/public - Accessible anonymously")
    void getPublicCompanies_PermitAll() throws Exception {
        CompanyPublicResponse sample = CompanyPublicResponse.builder()
                .id(1L)
                .companyName("Acme Technologies Inc.")
                .verificationStatus(CompanyVerificationStatus.VERIFIED)
                .verificationBadge("Verified")
                .build();

        when(companyService.getPublicCompanies()).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/v1/companies/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].companyName").value("Acme Technologies Inc."));
    }

    @Test
    @DisplayName("POST /api/v1/companies/register - Self-registers with 'NOT_VERIFIED' status")
    void registerCompany_ReturnsUnverifiedStatus() throws Exception {
        CompanyRegisterRequest request = CompanyRegisterRequest.builder()
                .email("contact@newstartup.io")
                .companyName("New Startup Inc.")
                .industry("AI & Machine Learning")
                .websiteUrl("https://newstartup.io")
                .location("New York, NY")
                .description("Next-gen AI solutions")
                .build();

        CompanyProfileResponse response = CompanyProfileResponse.builder()
                .id(2L)
                .email("contact@newstartup.io")
                .companyName("New Startup Inc.")
                .verificationStatus(CompanyVerificationStatus.NOT_VERIFIED)
                .verificationBadge("Not Verified")
                .build();

        when(companyService.registerCompany(any(CompanyRegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/companies/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.verificationStatus").value("NOT_VERIFIED"))
                .andExpect(jsonPath("$.data.verificationBadge").value("Not Verified"));
    }
}
