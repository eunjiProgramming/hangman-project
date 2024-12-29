package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

// 게임 중 플레이어가 글자를 추측할 때 사용하는 클래스
@Getter
@Setter
public class GameGuessRequest {
    // 현재 진행 중인 게임의 ID
    private Long gameId;

    // 추측하는 알파벳 (예: 'A', 'B', 'C' 등)
    private char letter;
}