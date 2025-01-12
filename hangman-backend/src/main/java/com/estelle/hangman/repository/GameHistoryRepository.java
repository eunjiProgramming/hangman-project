package com.estelle.hangman.repository;

import com.estelle.hangman.domain.GameHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

// GameHistory 엔티티에 대한 데이터베이스 작업을 처리합니다.
public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {

    // 특정 학생의 모든 게임 기록을 찾는 메소드입니다.
    // 메소드 이름으로부터 자동 생성되는 쿼리입니다.
    List<GameHistory> findAllByStudentId(Long studentId);

    // 특정 선생님의 학생들의 모든 게임 기록을 찾습니다.
    // student의 teacher_id를 기준으로 검색합니다.
    List<GameHistory> findAllByStudentTeacherId(Long teacherId);

    // 특정 학생의 특정 기간 동안의 게임 기록을 찾습니다.
    // BETWEEN 절을 사용하여 시작 날짜와 종료 날짜 사이의 기록을 조회합니다.
    @Query("SELECT gh FROM GameHistory gh WHERE gh.student.id = :studentId AND gh.playedAt BETWEEN :startDate AND :endDate")
    List<GameHistory> findByStudentIdAndPlayedAtBetween(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 특정 반의 모든 게임 기록을 찾습니다.
    @Query("SELECT gh FROM GameHistory gh WHERE gh.student.course.id = :courseId")
    List<GameHistory> findByCourseId(@Param("courseId") Long courseId);

    // 특정 선생님의 총 학생 수를 계산합니다.
    // DISTINCT를 사용하여 중복되는 학생 ID를 제거합니다.
    @Query("SELECT DISTINCT gh.student.id FROM GameHistory gh WHERE gh.student.teacher.id = :teacherId")
    int countDistinctStudentsByTeacherId(@Param("teacherId") Long teacherId);

    // 선생님이 담당하는 반 학생들의 게임 성공률을 계산합니다.
    // CASE WHEN을 사용하여 성공/실패를 1과 0으로 변환한 후 평균을 계산합니다.
    @Query(
            "SELECT AVG(CASE WHEN gh.isSuccess = true THEN 1.0 ELSE 0.0 END) " +
                    "FROM GameHistory gh " +
                    "JOIN gh.student.course c " +
                    "JOIN TeacherCourseAssignment tca ON tca.course.id = c.id " +
                    "WHERE tca.teacher.id = :teacherId"
    )
    Double calculateClassAverageByTeacherId(@Param("teacherId") Long teacherId);
}