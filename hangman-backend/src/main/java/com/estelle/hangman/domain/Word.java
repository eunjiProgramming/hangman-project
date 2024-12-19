package com.estelle.hangman.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity  // 데이터베이스 테이블과 연결된다는 표시
@Table(name = "words")  // 실제 데이터베이스의 'words' 테이블과 연결
@Getter  // Lombok: 모든 필드의 get메소드를 자동으로 생성 (예: getId(), getWord() 등)
@Setter  // Lombok: 모든 필드의 set메소드를 자동으로 생성 (예: setId(), setWord() 등)
@NoArgsConstructor  // Lombok: 매개변수 없는 기본 생성자를 자동으로 생성 (예: new Word())
public class Word {  // Word(단어) 클래스 시작

    @Id  // 고유 식별자(primary key) 표시
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID 자동 생성
    private Long id;  // 각 단어의 고유 번호

    @Column(nullable = false)  // 필수값
    private String word;  // 실제 단어 (예: "CAT", "DOG")

    @ManyToOne(fetch = FetchType.LAZY)  // 여러 단어가 한 반에 속할 수 있음
    @JoinColumn(name = "course_id", nullable = false)  // course_id 필수값
    private Course course;  // 이 단어가 어느 반에서 사용되는지

    @ManyToOne(fetch = FetchType.LAZY)  // 여러 단어가 한 선생님께 속할 수 있음
    @JoinColumn(name = "teacher_id", nullable = false)  // teacher_id 필수값
    private User teacher;  // 어느 선생님이 이 단어를 등록했는지

    private String category;  // 단어 카테고리 (예: "Animals", "Colors")

    @Column(nullable = false)  // 필수값
    private Integer difficulty;  // 단어 난이도 (1: 쉬움 ~ 5: 어려움)

    @CreationTimestamp  // 생성 시간 자동 기록
    private LocalDateTime createdAt;  // 단어 등록 시간

    @UpdateTimestamp  // 수정 시간 자동 기록
    private LocalDateTime updatedAt;  // 단어 정보 수정 시간
}