package com.matcher.platform.controller;

import com.matcher.platform.dto.common.ApiResponse;
import com.matcher.platform.dto.request.AdminOtpSendRequest;
import com.matcher.platform.dto.request.OtpSendRequest;
import com.matcher.platform.dto.request.OtpVerifyRequest;
import com.matcher.platform.dto.request.TokenRefreshRequest;
import com.matcher.platform.dto.response.AuthResponse;
import com.matcher.platform.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "1. Authentication", description = "Email OTP authentication, Admin 2-step verification, JWT token issuance, and 28-day refresh token rotation")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/otp/send")
    @Operation(summary = "Request Email OTP (Students & Companies)", description = "Generates and sends a secure 6-digit numeric OTP with 5-minute TTL to the provided email address.")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "OTP has been dispatched to " + request.getEmail() + ". Code valid for 5 minutes.",
                "OTP generated successfully"
        ));
    }

    @PostMapping("/admin/otp/send")
    @Operation(summary = "Request Admin OTP (Step 1: Password Verification)", description = "Validates Master Admin password and dispatches 6-digit login OTP to primary and emergency recovery email.")
    public ResponseEntity<ApiResponse<String>> sendAdminOtp(@Valid @RequestBody AdminOtpSendRequest request) {
        authService.sendAdminOtp(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Master Admin authentication step 1 successful. OTP has been dispatched to admin email and recovery email.",
                "Admin OTP generated successfully"
        ));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP & Login", description = "Validates the 6-digit OTP and returns a stateless Access Token along with a 28-day Refresh Token.")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtpAndLogin(@Valid @RequestBody OtpVerifyRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Authentication successful"));
    }

    @PostMapping({"/refresh", "/token/refresh"})
    @Operation(summary = "Refresh Access Token", description = "Validates existing refresh token, rotates it, and generates a fresh short-lived access token.")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout & Invalidate Session", description = "Revokes refresh token and clears active server-side session references.")
    public ResponseEntity<ApiResponse<String>> logout(Principal principal) {
        if (principal != null) {
            authService.logout(principal.getName());
        }
        return ResponseEntity.ok(ApiResponse.success("Session successfully terminated", "Logged out"));
    }
}
