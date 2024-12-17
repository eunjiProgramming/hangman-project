package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GameStartRequest {
    private Long courseId;   // admin, manager용 (선택적)
    private Long teacherId;  // admin용 (선택적)
}