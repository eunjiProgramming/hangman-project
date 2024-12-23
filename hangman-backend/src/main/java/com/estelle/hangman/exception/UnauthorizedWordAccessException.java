package com.estelle.hangman.exception;

public class UnauthorizedWordAccessException extends RuntimeException {
    public UnauthorizedWordAccessException(String message) {
        super(message);
    }
}
