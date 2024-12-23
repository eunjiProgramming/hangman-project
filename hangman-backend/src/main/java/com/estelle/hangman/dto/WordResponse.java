package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class WordResponse {
    private Long id;               // 단어의 고유 ID
    private String word;           // 단어 내용
    private String category;       // 카테고리 (Animals, Colors 등)
    private Integer difficulty;    // 난이도 (1-5)
    private String courseName;     // 단어가 속한 반 이름
    private String teacherName;    // 단어를 생성한 선생님 이름
    private LocalDateTime createdAt; // 단어 생성 시간
    private LocalDateTime updatedAt; // 단어 수정 시간
}