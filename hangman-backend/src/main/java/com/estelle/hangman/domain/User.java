package com.estelle.hangman.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity  // 데이터베이스 테이블과 연결된다는 표시
@Table(name = "users")  // 실제 데이터베이스의 'users' 테이블과 연결
@Getter  // Lombok: 모든 필드의 get메소드를 자동으로 생성 (예: getId(), getUsername() 등)
@Setter  // Lombok: 모든 필드의 set메소드를 자동으로 생성 (예: setId(), setUsername() 등)
@NoArgsConstructor  // Lombok: 매개변수 없는 기본 생성자를 자동으로 생성 (예: new User())
public class User {  // User(사용자) 클래스 시작

    @Id  // 고유 식별자(primary key)라는 표시
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID를 자동으로 1씩 증가시켜 생성
    private Long id;  // 각 사용자의 고유 번호

    @Column(nullable = false, unique = true)  // 필수값이며, 중복될 수 없음
    private String username;  // 사용자 이름/아이디 (예: "john_doe")

    @Column(nullable = false)  // 필수값
    private String password;  // 비밀번호 (암호화되어 저장됨)

    @Enumerated(EnumType.STRING)  // Role enum을 문자열로 저장
    @Column(nullable = false)  // 필수값
    private Role role;  // 사용자 역할(ADMIN/MANAGER/USER)

    @ManyToOne(fetch = FetchType.LAZY)  // 여러 학생이 한 반에 속할 수 있음
    @JoinColumn(name = "course_id")  // 외래키로 course_id 사용
    private Course course;  // 학생이 속한 반 (학생인 경우에만 사용)

    @ManyToOne(fetch = FetchType.LAZY)  // 여러 학생이 한 선생님께 배정될 수 있음
    @JoinColumn(name = "teacher_id")  // 외래키로 teacher_id 사용
    private User teacher;  // 담당 선생님 (학생인 경우에만 사용)

    @CreationTimestamp  // 생성 시간 자동 기록
    private LocalDateTime createdAt;  // 계정 생성 시간

    @UpdateTimestamp  // 수정 시간 자동 기록
    private LocalDateTime updatedAt;  // 계정 정보 수정 시간
}