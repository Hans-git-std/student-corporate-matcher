package com.matcher.platform.service;

import com.matcher.platform.dto.request.AdminOtpSendRequest;
import com.matcher.platform.dto.request.OtpSendRequest;
import com.matcher.platform.dto.request.OtpVerifyRequest;
import com.matcher.platform.dto.request.TokenRefreshRequest;
import com.matcher.platform.dto.response.AuthResponse;
import com.matcher.platform.entity.RefreshToken;
import com.matcher.platform.entity.StudentProfile;
import com.matcher.platform.entity.TeacherProfile;
import com.matcher.platform.entity.User;
import com.matcher.platform.entity.enums.ApprovalStatus;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.exception.BadRequestException;
import com.matcher.platform.exception.ForbiddenException;
import com.matcher.platform.exception.ResourceNotFoundException;
import com.matcher.platform.exception.UnauthorizedException;
import com.matcher.platform.repository.RefreshTokenRepository;
import com.matcher.platform.repository.StudentProfileRepository;
import com.matcher.platform.repository.TeacherProfileRepository;
import com.matcher.platform.repository.UserRepository;
import com.matcher.platform.security.EmailService;
import com.matcher.platform.security.JwtService;
import com.matcher.platform.security.MailQuotaAndRateLimiter;
import com.matcher.platform.security.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final MailQuotaAndRateLimiter mailQuotaAndRateLimiter;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.admin.email:admin@studentmatcher.com}")
    private String masterAdminEmail;

    @Value("${app.security.admin.recovery-email:hans31144@gmail.com}")
    private String adminRecoveryEmail;

    @Value("${app.security.jwt.refresh-token-expiration-ms:2419200000}")
    private long refreshTokenExpirationMs;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            TeacherProfileRepository teacherProfileRepository,
            StudentProfileRepository studentProfileRepository,
            OtpService otpService,
            JwtService jwtService,
            EmailService emailService,
            MailQuotaAndRateLimiter mailQuotaAndRateLimiter,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.mailQuotaAndRateLimiter = mailQuotaAndRateLimiter;
        this.passwordEncoder = passwordEncoder;
    }

    public void sendAdminOtp(AdminOtpSendRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (masterAdminEmail == null || !normalizedEmail.equalsIgnoreCase(masterAdminEmail.trim())) {
            throw new ForbiddenException("Unauthorized: Invalid administrative credentials.");
        }

        User adminUser = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ForbiddenException("Master Admin account is not initialized."));

        if (adminUser.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), adminUser.getPasswordHash())) {
            throw new UnauthorizedException("Invalid master admin password.");
        }

        mailQuotaAndRateLimiter.checkAndRecordMailDispatch(normalizedEmail);
        otpService.generateAndSendOtp(normalizedEmail);

        // Also dispatch to emergency recovery email if enabled
        if (Boolean.TRUE.equals(request.getSendToRecoveryEmail()) &&
                adminRecoveryEmail != null && !adminRecoveryEmail.isBlank() &&
                !adminRecoveryEmail.equalsIgnoreCase(normalizedEmail)) {
            try {
                log.info("Dispatching mirror security OTP to Admin Emergency Recovery Email: {}", adminRecoveryEmail);
                emailService.sendOtpEmail(adminRecoveryEmail.trim(), "[EMERGENCY RECOVERY OTP SENT FOR " + normalizedEmail + "]");
            } catch (Exception e) {
                log.warn("Failed to dispatch recovery email: {}", e.getMessage());
            }
        }
    }

    public void sendOtp(OtpSendRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        RoleType requestedRole = request.getRole() != null ? request.getRole() : RoleType.ROLE_STUDENT;

        // 1. Security Guard: Prevent bypassing Admin 2-Step Login
        if (requestedRole == RoleType.ROLE_ADMIN) {
            throw new ForbiddenException("Administrator login requires 2-step verification (Password + OTP) via /api/v1/auth/admin/otp/send.");
        }

        // 2. Security Guard: Verify Teacher registration and approval status
        if (requestedRole == RoleType.ROLE_TEACHER) {
            Optional<TeacherProfile> teacherOpt = teacherProfileRepository.findByUserEmail(normalizedEmail);
            if (teacherOpt.isEmpty()) {
                throw new ForbiddenException("No faculty profile found for this email. Please register at /api/v1/teachers/register first.");
            }
            TeacherProfile teacher = teacherOpt.get();
            if (teacher.getApprovalStatus() == ApprovalStatus.PENDING) {
                throw new ForbiddenException("No further action, verification is in waiting");
            }
            if (teacher.getApprovalStatus() == ApprovalStatus.REJECTED) {
                throw new ForbiddenException("Your faculty account was rejected. Reason: " +
                        (teacher.getRejectionReason() != null ? teacher.getRejectionReason() : "Contact administration."));
            }
        }

        // 3. Auto-provision user account if it doesn't exist (Only allowed for STUDENT and COMPANY)
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isEmpty()) {
            if (requestedRole == RoleType.ROLE_ADMIN || requestedRole == RoleType.ROLE_TEACHER) {
                throw new ForbiddenException("Privileged roles cannot be self-provisioned via public OTP.");
            }
            userRepository.save(new User(normalizedEmail, requestedRole));
        }

        // 4. Rate limiting & Daily SMTP Mail Quota Protection
        mailQuotaAndRateLimiter.checkAndRecordMailDispatch(normalizedEmail);

        // 5. Generate and dispatch secure OTP
        otpService.generateAndSendOtp(normalizedEmail);
    }

    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // 1. Verify OTP code
        otpService.verifyOtp(normalizedEmail, request.getOtp());

        // 2. Fetch User
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", normalizedEmail));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new UnauthorizedException("User account is disabled. Please contact administrator.");
        }

        // 3. Check Teacher approval status if teacher
        if (user.getRole() == RoleType.ROLE_TEACHER) {
            TeacherProfile profile = teacherProfileRepository.findByUserEmail(normalizedEmail)
                    .orElseThrow(() -> new ForbiddenException("Faculty profile not found."));
            if (profile.getApprovalStatus() == ApprovalStatus.PENDING) {
                throw new ForbiddenException("No further action, verification is in waiting");
            }
            if (profile.getApprovalStatus() == ApprovalStatus.REJECTED) {
                throw new ForbiddenException("Faculty account registration has been rejected.");
            }
        }

        // 4. Auto-provision StudentProfile baseline record if not already present
        if (user.getRole() == RoleType.ROLE_STUDENT) {
            try {
                if (studentProfileRepository.findByUserId(user.getId()).isEmpty()) {
                    StudentProfile studentProfile = new StudentProfile();
                    studentProfile.setUser(user);
                    studentProfileRepository.save(studentProfile);
                }
            } catch (Exception e) {
                log.warn("Could not auto-provision blank StudentProfile entity for userId {}: {}", user.getId(), e.getMessage());
            }
        }

        // 5. Generate JWT Access Token
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());

        // 6. Generate & Save Refresh Token (28 Days long-lived lifespan)
        String rawRefreshToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawRefreshToken);
        Instant expiresAt = Instant.now().plus(refreshTokenExpirationMs, ChronoUnit.MILLIS);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public AuthResponse refreshToken(TokenRefreshRequest request) {
        String rawRefreshToken = request.getRefreshToken().trim();
        String tokenHash = hashToken(rawRefreshToken);

        // 1. Find token by hash
        RefreshToken existingToken = refreshTokenRepository.findByTokenHashAndIsRevokedFalse(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid or revoked refresh token"));

        if (existingToken.getExpiresAt().isBefore(Instant.now())) {
            existingToken.setIsRevoked(true);
            refreshTokenRepository.save(existingToken);
            throw new UnauthorizedException("Refresh token has expired. Please log in again.");
        }

        User user = existingToken.getUser();
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new UnauthorizedException("User account is disabled.");
        }

        // 2. Refresh Token Rotation: Revoke used token and issue a fresh pair
        existingToken.setIsRevoked(true);
        refreshTokenRepository.save(existingToken);

        String newRawRefreshToken = UUID.randomUUID().toString();
        String newTokenHash = hashToken(newRawRefreshToken);
        Instant newExpiresAt = Instant.now().plus(refreshTokenExpirationMs, ChronoUnit.MILLIS);

        RefreshToken newTokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(newTokenHash)
                .expiresAt(newExpiresAt)
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(newTokenEntity);

        // 3. Issue new access token
        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public void logout(String email) {
        userRepository.findByEmail(email).ifPresent(user -> refreshTokenRepository.revokeAllByUserId(user.getId()));
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 cryptographic algorithm not available", e);
        }
    }
}
