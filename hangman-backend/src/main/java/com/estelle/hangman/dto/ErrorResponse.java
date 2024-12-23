package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;  // 에러 발생 시간
    private int status;              // HTTP 상태 코드 (예: 404, 500)
    private String message;          // 에러 메시지
    private String detail;           // 자세한 에러 설명
}