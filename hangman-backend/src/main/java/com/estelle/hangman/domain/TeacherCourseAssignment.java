package com.estelle.hangman.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity  // 데이터베이스 테이블과 연결된다는 표시
@Table(name = "teacher_course_assignments",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"teacher_id", "course_id"}))  // 선생님-반 조합이 중복되면 안됨
@Getter  // Lombok: 모든 필드의 get메소드를 자동으로 생성 (예: getId(), getTeacher() 등)
@Setter  // Lombok: 모든 필드의 set메소드를 자동으로 생성 (예: setId(), setTeacher() 등)
@NoArgsConstructor  // Lombok: 매개변수 없는 기본 생성자를 자동으로 생성 (예: new TeacherCourseAssignment())
public class TeacherCourseAssignment {

    @Id  // 고유 식별자(primary key) 표시
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID 자동 생성
    private Long id;  // 각 매핑의 고유 번호

    @ManyToOne(fetch = FetchType.LAZY)  // 한 선생님이 여러 반을 담당할 수 있음
    @JoinColumn(name = "teacher_id", nullable = false)  // teacher_id 필수값
    private User teacher;  // 담당 선생님

    @ManyToOne(fetch = FetchType.LAZY)  // 한 반에 여러 선생님이 배정될 수 있음
    @JoinColumn(name = "course_id", nullable = false)  // course_id 필수값
    private Course course;  // 담당하는 반

    @CreationTimestamp  // 생성 시간 자동 기록
    private LocalDateTime createdAt;  // 매핑 생성 시간
}