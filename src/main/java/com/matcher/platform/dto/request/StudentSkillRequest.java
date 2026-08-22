package com.matcher.platform.dto.request;

import com.matcher.platform.entity.enums.SkillProficiency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload to add or update a student skill")
public class StudentSkillRequest {

    @NotBlank(message = "Skill name is mandatory")
    @Size(min = 1, max = 50, message = "Skill name must be between 1 and 50 characters")
    @Schema(example = "Java", description = "Standardized skill name")
    private String skillName;

    @NotNull(message = "Proficiency level is mandatory")
    @Schema(example = "ADVANCED", description = "Proficiency: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT")
    private SkillProficiency proficiency;

    @DecimalMin(value = "0.0", message = "Years of experience cannot be negative")
    @Schema(example = "2.5", description = "Years of practical or project experience")
    private Double yearsOfExperience;

    public StudentSkillRequest() {
    }

    public StudentSkillRequest(String skillName, SkillProficiency proficiency, Double yearsOfExperience) {
        this.skillName = skillName;
        this.proficiency = proficiency;
        this.yearsOfExperience = yearsOfExperience;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String skillName;
        private SkillProficiency proficiency;
        private Double yearsOfExperience;

        public Builder skillName(String skillName) {
            this.skillName = skillName;
            return this;
        }

        public Builder proficiency(SkillProficiency proficiency) {
            this.proficiency = proficiency;
            return this;
        }

        public Builder yearsOfExperience(Double yearsOfExperience) {
            this.yearsOfExperience = yearsOfExperience;
            return this;
        }

        public StudentSkillRequest build() {
            return new StudentSkillRequest(skillName, proficiency, yearsOfExperience);
        }
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public SkillProficiency getProficiency() {
        return proficiency;
    }

    public void setProficiency(SkillProficiency proficiency) {
        this.proficiency = proficiency;
    }

    public Double getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Double yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }
}
