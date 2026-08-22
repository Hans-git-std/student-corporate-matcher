package com.matcher.platform.entity;

import com.matcher.platform.entity.enums.SkillProficiency;
import jakarta.persistence.*;

@Entity
@Table(name = "criteria_required_skills", indexes = {
        @Index(name = "idx_crit_skill_crit_id", columnList = "criteria_id"),
        @Index(name = "idx_crit_skill_skill_id", columnList = "skill_id")
})
public class CriteriaRequiredSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criteria_id", nullable = false)
    private HiringCriteria criteria;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SkillProficiency minProficiency = SkillProficiency.BEGINNER;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = true;

    @Column(nullable = false)
    private Double weightage = 1.0;

    public CriteriaRequiredSkill() {
    }

    public CriteriaRequiredSkill(HiringCriteria criteria, Skill skill, SkillProficiency minProficiency, Boolean isMandatory, Double weightage) {
        this.criteria = criteria;
        this.skill = skill;
        this.minProficiency = minProficiency != null ? minProficiency : SkillProficiency.BEGINNER;
        this.isMandatory = isMandatory != null ? isMandatory : true;
        this.weightage = weightage != null ? weightage : 1.0;
    }

    public CriteriaRequiredSkill(Long id, HiringCriteria criteria, Skill skill, SkillProficiency minProficiency, Boolean isMandatory, Double weightage) {
        this.id = id;
        this.criteria = criteria;
        this.skill = skill;
        this.minProficiency = minProficiency != null ? minProficiency : SkillProficiency.BEGINNER;
        this.isMandatory = isMandatory != null ? isMandatory : true;
        this.weightage = weightage != null ? weightage : 1.0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private HiringCriteria criteria;
        private Skill skill;
        private SkillProficiency minProficiency = SkillProficiency.BEGINNER;
        private Boolean isMandatory = true;
        private Double weightage = 1.0;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder criteria(HiringCriteria criteria) {
            this.criteria = criteria;
            return this;
        }

        public Builder skill(Skill skill) {
            this.skill = skill;
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

        public CriteriaRequiredSkill build() {
            return new CriteriaRequiredSkill(id, criteria, skill, minProficiency, isMandatory, weightage);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HiringCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(HiringCriteria criteria) {
        this.criteria = criteria;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
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
