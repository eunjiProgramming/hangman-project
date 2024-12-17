package com.estelle.hangman.repository;

import com.estelle.hangman.domain.GameHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {
    List<GameHistory> findAllByStudentId(Long studentId);
    List<GameHistory> findAllByStudentTeacherId(Long teacherId);

    @Query("SELECT gh FROM GameHistory gh WHERE gh.student.id = :studentId AND gh.playedAt BETWEEN :startDate AND :endDate")
    List<GameHistory> findByStudentIdAndPlayedAtBetween(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT gh FROM GameHistory gh WHERE gh.student.course.id = :courseId")
    List<GameHistory> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT DISTINCT gh.student.id FROM GameHistory gh WHERE gh.student.teacher.id = :teacherId")
    int countDistinctStudentsByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT AVG(CASE WHEN gh.isSuccess = true THEN 1.0 ELSE 0.0 END) " +
            "FROM GameHistory gh WHERE gh.student.teacher.id = :teacherId")
    Double calculateClassAverageByTeacherId(@Param("teacherId") Long teacherId);
}