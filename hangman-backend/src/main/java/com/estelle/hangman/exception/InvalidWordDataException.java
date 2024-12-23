package com.estelle.hangman.exception;

public class InvalidWordDataException extends RuntimeException {
    public InvalidWordDataException(String message) {
        super(message);
    }
}
