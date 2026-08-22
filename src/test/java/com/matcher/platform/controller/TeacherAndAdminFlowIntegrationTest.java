package com.matcher.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matcher.platform.dto.request.AdminOtpSendRequest;
import com.matcher.platform.dto.request.OtpSendRequest;
import com.matcher.platform.dto.request.TeacherRegisterRequest;
import com.matcher.platform.entity.enums.RoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TeacherAndAdminFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Admin 2-Step Login: Rejects invalid password")
    void testAdminOtpSendInvalidPassword() throws Exception {
        AdminOtpSendRequest req = new AdminOtpSendRequest("admin@studentmatcher.com", "WrongPassword!", false);

        mockMvc.perform(post("/api/v1/auth/admin/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Invalid master admin password")));
    }

    @Test
    @DisplayName("Admin 2-Step Login: Rejects unauthorized email")
    void testAdminOtpSendFakeAdmin() throws Exception {
        AdminOtpSendRequest req = new AdminOtpSendRequest("fakeadmin@hacker.com", "Admin@RootMaster2026!", false);

        mockMvc.perform(post("/api/v1/auth/admin/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security Defense: Blocking Admin login via public /otp/send")
    void testBlockAdminOnPublicOtpSend() throws Exception {
        OtpSendRequest req = new OtpSendRequest("admin@studentmatcher.com", RoleType.ROLE_ADMIN);

        mockMvc.perform(post("/api/v1/auth/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Administrator login requires 2-step verification")));
    }

    @Test
    @DisplayName("Teacher Lifecycle: Self-Registration followed by PENDING login rejection")
    void testTeacherSelfRegistrationAndPendingRejection() throws Exception {
        String uniqueEmpId = "EMP-" + System.currentTimeMillis();
        String teacherEmail = "prof." + System.currentTimeMillis() + "@faculty.edu";

        TeacherRegisterRequest regReq = TeacherRegisterRequest.builder()
                .fullName("Prof. Ada Lovelace")
                .email(teacherEmail)
                .employeeId(uniqueEmpId)
                .department("Computer Science")
                .assignedSubjects(List.of("Algorithms", "Operating Systems"))
                .build();

        // 1. Self-register
        mockMvc.perform(post("/api/v1/teachers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.employeeId").value(uniqueEmpId))
                .andExpect(jsonPath("$.data.approvalStatus").value("PENDING"));

        // 2. Attempt login while status is PENDING
        OtpSendRequest loginReq = new OtpSendRequest(teacherEmail, RoleType.ROLE_TEACHER);
        mockMvc.perform(post("/api/v1/auth/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("No further action, verification is in waiting"));
    }
}
