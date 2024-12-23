package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class WordResponse {
    private Long id;
    private String word;
    private String category;
    private Integer difficulty;
    private String courseName;
    private String teacherName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}