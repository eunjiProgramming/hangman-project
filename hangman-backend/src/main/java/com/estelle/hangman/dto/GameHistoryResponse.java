package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GameHistoryResponse {
    private Long id;             // 게임 기록의 고유 번호
    private String word;         // 플레이했던 단어
    private boolean success;     // 게임 성공 여부(true/false)
    private int attempts;        // 시도 횟수
    private String wrongLetters; // 틀린 알파벳들 (예: "X,Y,Z")
    private LocalDateTime playedAt; // 게임 플레이 시간
}