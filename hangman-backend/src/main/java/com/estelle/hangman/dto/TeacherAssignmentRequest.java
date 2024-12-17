package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TeacherAssignmentRequest {
    private Long teacherId;
    private Long courseId;
}