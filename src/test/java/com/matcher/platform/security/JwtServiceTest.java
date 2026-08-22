package com.matcher.platform.security;

import com.matcher.platform.entity.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, 900000L, 604800000L);
    }

    @Test
    @DisplayName("Should generate valid JWT access token and extract claims correctly")
    void testGenerateAndValidateToken() {
        String token = jwtService.generateAccessToken("student@university.edu", RoleType.ROLE_STUDENT);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token, "student@university.edu")).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("student@university.edu");
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_STUDENT");
    }

    @Test
    @DisplayName("Should return false for invalid or wrong user token")
    void testInvalidUserToken() {
        String token = jwtService.generateAccessToken("student@university.edu", RoleType.ROLE_STUDENT);

        assertThat(jwtService.isTokenValid(token, "other@university.edu")).isFalse();
    }
}
