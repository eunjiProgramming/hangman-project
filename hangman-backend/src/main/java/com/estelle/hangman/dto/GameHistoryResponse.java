package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GameHistoryResponse {
    private Long id;
    private String word;
    private boolean success;
    private int attempts;
    private String wrongLetters;
    private LocalDateTime playedAt;
}