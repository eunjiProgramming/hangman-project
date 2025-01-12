package com.estelle.hangman.repository;

import com.estelle.hangman.domain.TeacherCourseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// TeacherCourseAssignment 엔티티를 관리하는 리포지토리입니다.
// 이 리포지토리는 선생님과 반 사이의 관계(매핑)를 다룹니다.
public interface TeacherCourseAssignmentRepository extends JpaRepository<TeacherCourseAssignment, Long> {

    // 특정 선생님이 특정 반에 이미 배정되어 있는지 확인하는 메소드입니다.
    // 예를 들어, "김선생님이 1-A반에 이미 배정되어 있나요?" 를 확인할 때 사용합니다.
    boolean existsByTeacherIdAndCourseId(Long teacherId, Long courseId);

    // 특정 선생님-반 매핑을 삭제하는 메소드입니다.
    // @Modifying 어노테이션은 데이터를 변경하는 쿼리임을 나타냅니다.
    // 예를 들어, "김선생님을 1-A반에서 제외시켜주세요" 라는 요청을 처리할 때 사용합니다.
    @Modifying
    @Query("DELETE FROM TeacherCourseAssignment t WHERE t.teacher.id = :teacherId AND t.course.id = :courseId")
    void deleteByTeacherIdAndCourseId(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);

    // 특정 선생님의 모든 반 매핑을 조회합니다.
    // 예를 들어, "김선생님이 담당하는 모든 반을 보여주세요" 라는 요청을 처리할 때 사용합니다.
    List<TeacherCourseAssignment> findAllByTeacherId(Long teacherId);

    // 특정 반의 모든 선생님 매핑을 조회합니다.
    // 예를 들어, "1-A반을 담당하는 모든 선생님을 보여주세요" 라는 요청을 처리할 때 사용합니다.
    List<TeacherCourseAssignment> findAllByCourseId(Long courseId);

    // 특정 반의 모든 선생님 매핑을 삭제합니다.
    // 주로 반을 삭제할 때 사용됩니다 - 반을 삭제하기 전에 모든 선생님 매핑을 먼저 제거해야 합니다.
    @Modifying
    @Query("DELETE FROM TeacherCourseAssignment t WHERE t.course.id = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);
}