package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

// 단어 카테고리의 통계 정보를 조회할 때 사용하는 응답 데이터를 정의하는 클래스
@Getter
@Builder
public class WordCategoryResponse {
    // 카테고리 이름 (예: "Animals", "Colors")
    private String category;

    // 해당 카테고리에 속한 단어의 총 개수
    private int wordCount;

    // 해당 카테고리 단어들의 평균 난이도 (1~5 사이의 값)
    private double averageDifficulty;
}
