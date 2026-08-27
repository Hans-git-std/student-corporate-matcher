package com.matcher.platform.repository;

import com.matcher.platform.entity.HiringCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HiringCriteriaRepository extends JpaRepository<HiringCriteria, Long> {

    List<HiringCriteria> findByCompanyId(Long companyId);

    List<HiringCriteria> findByCompanyIdAndIsActiveTrue(Long companyId);

    @Query("SELECT hc FROM HiringCriteria hc " +
           "JOIN FETCH hc.company cp " +
           "WHERE hc.isActive = true")
    List<HiringCriteria> findAllActiveWithDetails();

    @Query("SELECT hc FROM HiringCriteria hc " +
           "JOIN FETCH hc.company cp " +
           "WHERE hc.id = :id")
    Optional<HiringCriteria> findWithDetailsById(@Param("id") Long id);
}
