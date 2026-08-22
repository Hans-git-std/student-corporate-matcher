package com.matcher.platform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details about academic score gap in a particular subject")
public class SubjectGapDetail {

    @Schema(example = "Data Structures & Algorithms")
    private String subjectName;

    @Schema(example = "65.0", description = "Student's current score")
    private Double currentScore;

    @Schema(example = "75.0", description = "Company's required cutoff mark")
    private Double requiredScore;

    @Schema(example = "10.0", description = "Difference required to meet cutoff")
    private Double scoreDeficit;

    @Schema(example = "A 10.0 score more is required in Data Structures & Algorithms", description = "User-facing gap feedback")
    private String gapRemark;

    public SubjectGapDetail() {
    }

    public SubjectGapDetail(String subjectName, Double currentScore, Double requiredScore, Double scoreDeficit, String gapRemark) {
        this.subjectName = subjectName;
        this.currentScore = currentScore;
        this.requiredScore = requiredScore;
        this.scoreDeficit = scoreDeficit;
        this.gapRemark = gapRemark;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String subjectName;
        private Double currentScore;
        private Double requiredScore;
        private Double scoreDeficit;
        private String gapRemark;

        public Builder subjectName(String subjectName) {
            this.subjectName = subjectName;
            return this;
        }

        public Builder currentScore(Double currentScore) {
            this.currentScore = currentScore;
            return this;
        }

        public Builder requiredScore(Double requiredScore) {
            this.requiredScore = requiredScore;
            return this;
        }

        public Builder scoreDeficit(Double scoreDeficit) {
            this.scoreDeficit = scoreDeficit;
            return this;
        }

        public Builder gapRemark(String gapRemark) {
            this.gapRemark = gapRemark;
            return this;
        }

        public SubjectGapDetail build() {
            return new SubjectGapDetail(subjectName, currentScore, requiredScore, scoreDeficit, gapRemark);
        }
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Double getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(Double currentScore) {
        this.currentScore = currentScore;
    }

    public Double getRequiredScore() {
        return requiredScore;
    }

    public void setRequiredScore(Double requiredScore) {
        this.requiredScore = requiredScore;
    }

    public Double getScoreDeficit() {
        return scoreDeficit;
    }

    public void setScoreDeficit(Double scoreDeficit) {
        this.scoreDeficit = scoreDeficit;
    }

    public String getGapRemark() {
        return gapRemark;
    }

    public void setGapRemark(String gapRemark) {
        this.gapRemark = gapRemark;
    }
}
