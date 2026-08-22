package com.matcher.platform.repository;

import com.matcher.platform.entity.CriteriaSubjectCutoff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CriteriaSubjectCutoffRepository extends JpaRepository<CriteriaSubjectCutoff, Long> {
    List<CriteriaSubjectCutoff> findByCriteriaId(Long criteriaId);
}
