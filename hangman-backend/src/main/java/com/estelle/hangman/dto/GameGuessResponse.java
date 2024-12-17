package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GameGuessResponse {
    private String maskedWord;
    private int remainingAttempts;
    private List<Character> guessedLetters;
    private List<Character> wrongLetters;
    private boolean isComplete;
    private boolean isSuccess;
}