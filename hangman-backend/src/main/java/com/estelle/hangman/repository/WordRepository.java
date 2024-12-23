package com.estelle.hangman.repository;

import com.estelle.hangman.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {
    // 특정 교사가 등록한 모든 단어 조회
    List<Word> findByTeacherId(Long teacherId);

    // 특정 반의 모든 단어 조회
    List<Word> findByCourseId(Long courseId);

    // 특정 반의 특정 교사가 등록한 단어 조회
    List<Word> findByCourseIdAndTeacherId(Long courseId, Long teacherId);
}