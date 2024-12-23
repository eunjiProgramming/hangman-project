package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TeacherAssignmentRequest {
    private Long teacherId;  // 배정할 선생님의 ID
    private Long courseId;   // 배정할 반의 ID
}