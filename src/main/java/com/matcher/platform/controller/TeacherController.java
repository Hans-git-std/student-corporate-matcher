package com.matcher.platform.controller;

import com.matcher.platform.dto.common.ApiResponse;
import com.matcher.platform.dto.request.TeacherProfileRequest;
import com.matcher.platform.dto.request.TeacherRegisterRequest;
import com.matcher.platform.dto.request.VerifyMarksRequest;
import com.matcher.platform.dto.response.SubjectMarkResponse;
import com.matcher.platform.dto.response.TeacherProfileResponse;
import com.matcher.platform.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/teachers")
@Tag(name = "3. Teacher Management & Marks Verification", description = "Teacher profile, registration, department assignments, and official student mark verification by Roll Number")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register Faculty Account", description = "Public self-registration for teachers. Account is placed in PENDING status until approved by Admin.")
    public ResponseEntity<ApiResponse<TeacherProfileResponse>> register(@Valid @RequestBody TeacherRegisterRequest request) {
        TeacherProfileResponse response = teacherService.registerTeacher(request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Faculty registration submitted successfully. Your account is pending administrator verification before login."),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('TEACHER') and @securityGuard.isValidTeacher(principal)")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get Teacher Profile", description = "Retrieves authenticated faculty member's profile and assigned subjects.")
    public ResponseEntity<ApiResponse<TeacherProfileResponse>> getProfile(Principal principal) {
        String email = getEmail(principal);
        TeacherProfileResponse response = teacherService.getProfile(email);
        return ResponseEntity.ok(ApiResponse.success(response, "Teacher profile retrieved successfully"));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('TEACHER') and @securityGuard.isValidTeacher(principal)")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update Teacher Profile", description = "Updates contact details, department, and assigned subjects.")
    public ResponseEntity<ApiResponse<TeacherProfileResponse>> updateProfile(
            Principal principal,
            @Valid @RequestBody TeacherProfileRequest request
    ) {
        String email = getEmail(principal);
        TeacherProfileResponse response = teacherService.updateOrCreateProfile(email, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Teacher profile updated successfully"));
    }

    @GetMapping("/pending-verifications")
    @PreAuthorize("hasRole('TEACHER') and @securityGuard.isValidTeacher(principal)")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get Pending Student Verifications", description = "Retrieves a list of students with unverified marks matching teacher assigned subjects.")
    public ResponseEntity<ApiResponse<List<com.matcher.platform.dto.response.PendingVerificationStudentResponse>>> getPendingVerifications(Principal principal) {
        String email = getEmail(principal);
        List<com.matcher.platform.dto.response.PendingVerificationStudentResponse> response = teacherService.getPendingStudentVerifications(email);
        return ResponseEntity.ok(ApiResponse.success(response, "Pending student verifications retrieved successfully"));
    }

    @GetMapping("/students/{rollNumber}/marks")
    @PreAuthorize("hasRole('TEACHER') and @securityGuard.isValidTeacher(principal)")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get Student Marks by Roll Number", description = "Fetches self-reported and verified marks for a student by their academic roll number.")
    public ResponseEntity<ApiResponse<List<SubjectMarkResponse>>> getStudentMarks(@PathVariable String rollNumber) {
        List<SubjectMarkResponse> marks = teacherService.getStudentMarksByRollNumber(rollNumber);
        return ResponseEntity.ok(ApiResponse.success(marks, "Student marks retrieved successfully"));
    }

    @PostMapping("/students/{rollNumber}/marks/verify")
    @PreAuthorize("hasRole('TEACHER') and @securityGuard.isValidTeacher(principal)")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Verify Student Marks", description = "Officially verifies academic scores for a student. Updates verification audit status.")
    public ResponseEntity<ApiResponse<List<SubjectMarkResponse>>> verifyMarks(
            Principal principal,
            @PathVariable String rollNumber,
            @Valid @RequestBody VerifyMarksRequest request
    ) {
        String teacherEmail = getEmail(principal);
        List<SubjectMarkResponse> marks = teacherService.verifyStudentMarks(teacherEmail, rollNumber, request);
        return ResponseEntity.ok(ApiResponse.success(marks, "Student marks officially verified successfully"));
    }

    private String getEmail(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Principal cannot be null");
        }
        return principal.getName();
    }
}
