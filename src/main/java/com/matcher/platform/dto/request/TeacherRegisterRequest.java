package com.matcher.platform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class TeacherRegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Employee ID is required")
    @Size(max = 30, message = "Employee ID cannot exceed 30 characters")
    private String employeeId;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    private String designation;

    private String phoneNumber;

    private List<String> assignedSubjects;

    public TeacherRegisterRequest() {
    }

    public TeacherRegisterRequest(String fullName, String email, String employeeId, String department, String designation, String phoneNumber, List<String> assignedSubjects) {
        this.fullName = fullName;
        this.email = email;
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
        this.phoneNumber = phoneNumber;
        this.assignedSubjects = assignedSubjects;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fullName;
        private String email;
        private String employeeId;
        private String department;
        private String designation;
        private String phoneNumber;
        private List<String> assignedSubjects;

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
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

        public Builder designation(String designation) {
            this.designation = designation;
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

        public TeacherRegisterRequest build() {
            return new TeacherRegisterRequest(fullName, email, employeeId, department, designation, phoneNumber, assignedSubjects);
        }
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
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
