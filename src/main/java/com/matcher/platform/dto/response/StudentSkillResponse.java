package com.matcher.platform.dto.response;

import com.matcher.platform.entity.enums.SkillProficiency;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Skill entry associated with student")
public class StudentSkillResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Java")
    private String skillName;

    @Schema(example = "ADVANCED")
    private SkillProficiency proficiency;

    @Schema(example = "2.5")
    private Double yearsOfExperience;

    public StudentSkillResponse() {
    }

    public StudentSkillResponse(Long id, String skillName, SkillProficiency proficiency, Double yearsOfExperience) {
        this.id = id;
        this.skillName = skillName;
        this.proficiency = proficiency;
        this.yearsOfExperience = yearsOfExperience;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String skillName;
        private SkillProficiency proficiency;
        private Double yearsOfExperience;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

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

        public StudentSkillResponse build() {
            return new StudentSkillResponse(id, skillName, proficiency, yearsOfExperience);
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
