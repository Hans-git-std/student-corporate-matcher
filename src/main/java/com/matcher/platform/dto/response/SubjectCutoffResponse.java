package com.matcher.platform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Subject cutoff requirement in hiring criteria")
public class SubjectCutoffResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Data Structures & Algorithms")
    private String subjectName;

    @Schema(example = "75.0")
    private Double minMarksCutoff;

    @Schema(example = "true")
    private Boolean isMandatory;

    public SubjectCutoffResponse() {
    }

    public SubjectCutoffResponse(Long id, String subjectName, Double minMarksCutoff, Boolean isMandatory) {
        this.id = id;
        this.subjectName = subjectName;
        this.minMarksCutoff = minMarksCutoff;
        this.isMandatory = isMandatory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String subjectName;
        private Double minMarksCutoff;
        private Boolean isMandatory;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

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

        public SubjectCutoffResponse build() {
            return new SubjectCutoffResponse(id, subjectName, minMarksCutoff, isMandatory);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
