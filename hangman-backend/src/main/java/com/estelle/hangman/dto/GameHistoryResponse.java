package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 완료된 게임의 기록을 조회할 때 사용하는 클래스
@Getter
@Builder
public class GameHistoryResponse {
    private Long id;             // 게임 기록의 고유 ID
    private String word;         // 게임에 사용된 단어
    private boolean success;     // 성공 여부
    private int attempts;        // 시도 횟수
    private String wrongLetters; // 틀린 알파벳들 (쉼표로 구분)
    private LocalDateTime playedAt; // 게임 플레이 시간
}