package com.estelle.hangman.repository;

import com.estelle.hangman.domain.TeacherCourseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeacherCourseAssignmentRepository extends JpaRepository<TeacherCourseAssignment, Long> {

    // 특정 교사-반 조합이 이미 존재하는지 확인
    boolean existsByTeacherIdAndCourseId(Long teacherId, Long courseId);

    // 특정 교사-반 매핑 삭제
    @Modifying
    @Query("DELETE FROM TeacherCourseAssignment t WHERE t.teacher.id = :teacherId AND t.course.id = :courseId")
    void deleteByTeacherIdAndCourseId(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);

    // 특정 교사의 모든 반 매핑 조회
    List<TeacherCourseAssignment> findAllByTeacherId(Long teacherId);

    // 특정 반의 모든 교사 매핑 조회
    List<TeacherCourseAssignment> findAllByCourseId(Long courseId);

    // 특정 반의 모든 교사 매핑 삭제 (반 삭제 시 사용)
    @Modifying
    @Query("DELETE FROM TeacherCourseAssignment t WHERE t.course.id = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);
}