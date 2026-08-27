package com.matcher.platform.repository;

import com.matcher.platform.entity.StudentAcademicRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAcademicRecordRepository extends JpaRepository<StudentAcademicRecord, Long> {

    List<StudentAcademicRecord> findByStudentId(Long studentId);

    @Query("SELECT r FROM StudentAcademicRecord r WHERE r.student.rollNumber = :rollNumber")
    List<StudentAcademicRecord> findByStudentRollNumber(@Param("rollNumber") String rollNumber);

    @Query("SELECT r FROM StudentAcademicRecord r WHERE r.isVerified = false OR r.isVerified IS NULL")
    List<StudentAcademicRecord> findByIsVerifiedFalse();

    @Query("SELECT COUNT(r) FROM StudentAcademicRecord r WHERE r.isVerified = false OR r.isVerified IS NULL")
    long countByIsVerifiedFalse();
}
