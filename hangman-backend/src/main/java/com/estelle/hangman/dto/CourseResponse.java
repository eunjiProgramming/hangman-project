package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CourseResponse {
    private Long id;                  // 반 고유 번호
    private String name;              // 반 이름
    private String description;       // 반 설명
    private String level;             // 레벨
    private List<String> teacherNames;// 담당 선생님 이름들
    private int studentCount;         // 학생 수
    private LocalDateTime createdAt;  // 반이 생성된 시간
    private LocalDateTime updatedAt;  // 반 정보가 수정된 시간
}