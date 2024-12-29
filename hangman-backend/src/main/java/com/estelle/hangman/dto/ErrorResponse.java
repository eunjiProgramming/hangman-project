package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

// 게임 중 발생하는 에러 정보를 클라이언트에게 전달하는 클래스
@Getter
@Builder
public class ErrorResponse {
    // 에러 발생 시간
    private LocalDateTime timestamp;

    // HTTP 상태 코드 (예: 404, 500)
    private int status;

    // 에러 메시지 (예: "Invalid letter")
    private String message;

    // 자세한 에러 설명
    private String detail;
}