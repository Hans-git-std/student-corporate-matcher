package com.matcher.platform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Individual subject mark entry (scored out of 100)")
public class SubjectMarkEntry {

    @NotBlank(message = "Subject name is mandatory")
    @Size(min = 2, max = 100, message = "Subject name must be between 2 and 100 characters")
    @Schema(example = "Data Structures & Algorithms")
    private String subjectName;

    @NotNull(message = "Marks obtained is mandatory")
    @DecimalMin(value = "0.0", inclusive = true, message = "Marks cannot be less than 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Marks cannot exceed 100")
    @Schema(example = "88.5", description = "Marks obtained out of 100")
    private Double marksObtained;

    @Schema(example = "Semester 4", description = "Academic term/semester")
    private String semester;

    public SubjectMarkEntry() {
    }

    public SubjectMarkEntry(String subjectName, Double marksObtained, String semester) {
        this.subjectName = subjectName;
        this.marksObtained = marksObtained;
        this.semester = semester;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String subjectName;
        private Double marksObtained;
        private String semester;

        public Builder subjectName(String subjectName) {
            this.subjectName = subjectName;
            return this;
        }

        public Builder marksObtained(Double marksObtained) {
            this.marksObtained = marksObtained;
            return this;
        }

        public Builder semester(String semester) {
            this.semester = semester;
            return this;
        }

        public SubjectMarkEntry build() {
            return new SubjectMarkEntry(subjectName, marksObtained, semester);
        }
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(Double marksObtained) {
        this.marksObtained = marksObtained;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }
}
