package com.matcher.platform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request payload for teacher adding or verifying official marks for a student")
public class VerifyMarksRequest {

    @NotEmpty(message = "At least one subject mark verification entry is required")
    @Valid
    @Schema(description = "List of verified subject mark entries")
    private List<TeacherMarkVerificationEntry> verifiedMarks;

    public VerifyMarksRequest() {
    }

    public VerifyMarksRequest(List<TeacherMarkVerificationEntry> verifiedMarks) {
        this.verifiedMarks = verifiedMarks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<TeacherMarkVerificationEntry> verifiedMarks;

        public Builder verifiedMarks(List<TeacherMarkVerificationEntry> verifiedMarks) {
            this.verifiedMarks = verifiedMarks;
            return this;
        }

        public VerifyMarksRequest build() {
            return new VerifyMarksRequest(verifiedMarks);
        }
    }

    public List<TeacherMarkVerificationEntry> getVerifiedMarks() {
        return verifiedMarks;
    }

    public void setVerifiedMarks(List<TeacherMarkVerificationEntry> verifiedMarks) {
        this.verifiedMarks = verifiedMarks;
    }
}
