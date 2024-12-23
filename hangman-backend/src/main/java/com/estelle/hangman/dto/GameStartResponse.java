package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameStartResponse {
    private Long gameId;         // 새로 시작된 게임의 ID
    private int wordLength;      // 맞춰야 할 단어의 길이
    private String maskedWord;   // 가려진 단어 (예: "_ _ _ _")
    private int maxAttempts;     // 최대 시도 가능 횟수
    private int remainingAttempts; // 남은 시도 횟수
}