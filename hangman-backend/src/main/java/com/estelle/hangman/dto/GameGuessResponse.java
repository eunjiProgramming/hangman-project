package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GameGuessResponse {
    private String maskedWord;             // 현재까지 맞춘 상태 (예: "C_T")
    private int remainingAttempts;         // 남은 시도 횟수
    private List<Character> guessedLetters;// 지금까지 시도한 알파벳들
    private List<Character> wrongLetters;  // 틀린 알파벳들
    private boolean isComplete;            // 게임 종료 여부
    private boolean isSuccess;             // 성공/실패 여부
}