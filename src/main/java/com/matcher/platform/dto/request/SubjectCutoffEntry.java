package com.matcher.platform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Criteria definition for a subject cutoff mark")
public class SubjectCutoffEntry {

    @NotBlank(message = "Subject name is mandatory")
    @Size(min = 2, max = 100, message = "Subject name must be between 2 and 100 characters")
    @Schema(example = "Data Structures & Algorithms")
    private String subjectName;

    @NotNull(message = "Minimum cutoff mark is mandatory")
    @DecimalMin(value = "0.0", inclusive = true, message = "Cutoff cannot be less than 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Cutoff cannot exceed 100")
    @Schema(example = "75.0", description = "Minimum required mark out of 100")
    private Double minMarksCutoff;

    @Schema(example = "true", description = "Whether this subject cutoff is strictly required")
    private Boolean isMandatory = true;

    public SubjectCutoffEntry() {
    }

    public SubjectCutoffEntry(String subjectName, Double minMarksCutoff, Boolean isMandatory) {
        this.subjectName = subjectName;
        this.minMarksCutoff = minMarksCutoff;
        this.isMandatory = isMandatory != null ? isMandatory : true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String subjectName;
        private Double minMarksCutoff;
        private Boolean isMandatory = true;

        public Builder subjectName(String subjectName) {
            this.subjectName = subjectName;
            return this;
        }

        public Builder minMarksCutoff(Double minMarksCutoff) {
            this.minMarksCutoff = minMarksCutoff;
            return this;
        }

        public Builder isMandatory(Boolean isMandatory) {
            this.isMandatory = isMandatory;
            return this;
        }

        public SubjectCutoffEntry build() {
            return new SubjectCutoffEntry(subjectName, minMarksCutoff, isMandatory);
        }
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Double getMinMarksCutoff() {
        return minMarksCutoff;
    }

    public void setMinMarksCutoff(Double minMarksCutoff) {
        this.minMarksCutoff = minMarksCutoff;
    }

    public Boolean getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(Boolean isMandatory) {
        this.isMandatory = isMandatory;
    }
}
