package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class AuthResponse {
    private String token;           // 로그인 성공하면 받는 인증 토큰
    private String username;        // 로그인한 사용자 이름
    private String role;           // 사용자 역할(ADMIN/MANAGER/USER)
    private Map<String, Object> additionalInfo;  // 추가 정보를 담는 Map
}