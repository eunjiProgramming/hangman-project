package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

// 게임이 시작되었을 때 서버가 클라이언트에게 보내는 초기 정보를 정의하는 클래스
@Getter
@Builder
public class GameStartResponse {
    // 새로 시작된 게임의 고유 ID (이후 게임 진행시 이 ID로 식별)
    private Long gameId;

    // 맞춰야 할 단어의 길이 (예: "CAT"는 3)
    private int wordLength;

    // 가려진 단어 표시 (예: "_ _ _")
    private String maskedWord;

    // 최대 시도 가능 횟수 (보통 10회)
    private int maxAttempts;

    // 현재 남은 시도 횟수
    private int remainingAttempts;

}