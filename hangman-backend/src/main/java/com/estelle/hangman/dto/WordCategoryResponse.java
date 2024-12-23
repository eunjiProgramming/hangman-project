package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WordCategoryResponse {
    private String category;       // 단어 카테고리 (예: "Animals", "Colors")
    private int wordCount;        // 해당 카테고리의 단어 수
    private double averageDifficulty; // 해당 카테고리 단어들의 평균 난이도
}
