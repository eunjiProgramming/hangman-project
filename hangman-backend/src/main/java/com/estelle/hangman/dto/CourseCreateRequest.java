package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CourseCreateRequest {
    private String name;         // 반 이름 (예: "Phonics 1A")
    private String description;  // 반 설명
    private String level;       // 레벨(Phonics/Beginner/Elementary)
}