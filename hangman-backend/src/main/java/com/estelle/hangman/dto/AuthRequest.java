package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {
    private String username;  // 로그인할 때 입력하는 사용자 이름
    private String password;  // 로그인할 때 입력하는 비밀번호
}