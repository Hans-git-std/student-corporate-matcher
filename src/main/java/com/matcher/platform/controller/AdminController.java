package com.matcher.platform.controller;

import com.matcher.platform.dto.common.ApiResponse;
import com.matcher.platform.dto.request.CompanyProfileRequest;
import com.matcher.platform.dto.request.CompanyRegisterRequest;
import com.matcher.platform.dto.request.CompanyStatusUpdateRequest;
import com.matcher.platform.dto.request.CreateTeacherRequest;
import com.matcher.platform.dto.request.TeacherRejectRequest;
import com.matcher.platform.dto.response.AdminDashboardStatsResponse;
import com.matcher.platform.dto.response.CompanyProfileResponse;
import com.matcher.platform.dto.response.StudentProfileResponse;
import com.matcher.platform.dto.response.SystemDiagnosticsResponse;
import com.matcher.platform.dto.response.TeacherProfileResponse;
import com.matcher.platform.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN') and @securityGuard.isMasterAdmin(principal)")
@Tag(name = "5. Admin Operations & Diagnostic Control", description = "Overarching administrative management, teacher verification lifecycle, student/company CRUD, and server diagnostics")
@SecurityRequirement(name = "BearerAuth")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ==========================================
    // Teacher Approval Lifecycle & Management
    // ==========================================

    @GetMapping("/teachers/pending")
    @Operation(summary = "List Pending Teacher Applications", description = "Fetches all faculty self-registrations waiting for administrative approval.")
    public ResponseEntity<ApiResponse<List<TeacherProfileResponse>>> getPendingTeachers() {
        List<TeacherProfileResponse> pending = adminService.getPendingTeachers();
        return ResponseEntity.ok(ApiResponse.success(pending, "Pending teacher applications retrieved successfully"));
    }

    @PostMapping("/teachers/{id}/approve")
    @Operation(summary = "Approve Teacher Registration", description = "Approves a pending teacher account, granting full faculty marks-verification access.")
    public ResponseEntity<ApiResponse<TeacherProfileResponse>> approveTeacher(@PathVariable Long id) {
        TeacherProfileResponse approved = adminService.approveTeacher(id);
        return ResponseEntity.ok(ApiResponse.success(approved, "Faculty account approved successfully"));
    }

    @PostMapping("/teachers/{id}/reject")
    @Operation(summary = "Reject Teacher Registration", description = "Rejects a pending teacher account with an optional reason.")
    public ResponseEntity<ApiResponse<TeacherProfileResponse>> rejectTeacher(
            @PathVariable Long id,
            @RequestBody(required = false) TeacherRejectRequest request
    ) {
        TeacherRejectRequest req = request != null ? request : new TeacherRejectRequest("Application rejected by administrator.");
        TeacherProfileResponse rejected = adminService.rejectTeacher(id, req);
        return ResponseEntity.ok(ApiResponse.success(rejected, "Faculty account rejected"));
    }

    @GetMapping("/teachers")
    @Operation(summary = "List All Teachers (Admin)", description = "Fetches all registered faculty members and their approval status.")
    public ResponseEntity<ApiResponse<List<TeacherProfileResponse>>> getAllTeachers() {
        List<TeacherProfileResponse> teachers = adminService.getAllTeachers();
        return ResponseEntity.ok(ApiResponse.success(teachers, "All faculty records retrieved successfully"));
    }

    @GetMapping("/teachers/{id}")
    @Operation(summary = "Get Teacher by ID (Admin)", description = "Fetches full profile details for a specific faculty member.")
    public ResponseEntity<ApiResponse<TeacherProfileResponse>> getTeacherById(@PathVariable Long id) {
        TeacherProfileResponse teacher = adminService.getTeacherById(id);
        return ResponseEntity.ok(ApiResponse.success(teacher, "Faculty details retrieved successfully"));
    }

    @PostMapping("/teachers")
    @Operation(summary = "Provision Faculty Account Directly", description = "Directly creates an active and pre-approved teacher account.")
    public ResponseEntity<ApiResponse<TeacherProfileResponse>> createTeacher(@Valid @RequestBody CreateTeacherRequest request) {
        TeacherProfileResponse response = adminService.createTeacher(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response, "Teacher account provisioned successfully"));
    }

    @DeleteMapping("/teachers/{id}")
    @Operation(summary = "Delete Teacher (Admin)", description = "Removes a faculty profile and user account from the system.")
    public ResponseEntity<ApiResponse<String>> deleteTeacher(@PathVariable Long id) {
        adminService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponse.success("Faculty member ID " + id + " deleted", "Faculty deleted successfully"));
    }

    // ==========================================
    // Student Record Management
    // ==========================================

    @GetMapping("/students")
    @Operation(summary = "List All Students (Admin)", description = "Fetches all registered student profiles with academic and skill records.")
    public ResponseEntity<ApiResponse<List<StudentProfileResponse>>> getAllStudents() {
        List<StudentProfileResponse> students = adminService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success(students, "All student profiles retrieved successfully"));
    }

    @GetMapping("/students/{id}")
    @Operation(summary = "Get Student by ID (Admin)", description = "Fetches full profile, verified academic marks, and skills for a student.")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getStudentById(@PathVariable Long id) {
        StudentProfileResponse student = adminService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(student, "Student profile retrieved successfully"));
    }

    @DeleteMapping("/students/{id}")
    @Operation(summary = "Delete Student (Admin)", description = "Deletes a student profile, academic marks, skills, and account.")
    public ResponseEntity<ApiResponse<String>> deleteStudent(@PathVariable Long id) {
        adminService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Student profile ID " + id + " deleted", "Student deleted successfully"));
    }

    // ==========================================
    // Company Management
    // ==========================================

    @GetMapping("/companies")
    @Operation(summary = "List All Companies (Admin)", description = "Fetches all companies regardless of verification status.")
    public ResponseEntity<ApiResponse<List<CompanyProfileResponse>>> getAllCompanies() {
        List<CompanyProfileResponse> companies = adminService.getAllCompanies();
        return ResponseEntity.ok(ApiResponse.success(companies, "All companies retrieved successfully"));
    }

    @GetMapping("/companies/{id}")
    @Operation(summary = "Get Company by ID (Admin)", description = "Fetches details of a specific company.")
    public ResponseEntity<ApiResponse<CompanyProfileResponse>> getCompanyById(@PathVariable Long id) {
        CompanyProfileResponse company = adminService.getCompanyById(id);
        return ResponseEntity.ok(ApiResponse.success(company, "Company retrieved successfully"));
    }

    @PostMapping("/companies")
    @Operation(summary = "Create Company (Admin)", description = "Directly creates a pre-verified company record.")
    public ResponseEntity<ApiResponse<CompanyProfileResponse>> createCompany(@Valid @RequestBody CompanyRegisterRequest request) {
        CompanyProfileResponse response = adminService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response, "Company created and verified by admin"));
    }

    @PutMapping("/companies/{id}")
    @Operation(summary = "Update Company (Admin)", description = "Updates details of any company in the system.")
    public ResponseEntity<ApiResponse<CompanyProfileResponse>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyProfileRequest request
    ) {
        CompanyProfileResponse response = adminService.updateCompany(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Company updated successfully by admin"));
    }

    @DeleteMapping("/companies/{id}")
    @Operation(summary = "Delete Company (Admin)", description = "Removes a company and its associated criteria from the platform.")
    public ResponseEntity<ApiResponse<String>> deleteCompany(@PathVariable Long id) {
        adminService.deleteCompany(id);
        return ResponseEntity.ok(ApiResponse.success("Company ID " + id + " has been deleted", "Company deleted successfully"));
    }

    @PatchMapping("/companies/{id}/status")
    @Operation(summary = "Update Company Verification Status", description = "Approve ('VERIFIED'), revoke ('NOT_VERIFIED'), or reject ('REJECTED') a company registration.")
    public ResponseEntity<ApiResponse<CompanyProfileResponse>> updateVerificationStatus(
            @PathVariable Long id,
            @Valid @RequestBody CompanyStatusUpdateRequest request
    ) {
        CompanyProfileResponse response = adminService.updateVerificationStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Company status updated to " + request.getStatus()));
    }

    // ==========================================
    // System Diagnostics & Analytics
    // ==========================================

    @GetMapping("/stats")
    @Operation(summary = "Dashboard Statistics", description = "Provides platform-wide metrics including verification backlogs and match totals.")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getDashboardStats() {
        AdminDashboardStatsResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard statistics retrieved successfully"));
    }

    @GetMapping("/system/diagnostics")
    @Operation(summary = "Server & System Diagnostics", description = "Real-time JVM RAM metrics, daily SMTP dispatch count, and database stats.")
    public ResponseEntity<ApiResponse<SystemDiagnosticsResponse>> getSystemDiagnostics() {
        SystemDiagnosticsResponse diag = adminService.getSystemDiagnostics();
        return ResponseEntity.ok(ApiResponse.success(diag, "System diagnostics retrieved successfully"));
    }
}
