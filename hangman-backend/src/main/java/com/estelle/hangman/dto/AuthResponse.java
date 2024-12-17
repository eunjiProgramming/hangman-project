package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class AuthResponse {
    private String token;
    private String username;
    private String role;
    private Map<String, Object> additionalInfo;
}