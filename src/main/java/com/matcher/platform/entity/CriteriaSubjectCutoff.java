package com.matcher.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "criteria_subject_cutoffs", indexes = {
        @Index(name = "idx_crit_cutoff_crit_id", columnList = "criteria_id"),
        @Index(name = "idx_crit_cutoff_sub_name", columnList = "subject_name")
})
public class CriteriaSubjectCutoff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criteria_id", nullable = false)
    private HiringCriteria criteria;

    @Column(name = "subject_name", nullable = false, length = 100)
    private String subjectName;

    @Column(name = "min_marks_cutoff", nullable = false)
    private Double minMarksCutoff;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = true;

    public CriteriaSubjectCutoff() {
    }

    public CriteriaSubjectCutoff(HiringCriteria criteria, String subjectName, Double minMarksCutoff, Boolean isMandatory) {
        this.criteria = criteria;
        this.subjectName = subjectName;
        this.minMarksCutoff = minMarksCutoff;
        this.isMandatory = isMandatory != null ? isMandatory : true;
    }

    public CriteriaSubjectCutoff(Long id, HiringCriteria criteria, String subjectName, Double minMarksCutoff, Boolean isMandatory) {
        this.id = id;
        this.criteria = criteria;
        this.subjectName = subjectName;
        this.minMarksCutoff = minMarksCutoff;
        this.isMandatory = isMandatory != null ? isMandatory : true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private HiringCriteria criteria;
        private String subjectName;
        private Double minMarksCutoff;
        private Boolean isMandatory = true;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder criteria(HiringCriteria criteria) {
            this.criteria = criteria;
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

        public CriteriaSubjectCutoff build() {
            return new CriteriaSubjectCutoff(id, criteria, subjectName, minMarksCutoff, isMandatory);
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
