package com.matcher.platform.repository;

import com.matcher.platform.entity.TeacherProfile;
import com.matcher.platform.entity.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {

    Optional<TeacherProfile> findByUserId(Long userId);

    @Query("SELECT tp FROM TeacherProfile tp JOIN tp.user u WHERE u.email = :email")
    Optional<TeacherProfile> findByUserEmail(@Param("email") String email);

    Optional<TeacherProfile> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    List<TeacherProfile> findByApprovalStatus(ApprovalStatus approvalStatus);

    long countByApprovalStatus(ApprovalStatus approvalStatus);

    @Query("SELECT DISTINCT tp FROM TeacherProfile tp LEFT JOIN FETCH tp.assignedSubjects WHERE tp.id = :id")
    Optional<TeacherProfile> findWithSubjectsById(@Param("id") Long id);

    @Query("SELECT DISTINCT tp FROM TeacherProfile tp LEFT JOIN FETCH tp.assignedSubjects JOIN tp.user u WHERE u.email = :email")
    Optional<TeacherProfile> findWithSubjectsByEmail(@Param("email") String email);
}
