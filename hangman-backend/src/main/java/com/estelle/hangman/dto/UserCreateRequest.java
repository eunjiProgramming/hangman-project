package com.estelle.hangman.dto;

import com.estelle.hangman.domain.Role;
import lombok.Getter;
import lombok.Setter;

// 새로운 사용자(관리자/선생님/학생)를 생성할 때 필요한 데이터를 정의하는 클래스
@Getter  // Lombok: 모든 필드의 getter 메소드 자동 생성
@Setter  // Lombok: 모든 필드의 setter 메소드 자동 생성
public class UserCreateRequest {
    private String username;   // 생성할 사용자의 아이디 (예: "john_student")
    private String password;   // 생성할 사용자의 비밀번호 (예: "password123")

    // Role은 enum 타입임: ADMIN(관리자), MANAGER(선생님), USER(학생) 중 하나
    private Role role;

    // 학생인 경우 소속될 반 ID (선생님/관리자는 null)
    private Long courseId;

    // 학생인 경우 담당 선생님 ID (선생님/관리자는 null)
    private Long teacherId;
}