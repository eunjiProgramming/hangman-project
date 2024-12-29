package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

// 기존 사용자의 정보를 수정할 때 필요한 데이터를 정의하는 클래스
@Getter  // Lombok: 모든 필드의 getter 메소드 자동 생성
@Setter  // Lombok: 모든 필드의 setter 메소드 자동 생성
public class UserUpdateRequest {
    // 변경할 비밀번호 (변경하지 않을 경우 null)
    private String password;

    // 학생의 반을 변경할 경우 새로운 반 ID (변경하지 않을 경우 null)
    private Long courseId;

    // 학생의 담당 선생님을 변경할 경우 새로운 선생님 ID (변경하지 않을 경우 null)
    private Long teacherId;
}