package com.estelle.hangman.dto;

import com.estelle.hangman.domain.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private Role role;
    private String courseName;    // 학생인 경우
    private String teacherName;   // 학생인 경우
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}