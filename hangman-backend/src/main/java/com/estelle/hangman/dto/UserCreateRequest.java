package com.estelle.hangman.dto;

import com.estelle.hangman.domain.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {
    private String username;   // 사용자 이름/아이디
    private String password;   // 비밀번호
    private Role role;        // 역할(ADMIN/MANAGER/USER)
    private Long courseId;    // 학생인 경우 속할 반 ID
    private Long teacherId;   // 학생인 경우 담당 선생님 ID
}