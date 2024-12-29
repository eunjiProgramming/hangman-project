package com.estelle.hangman.dto;


import lombok.Getter;
import lombok.Setter;

// 새로운 단어를 생성할 때 필요한 데이터를 정의하는 클래스
@Getter  // Lombok: 모든 필드의 getter 메소드 자동 생성
@Setter  // Lombok: 모든 필드의 setter 메소드 자동 생성
public class WordCreateRequest {
    // 추가할 단어 (예: "CAT", "DOG") - 저장 시 자동으로 대문자로 변환됨
    private String word;

    // 이 단어를 사용할 반의 ID (예: Phonics 1A반의 ID)
    private Long courseId;

    // 이 단어를 등록하는 선생님의 ID
    private Long teacherId;

    // 단어의 카테고리 (예: "Animals", "Colors", "Numbers")
    private String category;

    // 단어의 난이도 (1: 매우 쉬움 ~ 5: 매우 어려움)
    private Integer difficulty;
}

