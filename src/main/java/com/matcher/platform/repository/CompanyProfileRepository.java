package com.matcher.platform.repository;

import com.matcher.platform.entity.CompanyProfile;
import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {

    Optional<CompanyProfile> findByUserId(Long userId);

    @Query("SELECT cp FROM CompanyProfile cp JOIN cp.user u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<CompanyProfile> findByUserEmail(@Param("email") String email);

    Optional<CompanyProfile> findByCompanyNameIgnoreCase(String companyName);

    boolean existsByCompanyNameIgnoreCase(String companyName);

    List<CompanyProfile> findByVerificationStatus(CompanyVerificationStatus verificationStatus);

    long countByVerificationStatus(CompanyVerificationStatus verificationStatus);

    @Query("SELECT cp FROM CompanyProfile cp WHERE cp.id = :id")
    Optional<CompanyProfile> findWithCriteriaById(@Param("id") Long id);

    @Query("SELECT cp FROM CompanyProfile cp JOIN cp.user u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<CompanyProfile> findWithCriteriaByEmail(@Param("email") String email);

    @Query("SELECT cp FROM CompanyProfile cp")
    List<CompanyProfile> findAllWithCriteria();
}
