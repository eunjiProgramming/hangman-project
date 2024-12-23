package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WordUpdateRequest {
    private String word;       // 수정할 단어 내용
    private String category;   // 수정할 카테고리 (Animals, Colors 등)
    private Integer difficulty; // 수정할 난이도 (1-5)
}

