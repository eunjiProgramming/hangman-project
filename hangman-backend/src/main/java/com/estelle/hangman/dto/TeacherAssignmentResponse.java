package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TeacherAssignmentResponse {
    private Long id;
    private String teacherName;
    private String courseName;
    private LocalDateTime createdAt;
}