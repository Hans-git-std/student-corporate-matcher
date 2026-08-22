package com.matcher.platform.repository;

import com.matcher.platform.entity.CriteriaRequiredSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CriteriaRequiredSkillRepository extends JpaRepository<CriteriaRequiredSkill, Long> {
    List<CriteriaRequiredSkill> findByCriteriaId(Long criteriaId);
}
