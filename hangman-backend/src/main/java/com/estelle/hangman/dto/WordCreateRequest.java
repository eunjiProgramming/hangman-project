package com.estelle.hangman.dto;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WordCreateRequest {
    private String word;       // 생성할 단어
    private Long courseId;     // 단어가 속할 반의 ID
    private Long teacherId;    // 단어를 생성하는 선생님의 ID
    private String category;   // 단어의 카테고리 (Animals, Colors 등)
    private Integer difficulty; // 단어의 난이도 (1-5)
}

