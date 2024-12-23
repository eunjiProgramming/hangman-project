package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TeacherAssignmentResponse {
    private Long id;              // 매핑의 고유 번호
    private String teacherName;   // 배정된 선생님 이름
    private String courseName;    // 배정된 반 이름
    private LocalDateTime createdAt; // 배정된 시간
}