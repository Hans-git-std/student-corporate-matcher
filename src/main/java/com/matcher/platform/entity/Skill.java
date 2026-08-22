package com.matcher.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "skills", indexes = {
        @Index(name = "idx_skill_name_unique", columnList = "name", unique = true)
})
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 50)
    private String category;

    public Skill() {
    }

    public Skill(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public Skill(String name, String category) {
        this.name = name != null ? name.trim() : null;
        this.category = category;
    }

    public Skill(Long id, String name, String category) {
        this.id = id;
        this.name = name != null ? name.trim() : null;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
