package com.estelle.hangman.repository;

import com.estelle.hangman.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // 같은 이름의 반이 있는지 확인하는 메소드
    boolean existsByName(String name);

    // 특정 선생님이 담당하는 모든 반을 찾는 메소드
    @Query("SELECT c FROM Course c JOIN TeacherCourseAssignment t ON c.id = t.course.id WHERE t.teacher.id = :teacherId")
    List<Course> findByTeacherId(@Param("teacherId") Long teacherId);

    // 특정 선생님이 특정 반을 담당하는지 확인하는 메소드
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TeacherCourseAssignment t WHERE t.course.id = :courseId AND t.teacher.id = :teacherId")
    boolean existsByIdAndTeacherId(@Param("courseId") Long courseId, @Param("teacherId") Long teacherId);
}