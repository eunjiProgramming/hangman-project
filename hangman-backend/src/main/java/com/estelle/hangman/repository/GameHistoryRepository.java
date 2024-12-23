package com.estelle.hangman.repository;

import com.estelle.hangman.domain.GameHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {
    // 특정 학생의 모든 게임 기록 찾기
    List<GameHistory> findAllByStudentId(Long studentId);

    // 특정 선생님의 학생들의 모든 게임 기록 찾기
    List<GameHistory> findAllByStudentTeacherId(Long teacherId);

    // 특정 학생의 특정 기간 동안의 게임 기록 찾기
    @Query("SELECT gh FROM GameHistory gh WHERE gh.student.id = :studentId AND gh.playedAt BETWEEN :startDate AND :endDate")
    List<GameHistory> findByStudentIdAndPlayedAtBetween(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 특정 반의 모든 게임 기록 찾기
    @Query("SELECT gh FROM GameHistory gh WHERE gh.student.course.id = :courseId")
    List<GameHistory> findByCourseId(@Param("courseId") Long courseId);

    // 특정 선생님의 총 학생 수 계산
    @Query("SELECT DISTINCT gh.student.id FROM GameHistory gh WHERE gh.student.teacher.id = :teacherId")
    int countDistinctStudentsByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 선생님이 담당하는 반 학생들의 게임 성공률 계산
     * - GameHistory -> Student -> Course -> TeacherCourseAssignment 순으로 연결
     * - 결과는 0.0 ~ 1.0 사이의 값으로 반환 (예: 0.667 = 66.7% 성공률)
     */
    @Query(
            "SELECT AVG(CASE WHEN gh.isSuccess = true THEN 1.0 ELSE 0.0 END) " +
                    "FROM GameHistory gh " +
                    "JOIN gh.student.course c " +
                    "JOIN TeacherCourseAssignment tca ON tca.course.id = c.id " +
                    "WHERE tca.teacher.id = :teacherId"
    )
    Double calculateClassAverageByTeacherId(@Param("teacherId") Long teacherId);
}