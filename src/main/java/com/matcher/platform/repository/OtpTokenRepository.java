package com.matcher.platform.repository;

import com.matcher.platform.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(String email);

    long countByEmailAndCreatedAtAfter(String email, java.time.Instant timestamp);

    java.util.List<OtpToken> findByIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(java.time.Instant now);

    java.util.List<OtpToken> findTop50ByOrderByCreatedAtDesc();

    void deleteByExpiresAtBefore(java.time.Instant now);
}
