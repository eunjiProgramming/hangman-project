package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

// 게임을 시작할 때 필요한 설정을 정의하는 클래스
@Getter
@Setter
public class GameStartRequest {
    // 어느 반의 단어를 사용할지 (관리자/선생님이 특정 반의 단어로 게임할 때 사용)
    private Long courseId;

    // 어느 선생님의 단어를 사용할지 (관리자가 특정 선생님의 단어로 게임할 때 사용)
    private Long teacherId;

    // 학생의 경우 이 두 필드가 모두 null - 자신의 반과 선생님으로 자동 설정됨
}