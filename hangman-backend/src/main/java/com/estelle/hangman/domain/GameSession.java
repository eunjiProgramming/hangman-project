package com.estelle.hangman.domain;

import java.util.HashSet;
import java.util.Set;

public class GameSession {
    private final Word word;
    private final Set<Character> guessedLetters = new HashSet<>();
    private final Set<Character> wrongLetters = new HashSet<>();
    private final int maxAttempts = 10;
    private boolean isComplete = false;
    private boolean isSuccess = false;

    public GameSession(Word word) {
        this.word = word;
    }

    public String getMaskedWord() {
        StringBuilder masked = new StringBuilder();
        for (char c : word.getWord().toCharArray()) {
            if (guessedLetters.contains(Character.toUpperCase(c))) {
                masked.append(c);
            } else {
                masked.append("_");
            }
            masked.append(" ");
        }
        return masked.toString().trim();
    }

    public boolean guessLetter(char letter) {
        letter = Character.toUpperCase(letter);
        if (guessedLetters.contains(letter)) {
            return false;
        }

        guessedLetters.add(letter);
        if (!word.getWord().toUpperCase().contains(String.valueOf(letter))) {
            wrongLetters.add(letter);
        }

        checkGameStatus();
        return true;
    }

    public void forfeit() {
        isComplete = true;
        isSuccess = false;
    }

    private void checkGameStatus() {
        if (wrongLetters.size() >= maxAttempts) {
            isComplete = true;
            return;
        }

        isSuccess = word.getWord().toUpperCase().chars()
                .mapToObj(c -> (char) c)
                .allMatch(guessedLetters::contains);
        isComplete = isSuccess;
    }

    // Getters
    public Word getWord() { return word; }
    public Set<Character> getGuessedLetters() { return guessedLetters; }
    public Set<Character> getWrongLetters() { return wrongLetters; }
    public int getMaxAttempts() { return maxAttempts; }
    public boolean isComplete() { return isComplete; }
    public boolean isSuccess() { return isSuccess; }
}