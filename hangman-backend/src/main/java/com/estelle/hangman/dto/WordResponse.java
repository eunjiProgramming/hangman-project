package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

// 단어 정보를 조회할 때 서버가 클라이언트에게 보내는 응답 데이터를 정의하는 클래스
@Getter   // Lombok: 모든 필드의 getter 메소드 자동 생성
@Builder  // Lombok: 빌더 패턴 구현을 자동화 (객체 생성을 더 유연하게 함)
public class WordResponse {
    private Long id;               // 단어의 고유 ID
    private String word;           // 단어
    private String category;       // 카테고리
    private Integer difficulty;    // 난이도
    private String courseName;     // 이 단어가 속한 반 이름
    private String teacherName;    // 이 단어를 등록한 선생님 이름
    private LocalDateTime createdAt; // 단어 등록 시간
    private LocalDateTime updatedAt; // 단어 정보 마지막 수정 시간
}