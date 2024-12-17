package com.estelle.hangman.dto;

import com.estelle.hangman.domain.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {
    private String username;
    private String password;
    private Role role;
    private Long courseId;    // 학생인 경우 필요
    private Long teacherId;   // 학생인 경우 필요
}