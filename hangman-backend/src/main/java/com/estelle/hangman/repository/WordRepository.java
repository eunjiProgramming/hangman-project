package com.estelle.hangman.repository;

import com.estelle.hangman.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findByTeacherId(Long teacherId);
    List<Word> findByCourseId(Long courseId);
    List<Word> findByCourseIdAndTeacherId(Long courseId, Long teacherId);
}