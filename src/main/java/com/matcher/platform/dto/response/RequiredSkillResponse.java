package com.matcher.platform.dto.response;

import com.matcher.platform.entity.enums.SkillProficiency;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Required skill requirement details in hiring criteria")
public class RequiredSkillResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Java")
    private String skillName;

    @Schema(example = "INTERMEDIATE")
    private SkillProficiency minProficiency;

    @Schema(example = "true")
    private Boolean isMandatory;

    @Schema(example = "1.0")
    private Double weightage;

    public RequiredSkillResponse() {
    }

    public RequiredSkillResponse(Long id, String skillName, SkillProficiency minProficiency, Boolean isMandatory, Double weightage) {
        this.id = id;
        this.skillName = skillName;
        this.minProficiency = minProficiency;
        this.isMandatory = isMandatory;
        this.weightage = weightage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String skillName;
        private SkillProficiency minProficiency;
        private Boolean isMandatory;
        private Double weightage;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder skillName(String skillName) {
            this.skillName = skillName;
            return this;
        }

        public Builder minProficiency(SkillProficiency minProficiency) {
            this.minProficiency = minProficiency;
            return this;
        }

        public Builder isMandatory(Boolean isMandatory) {
            this.isMandatory = isMandatory;
            return this;
        }

        public Builder weightage(Double weightage) {
            this.weightage = weightage;
            return this;
        }

        public RequiredSkillResponse build() {
            return new RequiredSkillResponse(id, skillName, minProficiency, isMandatory, weightage);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public SkillProficiency getMinProficiency() {
        return minProficiency;
    }

    public void setMinProficiency(SkillProficiency minProficiency) {
        this.minProficiency = minProficiency;
    }

    public Boolean getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(Boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    public Double getWeightage() {
        return weightage;
    }

    public void setWeightage(Double weightage) {
        this.weightage = weightage;
    }
}
