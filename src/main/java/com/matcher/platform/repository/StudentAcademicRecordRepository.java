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

    Optional<StudentAcademicRecord> findByStudentIdAndSubjectNameIgnoreCase(Long studentId, String subjectName);

    List<StudentAcademicRecord> findByIsVerifiedFalse();

    long countByIsVerifiedFalse();
}
