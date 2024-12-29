package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

// 로그인할 때 클라이언트가 서버로 보내는 데이터 형식을 정의하는 클래스
@Getter  // Lombok: 모든 필드의 getter 메소드를 자동 생성
@Setter  // Lombok: 모든 필드의 setter 메소드를 자동 생성
public class AuthRequest {
    private String username;  // 사용자가 입력한 아이디 (예: "john_teacher")
    private String password;  // 사용자가 입력한 비밀번호 (예: "password123")
}