package com.estelle.hangman.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity  // 이 클래스가 데이터베이스 테이블과 연결된다는 표시
@Table(name = "courses")  // 실제 데이터베이스의 'courses' 테이블과 연결
@Getter  // Lombok: 모든 필드의 get메소드를 자동으로 생성 (예: getId(), getName() 등)
@Setter  // Lombok: 모든 필드의 set메소드를 자동으로 생성 (예: setId(), setName() 등)
@NoArgsConstructor  // Lombok: 매개변수 없는 기본 생성자를 자동으로 생성 (예: new Course())
public class Course {  // Course(반) 클래스 시작

    @Id  // 이것이 고유 식별자(primary key)라는 표시
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID를 자동으로 생성(1, 2, 3...)
    private Long id;  // 각 반의 고유 번호

    @Column(nullable = false)  // 이 필드는 반드시 값이 있어야 함
    private String name;  // 반 이름 (예: "Phonics 1A", "Beginner 2B")

    private String description;  // 반 설명 (예: "기초 파닉스 수업")

    private String level;  // 수업 레벨 (예: "Phonics", "Beginner", "Elementary")

    @OneToMany(mappedBy = "course")  // 한 반에 여러 학생이 있을 수 있음
    private List<User> students = new ArrayList<>();  // 이 반에 속한 학생들 목록

    @OneToMany(mappedBy = "course")  // 한 반에 여러 단어가 있을 수 있음
    private List<Word> words = new ArrayList<>();  // 이 반에서 사용하는 단어들 목록

    @CreationTimestamp  // 이 반이 생성된 시간을 자동으로 기록
    private LocalDateTime createdAt;  // 반 생성 시간

    @UpdateTimestamp  // 이 반 정보가 수정된 시간을 자동으로 기록
    private LocalDateTime updatedAt;  // 반 정보 수정 시간
}