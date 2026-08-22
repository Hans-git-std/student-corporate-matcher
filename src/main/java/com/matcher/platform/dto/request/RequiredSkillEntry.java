package com.matcher.platform.dto.request;

import com.matcher.platform.entity.enums.SkillProficiency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Criteria definition for a required skill")
public class RequiredSkillEntry {

    @NotBlank(message = "Skill name is mandatory")
    @Size(min = 1, max = 50, message = "Skill name must be between 1 and 50 characters")
    @Schema(example = "Java")
    private String skillName;

    @NotNull(message = "Minimum proficiency is mandatory")
    @Schema(example = "INTERMEDIATE")
    private SkillProficiency minProficiency;

    @Schema(example = "true", description = "Whether this skill is strictly mandatory or preferred")
    private Boolean isMandatory = true;

    @DecimalMin(value = "0.0", message = "Weightage cannot be negative")
    @DecimalMax(value = "10.0", message = "Weightage cannot exceed 10")
    @Schema(example = "1.0", description = "Relative weight for score calculation in relaxed matching mode")
    private Double weightage = 1.0;

    public RequiredSkillEntry() {
    }

    public RequiredSkillEntry(String skillName, SkillProficiency minProficiency, Boolean isMandatory, Double weightage) {
        this.skillName = skillName;
        this.minProficiency = minProficiency;
        this.isMandatory = isMandatory != null ? isMandatory : true;
        this.weightage = weightage != null ? weightage : 1.0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String skillName;
        private SkillProficiency minProficiency;
        private Boolean isMandatory = true;
        private Double weightage = 1.0;

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

        public RequiredSkillEntry build() {
            return new RequiredSkillEntry(skillName, minProficiency, isMandatory, weightage);
        }
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
