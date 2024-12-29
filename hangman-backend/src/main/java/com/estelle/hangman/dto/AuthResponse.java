package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

// 로그인 성공 시 서버가 클라이언트에게 보내는 응답 데이터 형식을 정의하는 클래스
@Getter  // Lombok: 모든 필드의 getter 메소드를 자동 생성
@Builder // Lombok: 빌더 패턴 구현을 자동화 (객체 생성을 더 유연하게 함)
public class AuthResponse {
    private String token;           // JWT 인증 토큰 (예: "eyJhbGciOiJIUzI1...")
    private String username;        // 로그인한 사용자의 아이디 (예: "john_teacher")
    private String role;           // 사용자의 역할 (예: "ADMIN", "MANAGER", "USER")

    // 추가 정보를 담는 Map (예: 학생의 경우 소속 반 정보, 담당 선생님 정보 등)
    private Map<String, Object> additionalInfo;
}