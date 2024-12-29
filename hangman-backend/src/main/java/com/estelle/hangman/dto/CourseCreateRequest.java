package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

// 새로운 반을 생성할 때 필요한 데이터를 정의하는 클래스
@Getter  // Lombok: 모든 필드의 getter 메소드 자동 생성
@Setter  // Lombok: 모든 필드의 setter 메소드 자동 생성
public class CourseCreateRequest {
    // 반 이름 (예: "Phonics 1A", "Beginner 2B")
    private String name;

    // 반에 대한 설명 (예: "초급 파닉스 수업, 알파벳과 기본 발음 학습")
    private String description;

    // 반의 레벨 (예: "Phonics", "Beginner", "Elementary")
    private String level;
}