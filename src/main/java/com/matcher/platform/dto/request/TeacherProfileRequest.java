package com.matcher.platform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request payload to create or update teacher profile")
public class TeacherProfileRequest {

    @NotBlank(message = "Full name is mandatory")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(example = "Dr. Alan Turing")
    private String fullName;

    @NotBlank(message = "Employee ID is mandatory")
    @Schema(example = "EMP-FAC-1002")
    private String employeeId;

    @NotBlank(message = "Department is mandatory")
    @Schema(example = "Computer Science & Engineering")
    private String department;

    @Pattern(regexp = "^(\\+?[0-9]{10,15})?$", message = "Phone number must be valid 10-15 digits")
    @Schema(example = "+1987654321")
    private String phoneNumber;

    @Schema(description = "List of assigned subjects taught or verified by this teacher")
    private List<String> assignedSubjects;

    public TeacherProfileRequest() {
    }

    public TeacherProfileRequest(String fullName, String employeeId, String department, String phoneNumber, List<String> assignedSubjects) {
        this.fullName = fullName;
        this.employeeId = employeeId;
        this.department = department;
        this.phoneNumber = phoneNumber;
        this.assignedSubjects = assignedSubjects;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fullName;
        private String employeeId;
        private String department;
        private String phoneNumber;
        private List<String> assignedSubjects;

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder assignedSubjects(List<String> assignedSubjects) {
            this.assignedSubjects = assignedSubjects;
            return this;
        }

        public TeacherProfileRequest build() {
            return new TeacherProfileRequest(fullName, employeeId, department, phoneNumber, assignedSubjects);
        }
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<String> getAssignedSubjects() {
        return assignedSubjects;
    }

    public void setAssignedSubjects(List<String> assignedSubjects) {
        this.assignedSubjects = assignedSubjects;
    }
}
