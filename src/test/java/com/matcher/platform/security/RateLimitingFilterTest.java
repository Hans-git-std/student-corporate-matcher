package com.matcher.platform.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingFilterTest {

    private final RateLimitingFilter rateLimitingFilter = new RateLimitingFilter();

    @Test
    @DisplayName("Should allow requests within limit")
    void testAllowRequestsWithinLimit() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/auth/otp/send");
        request.setRemoteAddr("192.168.1.100");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        rateLimitingFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should block and return HTTP 429 when rate limit exceeded")
    void testBlockWhenLimitExceeded() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/auth/otp/send");
        request.setRemoteAddr("10.0.0.99");

        // Send 60 requests
        for (int i = 0; i < 60; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();
            rateLimitingFilter.doFilter(request, response, filterChain);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        // 61st request should be rejected with 429 Too Many Requests
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        rateLimitingFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).contains("Too many requests");
    }
}
