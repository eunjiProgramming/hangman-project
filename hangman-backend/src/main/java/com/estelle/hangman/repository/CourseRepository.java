package com.estelle.hangman.repository;

import com.estelle.hangman.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// JpaRepository를 상속받아 Course 엔티티에 대한 기본적인 CRUD 작업을 수행할 수 있게 됩니다.
// 첫 번째 타입 파라미터 Course는 다룰 엔티티, 두 번째 Long은 엔티티의 ID 타입입니다.
public interface CourseRepository extends JpaRepository<Course, Long> {

    // 같은 이름의 반이 있는지 확인하는 메소드입니다.
    // 메소드 이름 규칙에 따라 JPA가 자동으로 쿼리를 생성합니다.
    // "SELECT COUNT(*) > 0 FROM courses WHERE name = ?" 와 같은 SQL이 실행됩니다.
    boolean existsByName(String name);

    // @Query 어노테이션으로 직접 JPQL 쿼리를 작성합니다.
    // 특정 선생님이 담당하는 모든 반을 찾는 메소드입니다.
    // TeacherCourseAssignment 테이블을 통해 조인하여 결과를 가져옵니다.
    @Query("SELECT c FROM Course c JOIN TeacherCourseAssignment t ON c.id = t.course.id WHERE t.teacher.id = :teacherId")
    List<Course> findByTeacherId(@Param("teacherId") Long teacherId);

    // 특정 선생님이 특정 반을 담당하는지 확인하는 메소드입니다.
    // COUNT > 0 을 통해 존재 여부만 확인합니다.
    // CASE WHEN 구문을 사용하여 boolean 타입으로 결과를 반환합니다.
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TeacherCourseAssignment t WHERE t.course.id = :courseId AND t.teacher.id = :teacherId")
    boolean existsByIdAndTeacherId(@Param("courseId") Long courseId, @Param("teacherId") Long teacherId);
}