package com.matcher.platform.controller;

import com.matcher.platform.dto.common.ApiResponse;
import com.matcher.platform.dto.request.SelfReportMarksRequest;
import com.matcher.platform.dto.request.StudentProfileRequest;
import com.matcher.platform.dto.request.StudentSkillRequest;
import com.matcher.platform.dto.response.CompanyMatchResponse;
import com.matcher.platform.dto.response.StudentProfileResponse;
import com.matcher.platform.dto.response.SubjectMarkResponse;
import com.matcher.platform.service.MatchingEngineService;
import com.matcher.platform.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "2. Student Management & Matching", description = "Student profile CRUD, self-reported marks, skills, and intelligent matching engine")
@SecurityRequirement(name = "BearerAuth")
public class StudentController {

    private final StudentService studentService;
    private final MatchingEngineService matchingEngineService;

    public StudentController(StudentService studentService, MatchingEngineService matchingEngineService) {
        this.studentService = studentService;
        this.matchingEngineService = matchingEngineService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get Student Profile", description = "Retrieves profile, skills, self-reported marks, and verification statuses for the authenticated student.")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getProfile(Principal principal) {
        String email = getEmail(principal);
        StudentProfileResponse response = studentService.getProfile(email);
        return ResponseEntity.ok(ApiResponse.success(response, "Student profile retrieved successfully"));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Update Student Profile", description = "Updates bio, contact details, address, and portfolio links for the authenticated student.")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> updateProfile(
            Principal principal,
            @Valid @RequestBody StudentProfileRequest request
    ) {
        String email = getEmail(principal);
        StudentProfileResponse response = studentService.updateOrCreateProfile(email, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Student profile updated successfully"));
    }

    @PostMapping("/marks/self-report")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Self-Report Academic Marks", description = "Allows students to submit self-reported marks (0-100) per subject. Marks remain in 'Pending Verification' state until confirmed by a teacher.")
    public ResponseEntity<ApiResponse<List<SubjectMarkResponse>>> selfReportMarks(
            Principal principal,
            @Valid @RequestBody SelfReportMarksRequest request
    ) {
        String email = getEmail(principal);
        List<SubjectMarkResponse> reportedMarks = studentService.selfReportMarks(email, request);
        return ResponseEntity.ok(ApiResponse.success(reportedMarks, "Self-reported marks submitted successfully"));
    }

    @GetMapping("/marks")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "View Academic Marks", description = "Fetches complete academic mark sheet with verification flags and teacher audit info.")
    public ResponseEntity<ApiResponse<List<SubjectMarkResponse>>> getAcademicMarks(Principal principal) {
        String email = getEmail(principal);
        List<SubjectMarkResponse> marks = studentService.getAcademicMarks(email);
        return ResponseEntity.ok(ApiResponse.success(marks, "Academic marks retrieved successfully"));
    }

    @PostMapping("/skills")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Add or Update Skill", description = "Adds a technical skill and proficiency level to the student profile.")
    public ResponseEntity<ApiResponse<String>> addSkill(
            Principal principal,
            @Valid @RequestBody StudentSkillRequest request
    ) {
        String email = getEmail(principal);
        studentService.addOrUpdateSkill(email, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Skill " + request.getSkillName() + " (" + request.getProficiency() + ") recorded",
                "Skill updated successfully"
        ));
    }

    @DeleteMapping("/skills/{skillName}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Delete Skill", description = "Removes a skill from student profile.")
    public ResponseEntity<ApiResponse<String>> deleteSkill(
            Principal principal,
            @PathVariable String skillName
    ) {
        String email = getEmail(principal);
        studentService.deleteSkill(email, skillName);
        return ResponseEntity.ok(ApiResponse.success("Skill " + skillName + " removed", "Skill deleted successfully"));
    }

    @GetMapping("/matches")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
            summary = "Get Company Matches",
            description = "Calculates ranked company matches. Applies Strict filtering first; if fewer than 3 companies match, falls back to Relaxed Weighted score with subject gap feedback (e.g., 'A 10.0 score more is required in Data Structures'). Marks pending verification are tagged with 'Verification by Teacher is Required'."
    )
    public ResponseEntity<ApiResponse<List<CompanyMatchResponse>>> getRecommendedMatches(Principal principal) {
        String email = getEmail(principal);
        List<CompanyMatchResponse> matches = matchingEngineService.calculateMatchesForStudent(email);
        return ResponseEntity.ok(ApiResponse.success(matches, "Company matches evaluated successfully"));
    }

    private String getEmail(Principal principal) {
        return principal != null ? principal.getName() : "student@university.edu";
    }
}
