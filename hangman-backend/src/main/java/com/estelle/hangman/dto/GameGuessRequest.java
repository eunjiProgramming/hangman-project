package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GameGuessRequest {
    private Long gameId;  // 현재 진행 중인 게임의 ID
    private char letter;  // 추측하는 알파벳
}