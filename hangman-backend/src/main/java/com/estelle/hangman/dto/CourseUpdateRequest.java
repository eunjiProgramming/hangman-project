package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

// 기존 반의 정보를 수정할 때 필요한 데이터를 정의하는 클래스
@Getter  // Lombok: 모든 필드의 getter 메소드 자동 생성
@Setter  // Lombok: 모든 필드의 setter 메소드 자동 생성
public class CourseUpdateRequest {
    // 변경할 반 이름 (변경하지 않을 경우 null)
    private String name;

    // 변경할 반 설명 (변경하지 않을 경우 null)
    private String description;

    // 변경할 레벨 (변경하지 않을 경우 null)
    private String level;
}