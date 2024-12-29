package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

// 기존 단어의 정보를 수정할 때 필요한 데이터를 정의하는 클래스
@Getter  // Lombok: 모든 필드의 getter 메소드 자동 생성
@Setter  // Lombok: 모든 필드의 setter 메소드 자동 생성
public class WordUpdateRequest {
    // 변경할 단어 (변경하지 않을 경우 null)
    private String word;

    // 변경할 카테고리 (변경하지 않을 경우 null)
    private String category;

    // 변경할 난이도 (변경하지 않을 경우 null)
    private Integer difficulty;
}

