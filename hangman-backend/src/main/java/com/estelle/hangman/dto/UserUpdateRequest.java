package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserUpdateRequest {
    private String password;  // 비밀번호 변경 시에만 사용
    private Long courseId;    // 학생의 반 변경 시 사용
    private Long teacherId;   // 학생의 담당 교사 변경 시 사용
}