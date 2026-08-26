package com.matcher.platform.service;

import com.matcher.platform.dto.request.OtpSendRequest;
import com.matcher.platform.dto.request.OtpVerifyRequest;
import com.matcher.platform.dto.request.TokenRefreshRequest;
import com.matcher.platform.dto.response.AuthResponse;
import com.matcher.platform.entity.RefreshToken;
import com.matcher.platform.entity.StudentProfile;
import com.matcher.platform.entity.User;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.exception.ForbiddenException;
import com.matcher.platform.repository.RefreshTokenRepository;
import com.matcher.platform.repository.StudentProfileRepository;
import com.matcher.platform.repository.TeacherProfileRepository;
import com.matcher.platform.repository.UserRepository;
import com.matcher.platform.security.JwtService;
import com.matcher.platform.security.MailQuotaAndRateLimiter;
import com.matcher.platform.security.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TeacherProfileRepository teacherProfileRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private JwtService jwtService;

    @Mock
    private MailQuotaAndRateLimiter mailQuotaAndRateLimiter;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L, "student@university.edu", RoleType.ROLE_STUDENT, true, null, null);
        ReflectionTestUtils.setField(authService, "masterAdminEmail", "admin@studentmatcher.com");
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 2419200000L);
    }

    @Test
    @DisplayName("Should send OTP and auto-provision user if new student")
    void testSendOtp() {
        OtpSendRequest request = new OtpSendRequest("newstudent@university.edu", RoleType.ROLE_STUDENT);

        when(userRepository.findByEmail("newstudent@university.edu")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(new User("newstudent@university.edu", RoleType.ROLE_STUDENT));

        authService.sendOtp(request);

        verify(mailQuotaAndRateLimiter).checkAndRecordMailDispatch("newstudent@university.edu");
        verify(otpService).generateAndSendOtp("newstudent@university.edu");
    }

    @Test
    @DisplayName("Should forbid unauthorized Admin self-registration")
    void testSendOtp_UnauthorizedAdminRejected() {
        OtpSendRequest request = new OtpSendRequest("hacker@evil.com", RoleType.ROLE_ADMIN);

        assertThatThrownBy(() -> authService.sendOtp(request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Should forbid unauthorized Teacher self-registration")
    void testSendOtp_UnauthorizedTeacherRejected() {
        OtpSendRequest request = new OtpSendRequest("student@university.edu", RoleType.ROLE_TEACHER);

        when(teacherProfileRepository.findByUserEmail("student@university.edu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.sendOtp(request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Should verify OTP and return valid AuthResponse with access token, 28-day refresh token, and auto-provision StudentProfile")
    void testVerifyOtp() {
        OtpVerifyRequest request = new OtpVerifyRequest("student@university.edu", "123456");

        when(otpService.verifyOtp("student@university.edu", "123456")).thenReturn(true);
        when(userRepository.findByEmail("student@university.edu")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(jwtService.generateAccessToken("student@university.edu", RoleType.ROLE_STUDENT))
                .thenReturn("mock-access-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.verifyOtp(request);

        assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getEmail()).isEqualTo("student@university.edu");
        assertThat(response.getRole()).isEqualTo(RoleType.ROLE_STUDENT);
        verify(studentProfileRepository).save(any(StudentProfile.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should rotate refresh token and issue new token pair")
    void testRefreshToken() {
        TokenRefreshRequest request = new TokenRefreshRequest("valid-refresh-token");

        RefreshToken existingToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .tokenHash("some-hash")
                .expiresAt(Instant.now().plus(28, ChronoUnit.DAYS))
                .isRevoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHashAndIsRevokedFalse(anyString()))
                .thenReturn(Optional.of(existingToken));
        when(jwtService.generateAccessToken("student@university.edu", RoleType.ROLE_STUDENT))
                .thenReturn("new-mock-access-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.refreshToken(request);

        assertThat(response.getAccessToken()).isEqualTo("new-mock-access-token");
        assertThat(existingToken.getIsRevoked()).isTrue();
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }
}
