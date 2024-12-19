package com.estelle.hangman.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity  // 이 클래스가 데이터베이스 테이블과 연결된다는 표시
@Table(name = "game_history")  // 실제 데이터베이스의 'game_history' 테이블과 연결
@Getter  // Lombok: 모든 필드의 get메소드를 자동으로 생성 (예: getId(), getStudent() 등)
@Setter  // Lombok: 모든 필드의 set메소드를 자동으로 생성 (예: setId(), setStudent() 등)
@NoArgsConstructor  // Lombok: 매개변수 없는 기본 생성자를 자동으로 생성 (예: new GameHistory())
public class GameHistory {

    @Id  // 이것이 primary key(고유 식별자)라는 표시
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID를 자동으로 1씩 증가시켜 생성
    private Long id;  // 각 게임 기록의 고유 번호

    @ManyToOne(fetch = FetchType.LAZY)  // 여러 게임 기록이 한 학생에게 속할 수 있음
    @JoinColumn(name = "student_id", nullable = false)  // student_id 컬럼과 연결, 필수값
    private User student;  // 게임을 플레이한 학생

    @ManyToOne(fetch = FetchType.LAZY)  // 여러 게임 기록이 한 단어에 속할 수 있음
    @JoinColumn(name = "word_id", nullable = false)  // word_id 컬럼과 연결, 필수값
    private Word word;  // 플레이한 단어

    @Column(nullable = false)  // 이 필드는 필수값
    private Boolean isSuccess;  // 게임 성공 여부 (true/false)

    @Column(nullable = false)  // 이 필드는 필수값
    private Integer attempts;  // 시도 횟수

    private String wrongLetters;  // 틀린 알파벳들을 저장 (예: "A,B,C")

    @CreationTimestamp  // 데이터가 생성될 때 자동으로 시간 기록
    private LocalDateTime playedAt;  // 게임 플레이 시간
}