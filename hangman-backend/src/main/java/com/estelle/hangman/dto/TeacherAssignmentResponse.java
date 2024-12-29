package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 선생님 배정 정보를 조회할 때 서버가 클라이언트에게 보내는 응답 데이터를 정의하는 클래스
@Getter   // Lombok: 모든 필드의 getter 메소드 자동 생성
@Builder  // Lombok: 빌더 패턴 구현을 자동화 (객체 생성을 더 유연하게 함)
public class TeacherAssignmentResponse {
    private Long id;              // 배정 정보의 고유 ID
    private String teacherName;   // 배정된 선생님 이름
    private String courseName;    // 배정된 반 이름
    private LocalDateTime createdAt; // 배정된 시간
}