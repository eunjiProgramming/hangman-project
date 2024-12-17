package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GameGuessRequest {
    private Long gameId;
    private char letter;
}