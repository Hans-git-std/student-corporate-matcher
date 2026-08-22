package com.matcher.platform.repository;

import com.matcher.platform.entity.StudentSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentSkillRepository extends JpaRepository<StudentSkill, Long> {

    List<StudentSkill> findByStudentId(Long studentId);

    @Query("SELECT ss FROM StudentSkill ss JOIN ss.skill s WHERE ss.student.id = :studentId AND LOWER(s.name) = LOWER(:skillName)")
    Optional<StudentSkill> findByStudentIdAndSkillName(@Param("studentId") Long studentId, @Param("skillName") String skillName);

    @Modifying
    @Query("DELETE FROM StudentSkill ss WHERE ss.student.id = :studentId AND ss.skill.id IN (SELECT s.id FROM Skill s WHERE LOWER(s.name) = LOWER(:skillName))")
    void deleteByStudentIdAndSkillName(@Param("studentId") Long studentId, @Param("skillName") String skillName);
}
