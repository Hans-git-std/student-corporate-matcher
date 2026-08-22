package com.matcher.platform.entity;

import com.matcher.platform.entity.enums.SkillProficiency;
import jakarta.persistence.*;

@Entity
@Table(name = "student_skills", indexes = {
        @Index(name = "idx_stuskill_student_id", columnList = "student_id"),
        @Index(name = "idx_stuskill_skill_id", columnList = "skill_id")
})
public class StudentSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SkillProficiency proficiency;

    @Column(name = "years_of_experience")
    private Double yearsOfExperience;

    public StudentSkill() {
    }

    public StudentSkill(StudentProfile student, Skill skill, SkillProficiency proficiency, Double yearsOfExperience) {
        this.student = student;
        this.skill = skill;
        this.proficiency = proficiency;
        this.yearsOfExperience = yearsOfExperience;
    }

    public StudentSkill(Long id, StudentProfile student, Skill skill, SkillProficiency proficiency, Double yearsOfExperience) {
        this.id = id;
        this.student = student;
        this.skill = skill;
        this.proficiency = proficiency;
        this.yearsOfExperience = yearsOfExperience;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudentProfile getStudent() {
        return student;
    }

    public void setStudent(StudentProfile student) {
        this.student = student;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
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
