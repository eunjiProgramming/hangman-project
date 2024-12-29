package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 글자 추측 후 서버가 보내는 게임 상태 정보를 정의하는 클래스
@Getter
@Builder
public class GameGuessResponse {
    // 현재까지의 진행 상태 (예: "C_T" - A를 틀리고 C를 맞춘 상태)
    private String maskedWord;

    // 남은 시도 횟수
    private int remainingAttempts;

    // 지금까지 시도한 모든 알파벳들
    private List<Character> guessedLetters;

    // 틀린 알파벳들만 따로 모음
    private List<Character> wrongLetters;

    // 게임이 끝났는지 여부
    private boolean isComplete;

    // 성공/실패 여부
    private boolean isSuccess;
}