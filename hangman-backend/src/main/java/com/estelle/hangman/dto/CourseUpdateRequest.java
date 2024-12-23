package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CourseUpdateRequest {
    private String name;         // 수정할 반 이름
    private String description;  // 수정할 반 설명
    private String level;        // 수정할 레벨
}