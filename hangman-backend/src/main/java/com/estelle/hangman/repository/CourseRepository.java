package com.estelle.hangman.repository;

import com.estelle.hangman.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByName(String name);

    @Query("SELECT c FROM Course c JOIN TeacherCourseAssignment t ON c.id = t.course.id WHERE t.teacher.id = :teacherId")
    List<Course> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TeacherCourseAssignment t WHERE t.course.id = :courseId AND t.teacher.id = :teacherId")
    boolean existsByIdAndTeacherId(@Param("courseId") Long courseId, @Param("teacherId") Long teacherId);
}