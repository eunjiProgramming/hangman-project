package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 반 정보를 조회할 때 서버가 클라이언트에게 보내는 응답 데이터를 정의하는 클래스
@Getter   // Lombok: 모든 필드의 getter 메소드 자동 생성
@Builder  // Lombok: 빌더 패턴 구현을 자동화 (객체 생성을 더 유연하게 함)
public class CourseResponse {
    private Long id;                  // 반의 고유 ID
    private String name;              // 반 이름
    private String description;       // 반 설명
    private String level;             // 반 레벨

    // 이 반을 담당하는 모든 선생님들의 이름 목록
    private List<String> teacherNames;

    // 이 반에 속한 학생 수
    private int studentCount;

    private LocalDateTime createdAt;  // 반 생성 시간
    private LocalDateTime updatedAt;  // 반 정보 마지막 수정 시간
}