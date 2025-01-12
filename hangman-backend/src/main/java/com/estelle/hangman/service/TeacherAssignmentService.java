package com.estelle.hangman.service;

import com.estelle.hangman.domain.Course;
import com.estelle.hangman.domain.Role;
import com.estelle.hangman.domain.TeacherCourseAssignment;
import com.estelle.hangman.domain.User;
import com.estelle.hangman.dto.TeacherAssignmentRequest;
import com.estelle.hangman.dto.TeacherAssignmentResponse;
import com.estelle.hangman.exception.CourseNotFoundException;
import com.estelle.hangman.exception.DuplicateAssignmentException;
import com.estelle.hangman.exception.TeacherNotFoundException;
import com.estelle.hangman.repository.CourseRepository;
import com.estelle.hangman.repository.TeacherCourseAssignmentRepository;
import com.estelle.hangman.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service  // 스프링의 서비스 계층 컴포넌트임을 나타냄
@RequiredArgsConstructor  // final 필드에 대한 생성자를 자동으로 만들어줌
@Transactional(readOnly = true)  // 모든 메서드를 기본적으로 읽기 전용으로 설정
public class TeacherAssignmentService {

    // 교사-반 매핑 정보를 데이터베이스에서 관리하는 리포지토리
    private final TeacherCourseAssignmentRepository assignmentRepository;

    // 사용자(선생님) 정보를 데이터베이스에서 관리하는 리포지토리
    private final UserRepository userRepository;

    // 반 정보를 데이터베이스에서 관리하는 리포지토리
    private final CourseRepository courseRepository;

    @Transactional  // 데이터를 변경하는 작업이므로 트랜잭션 시작
    public TeacherAssignmentResponse assignTeacher(TeacherAssignmentRequest request) {
        // 선생님 정보를 데이터베이스에서 찾아옴
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found"));

        // 실제로 선생님인지(MANAGER 역할인지) 확인
        if (teacher.getRole() != Role.MANAGER) {
            throw new IllegalArgumentException("User is not a teacher");
        }

        // 반 정보를 데이터베이스에서 찾아옴
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException("Course not found"));

        // 이미 이 선생님이 이 반에 배정되어 있는지 확인
        if (assignmentRepository.existsByTeacherIdAndCourseId(
                request.getTeacherId(), request.getCourseId())) {
            throw new DuplicateAssignmentException("Teacher is already assigned to this course");
        }

        // 새로운 배정 정보 생성
        TeacherCourseAssignment assignment = new TeacherCourseAssignment();
        assignment.setTeacher(teacher);
        assignment.setCourse(course);

        // 데이터베이스에 저장하고 응답 객체로 변환하여 반환
        TeacherCourseAssignment savedAssignment = assignmentRepository.save(assignment);
        return convertToResponse(savedAssignment);
    }

    @Transactional  // 데이터를 변경하는 작업이므로 트랜잭션 시작
    public void removeAssignment(Long teacherId, Long courseId) {
        // 배정 정보가 존재하는지 먼저 확인
        if (!assignmentRepository.existsByTeacherIdAndCourseId(teacherId, courseId)) {
            throw new IllegalArgumentException("Assignment not found");
        }
        // 배정 정보 삭제
        assignmentRepository.deleteByTeacherIdAndCourseId(teacherId, courseId);
    }

    // 특정 선생님의 모든 반 배정 정보를 조회
    public List<TeacherAssignmentResponse> getAssignmentsByTeacher(Long teacherId) {
        // 선생님이 존재하는지 확인
        if (!userRepository.existsById(teacherId)) {
            throw new TeacherNotFoundException("Teacher not found");
        }
        // 선생님의 모든 배정 정보를 찾아서 응답 객체로 변환하여 반환
        return assignmentRepository.findAllByTeacherId(teacherId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 특정 반의 모든 선생님 배정 정보를 조회
    public List<TeacherAssignmentResponse> getAssignmentsByCourse(Long courseId) {
        // 반이 존재하는지 확인
        if (!courseRepository.existsById(courseId)) {
            throw new CourseNotFoundException("Course not found");
        }
        // 반의 모든 배정 정보를 찾아서 응답 객체로 변환하여 반환
        return assignmentRepository.findAllByCourseId(courseId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 배정 정보를 응답 객체로 변환하는 private 메서드
    private TeacherAssignmentResponse convertToResponse(TeacherCourseAssignment assignment) {
        return TeacherAssignmentResponse.builder()
                .id(assignment.getId())           // 배정 정보의 고유 ID
                .teacherName(assignment.getTeacher().getUsername())  // 선생님 이름
                .courseName(assignment.getCourse().getName())        // 반 이름
                .createdAt(assignment.getCreatedAt())               // 배정된 시간
                .build();
    }
}