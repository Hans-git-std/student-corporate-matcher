package com.matcher.platform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Teacher official mark entry / verification record")
public class TeacherMarkVerificationEntry {

    @NotBlank(message = "Subject name is mandatory")
    @Size(min = 2, max = 100, message = "Subject name must be between 2 and 100 characters")
    @Schema(example = "Data Structures & Algorithms")
    private String subjectName;

    @NotNull(message = "Official verified marks are mandatory")
    @DecimalMin(value = "0.0", inclusive = true, message = "Marks cannot be less than 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Marks cannot exceed 100")
    @Schema(example = "92.0", description = "Official marks verified out of 100")
    private Double verifiedMarks;

    @Schema(example = "Semester 4")
    private String semester;

    @Schema(example = "Verified against Department Examination Records 2026", description = "Teacher remarks/notes")
    private String remarks;

    public TeacherMarkVerificationEntry() {
    }

    public TeacherMarkVerificationEntry(String subjectName, Double verifiedMarks, String semester, String remarks) {
        this.subjectName = subjectName;
        this.verifiedMarks = verifiedMarks;
        this.semester = semester;
        this.remarks = remarks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String subjectName;
        private Double verifiedMarks;
        private String semester;
        private String remarks;

        public Builder subjectName(String subjectName) {
            this.subjectName = subjectName;
            return this;
        }

        public Builder verifiedMarks(Double verifiedMarks) {
            this.verifiedMarks = verifiedMarks;
            return this;
        }

        public Builder semester(String semester) {
            this.semester = semester;
            return this;
        }

        public Builder remarks(String remarks) {
            this.remarks = remarks;
            return this;
        }

        public TeacherMarkVerificationEntry build() {
            return new TeacherMarkVerificationEntry(subjectName, verifiedMarks, semester, remarks);
        }
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Double getVerifiedMarks() {
        return verifiedMarks;
    }

    public void setVerifiedMarks(Double verifiedMarks) {
        this.verifiedMarks = verifiedMarks;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
