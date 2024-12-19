package com.estelle.hangman.domain;

public enum Role {
    ADMIN,   // 관리자: 시스템의 모든 것을 관리할 수 있는 사람
    MANAGER, // 선생님: 자신의 반과 학생들만 관리할 수 있는 사람
    USER     // 학생: 게임만 플레이할 수 있는 사람
}