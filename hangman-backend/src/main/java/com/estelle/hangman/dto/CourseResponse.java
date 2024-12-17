package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CourseResponse {
    private Long id;
    private String name;
    private String description;
    private String level;
    private List<String> teacherNames;  // 담당 교사 목록
    private int studentCount;           // 수강 학생 수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}