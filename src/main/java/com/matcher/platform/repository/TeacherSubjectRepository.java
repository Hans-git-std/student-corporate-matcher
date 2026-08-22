package com.matcher.platform.repository;

import com.matcher.platform.entity.TeacherSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, Long> {

    List<TeacherSubject> findByTeacherId(Long teacherId);

    @Modifying
    @Query("DELETE FROM TeacherSubject ts WHERE ts.teacher.id = :teacherId")
    void deleteByTeacherId(@Param("teacherId") Long teacherId);
}
