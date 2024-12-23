package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WordWithCategoryResponse {
    private Long id;
    private String word;
    private String category;
    private Integer difficulty;
    private String courseName;
    private String teacherName;
}