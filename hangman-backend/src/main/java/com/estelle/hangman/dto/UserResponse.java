package com.estelle.hangman.dto;

import com.estelle.hangman.domain.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 사용자 정보를 조회할 때 서버가 클라이언트에게 보내는 응답 데이터를 정의하는 클래스
@Getter   // Lombok: 모든 필드의 getter 메소드 자동 생성
@Builder  // Lombok: 빌더 패턴 구현을 자동화 (객체 생성을 더 유연하게 함)
public class UserResponse {
    private Long id;              // 사용자의 고유 ID (데이터베이스에서 자동 생성)
    private String username;      // 사용자의 아이디
    private Role role;           // 사용자의 역할 (ADMIN/MANAGER/USER)

    // 학생인 경우 소속된 반 이름 (선생님/관리자는 null)
    private String courseName;

    // 학생인 경우 담당 선생님 이름 (선생님/관리자는 null)
    private String teacherName;

    private LocalDateTime createdAt; // 계정 생성 시간
    private LocalDateTime updatedAt; // 마지막 정보 수정 시간
}