package com.matcher.platform.controller;

import com.matcher.platform.dto.common.ApiResponse;
import com.matcher.platform.dto.request.CompanyProfileRequest;
import com.matcher.platform.dto.request.CompanyRegisterRequest;
import com.matcher.platform.dto.request.HiringCriteriaRequest;
import com.matcher.platform.dto.response.CompanyProfileResponse;
import com.matcher.platform.dto.response.CompanyPublicResponse;
import com.matcher.platform.dto.response.HiringCriteriaResponse;
import com.matcher.platform.service.CompanyService;
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
@RequestMapping("/api/v1/companies")
@Tag(name = "4. Company & Hiring Criteria", description = "Public company directory, self-registration, profile management, and hiring criteria definition")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/public")
    @Operation(summary = "Browse Public Companies", description = "Publicly viewable directory of companies. Unverified companies are marked with badge 'Not Verified'.")
    public ResponseEntity<ApiResponse<List<CompanyPublicResponse>>> getPublicCompanies() {
        List<CompanyPublicResponse> companies = companyService.getPublicCompanies();
        return ResponseEntity.ok(ApiResponse.success(companies, "Public companies retrieved successfully"));
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Get Public Company Details", description = "Retrieves public profile, verification badge, and active job criteria for a specific company.")
    public ResponseEntity<ApiResponse<CompanyPublicResponse>> getPublicCompanyById(@PathVariable Long id) {
        CompanyPublicResponse company = companyService.getPublicCompanyById(id);
        return ResponseEntity.ok(ApiResponse.success(company, "Company details retrieved successfully"));
    }

    @PostMapping("/register")
    @Operation(summary = "Company Self-Registration", description = "Allows companies to register. Status is initialized as 'NOT_VERIFIED' with badge 'Not Verified' until reviewed by an Admin.")
    public ResponseEntity<ApiResponse<CompanyProfileResponse>> registerCompany(@Valid @RequestBody CompanyRegisterRequest request) {
        CompanyProfileResponse response = companyService.registerCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                response,
                "Company registered successfully. Status is 'Not Verified' pending administrative review."
        ));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('COMPANY')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get Company Profile", description = "Retrieves authenticated company's profile and active criteria.")
    public ResponseEntity<ApiResponse<CompanyProfileResponse>> getProfile(Principal principal) {
        String email = getEmail(principal);
        CompanyProfileResponse response = companyService.getProfile(email);
        return ResponseEntity.ok(ApiResponse.success(response, "Company profile retrieved successfully"));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('COMPANY')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update Company Profile", description = "Updates company information, description, location, and logo.")
    public ResponseEntity<ApiResponse<CompanyProfileResponse>> updateProfile(
            Principal principal,
            @Valid @RequestBody CompanyProfileRequest request
    ) {
        String email = getEmail(principal);
        CompanyProfileResponse response = companyService.updateProfile(email, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Company profile updated successfully"));
    }

    @PostMapping("/criteria")
    @PreAuthorize("hasRole('COMPANY')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Define Hiring Criteria & Cutoffs", description = "Creates or updates hiring criteria including mandatory/preferred skills and subject cutoff marks.")
    public ResponseEntity<ApiResponse<HiringCriteriaResponse>> defineHiringCriteria(
            Principal principal,
            @Valid @RequestBody HiringCriteriaRequest request
    ) {
        String email = getEmail(principal);
        HiringCriteriaResponse response = companyService.defineHiringCriteria(email, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Hiring criteria configured successfully"));
    }

    @GetMapping("/criteria")
    @PreAuthorize("hasRole('COMPANY')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "List Active Hiring Criteria", description = "Fetches all active hiring criteria and cutoffs defined by the authenticated company.")
    public ResponseEntity<ApiResponse<List<HiringCriteriaResponse>>> getHiringCriteria(Principal principal) {
        String email = getEmail(principal);
        List<HiringCriteriaResponse> list = companyService.getHiringCriteria(email);
        return ResponseEntity.ok(ApiResponse.success(list, "Hiring criteria retrieved successfully"));
    }

    @PutMapping("/criteria/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update Hiring Criteria", description = "Updates an existing hiring criteria by ID for the authenticated company.")
    public ResponseEntity<ApiResponse<HiringCriteriaResponse>> updateHiringCriteria(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody HiringCriteriaRequest request
    ) {
        String email = getEmail(principal);
        HiringCriteriaResponse response = companyService.updateHiringCriteria(email, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Hiring criteria updated successfully"));
    }

    @DeleteMapping("/criteria/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Delete Hiring Criteria", description = "Removes a specific hiring criteria by ID.")
    public ResponseEntity<ApiResponse<String>> deleteHiringCriteria(
            Principal principal,
            @PathVariable Long id
    ) {
        String email = getEmail(principal);
        companyService.deleteHiringCriteria(email, id);
        return ResponseEntity.ok(ApiResponse.success("Hiring criteria ID " + id + " deleted successfully", "Criteria deleted successfully"));
    }

    private String getEmail(Principal principal) {
        return principal != null ? principal.getName() : "recruiting@acmetech.com";
    }
}
