package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameStartResponse {
    private Long gameId;
    private int wordLength;
    private String maskedWord;    // 예: "_ _ _ _ _"
    private int maxAttempts;
    private int remainingAttempts;
}