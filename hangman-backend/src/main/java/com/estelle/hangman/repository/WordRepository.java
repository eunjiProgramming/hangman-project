package com.estelle.hangman.repository;

import com.estelle.hangman.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Word 엔티티를 관리하는 리포지토리입니다.
// 게임에서 사용되는 단어들을 관리합니다.
public interface WordRepository extends JpaRepository<Word, Long> {

    // 특정 선생님이 등록한 모든 단어를 조회합니다.
    List<Word> findByTeacherId(Long teacherId);

    // 특정 반의 모든 단어를 조회합니다.
    List<Word> findByCourseId(Long courseId);

    // 특정 반의 특정 선생님이 등록한 단어를 조회합니다.
    List<Word> findByCourseIdAndTeacherId(Long courseId, Long teacherId);

    // 카테고리와 난이도로 단어를 검색합니다.
    // category나 difficulty가 null이면 해당 조건은 무시됩니다.
    @Query("SELECT w FROM Word w WHERE " +
            "(:category IS NULL OR w.category = :category) AND " +
            "(:difficulty IS NULL OR w.difficulty = :difficulty)")
    List<Word> findByCategoryAndDifficulty(
            @Param("category") String category,
            @Param("difficulty") Integer difficulty);

    // 모든 카테고리 목록을 가져옵니다.
    // DISTINCT를 사용하여 중복을 제거합니다.
    @Query("SELECT DISTINCT w.category FROM Word w ORDER BY w.category")
    List<String> findAllCategories();

    // 특정 선생님의 특정 카테고리 단어들을 가져옵니다.
    @Query("SELECT w FROM Word w WHERE w.teacher.id = :teacherId AND w.category = :category")
    List<Word> findByTeacherIdAndCategory(
            @Param("teacherId") Long teacherId,
            @Param("category") String category);

    // 특정 반의 특정 카테고리 단어들을 가져옵니다.
    @Query("SELECT w FROM Word w WHERE w.course.id = :courseId AND w.category = :category")
    List<Word> findByCourseIdAndCategory(
            @Param("courseId") Long courseId,
            @Param("category") String category);

    // 키워드, 카테고리, 난이도로 단어를 검색합니다.
    // LOWER 함수를 사용하여 대소문자 구분 없이 검색합니다.
    @Query("SELECT w FROM Word w WHERE " +
            "(:keyword IS NULL OR LOWER(w.word) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:category IS NULL OR w.category = :category) AND " +
            "(:difficulty IS NULL OR w.difficulty = :difficulty)")
    List<Word> searchWords(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("difficulty") Integer difficulty);
}