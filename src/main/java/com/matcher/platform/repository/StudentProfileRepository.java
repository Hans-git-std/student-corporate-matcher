package com.matcher.platform.repository;

import com.matcher.platform.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUserId(Long userId);

    @Query("SELECT sp FROM StudentProfile sp JOIN sp.user u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<StudentProfile> findByUserEmail(@Param("email") String email);

    Optional<StudentProfile> findByRollNumber(String rollNumber);

    boolean existsByRollNumber(String rollNumber);

    @Query("SELECT sp FROM StudentProfile sp")
    List<StudentProfile> findAllWithDetails();

    @Query("SELECT sp FROM StudentProfile sp WHERE sp.id = :id")
    Optional<StudentProfile> findWithDetailsById(@Param("id") Long id);

    @Query("SELECT sp FROM StudentProfile sp JOIN sp.user u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<StudentProfile> findWithDetailsByEmail(@Param("email") String email);

    @Query("SELECT sp FROM StudentProfile sp WHERE sp.rollNumber = :rollNumber")
    Optional<StudentProfile> findWithDetailsByRollNumber(@Param("rollNumber") String rollNumber);
}
