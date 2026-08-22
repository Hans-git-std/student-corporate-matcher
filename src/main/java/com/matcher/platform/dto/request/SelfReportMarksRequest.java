package com.matcher.platform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request payload for student self-reporting marks across multiple subjects")
public class SelfReportMarksRequest {

    @NotEmpty(message = "At least one subject mark entry is required")
    @Valid
    @Schema(description = "List of subject marks scored out of 100")
    private List<SubjectMarkEntry> marks;

    public SelfReportMarksRequest() {
    }

    public SelfReportMarksRequest(List<SubjectMarkEntry> marks) {
        this.marks = marks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<SubjectMarkEntry> marks;

        public Builder marks(List<SubjectMarkEntry> marks) {
            this.marks = marks;
            return this;
        }

        public SelfReportMarksRequest build() {
            return new SelfReportMarksRequest(marks);
        }
    }

    public List<SubjectMarkEntry> getMarks() {
        return marks;
    }

    public void setMarks(List<SubjectMarkEntry> marks) {
        this.marks = marks;
    }
}
