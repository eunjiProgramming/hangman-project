package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GameStartRequest {
    private Long courseId;   // 어느 반의 단어를 사용할지 (관리자/선생님용)
    private Long teacherId;  // 어느 선생님의 단어를 사용할지 (관리자용)
}