package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

// 특정 반에 선생님을 배정할 때 필요한 데이터를 정의하는 클래스
@Getter  // Lombok: 모든 필드의 getter 메소드 자동 생성
@Setter  // Lombok: 모든 필드의 setter 메소드 자동 생성
public class TeacherAssignmentRequest {
    // 배정할 선생님의 ID
    private Long teacherId;

    // 배정할 반의 ID
    private Long courseId;
}