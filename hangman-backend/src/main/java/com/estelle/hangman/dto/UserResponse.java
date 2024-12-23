package com.estelle.hangman.dto;

import com.estelle.hangman.domain.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Long id;              // 사용자 고유 번호
    private String username;      // 사용자 이름/아이디
    private Role role;           // 역할
    private String courseName;    // 학생인 경우 반 이름
    private String teacherName;   // 학생인 경우 담당 선생님 이름
    private LocalDateTime createdAt; // 계정 생성 시간
    private LocalDateTime updatedAt; // 정보 수정 시간
}