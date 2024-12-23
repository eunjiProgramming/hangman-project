package com.estelle.hangman.repository;

import com.estelle.hangman.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {
    // 특정 교사가 등록한 모든 단어 조회
    List<Word> findByTeacherId(Long teacherId);

    // 특정 반의 모든 단어 조회
    List<Word> findByCourseId(Long courseId);

    // 특정 반의 특정 교사가 등록한 단어 조회
    List<Word> findByCourseIdAndTeacherId(Long courseId, Long teacherId);

    // 카테고리별 단어 찾기 (난이도 옵션)
    @Query("SELECT w FROM Word w WHERE " +
            "(:category IS NULL OR w.category = :category) AND " +
            "(:difficulty IS NULL OR w.difficulty = :difficulty)")
    List<Word> findByCategoryAndDifficulty(
            @Param("category") String category,
            @Param("difficulty") Integer difficulty);

    // 모든 카테고리 목록 가져오기
    @Query("SELECT DISTINCT w.category FROM Word w ORDER BY w.category")
    List<String> findAllCategories();

    // 특정 선생님의 특정 카테고리 단어들 가져오기
    @Query("SELECT w FROM Word w WHERE w.teacher.id = :teacherId AND w.category = :category")
    List<Word> findByTeacherIdAndCategory(
            @Param("teacherId") Long teacherId,
            @Param("category") String category);

    // 특정 반의 특정 카테고리 단어들 가져오기
    @Query("SELECT w FROM Word w WHERE w.course.id = :courseId AND w.category = :category")
    List<Word> findByCourseIdAndCategory(
            @Param("courseId") Long courseId,
            @Param("category") String category);

    @Query("SELECT w FROM Word w WHERE " +
            "(:keyword IS NULL OR LOWER(w.word) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:category IS NULL OR w.category = :category) AND " +
            "(:difficulty IS NULL OR w.difficulty = :difficulty)")
    List<Word> searchWords(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("difficulty") Integer difficulty);
}