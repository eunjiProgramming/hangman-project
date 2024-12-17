package com.estelle.hangman.repository;

import com.estelle.hangman.domain.TeacherCourseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeacherCourseAssignmentRepository extends JpaRepository<TeacherCourseAssignment, Long> {

    boolean existsByTeacherIdAndCourseId(Long teacherId, Long courseId);

    @Modifying
    @Query("DELETE FROM TeacherCourseAssignment t WHERE t.teacher.id = :teacherId AND t.course.id = :courseId")
    void deleteByTeacherIdAndCourseId(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);

    List<TeacherCourseAssignment> findAllByTeacherId(Long teacherId);

    List<TeacherCourseAssignment> findAllByCourseId(Long courseId);

    @Modifying
    @Query("DELETE FROM TeacherCourseAssignment t WHERE t.course.id = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);
}