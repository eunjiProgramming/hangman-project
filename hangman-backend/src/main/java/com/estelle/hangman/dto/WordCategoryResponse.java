package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WordCategoryResponse {
    private String category;
    private int wordCount;
    private double averageDifficulty;
}
