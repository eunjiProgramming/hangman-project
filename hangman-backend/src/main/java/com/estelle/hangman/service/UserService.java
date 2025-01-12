package com.estelle.hangman.service;

import com.estelle.hangman.domain.Course;
import com.estelle.hangman.domain.User;
import com.estelle.hangman.dto.UserCreateRequest;
import com.estelle.hangman.dto.UserResponse;
import com.estelle.hangman.dto.UserUpdateRequest;
import com.estelle.hangman.repository.CourseRepository;
import com.estelle.hangman.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service  // 이 클래스가 스프링의 서비스 계층 컴포넌트임을 나타냄
@RequiredArgsConstructor  // final로 선언된 필드의 생성자를 자동으로 만들어줌
@Transactional(readOnly = true)  // 기본적으로 모든 메서드는 읽기 전용으로 설정
public class UserService {

    // 사용자 정보를 데이터베이스에서 관리하는 리포지토리
    private final UserRepository userRepository;

    // 반 정보를 데이터베이스에서 관리하는 리포지토리
    private final CourseRepository courseRepository;

    // 비밀번호를 안전하게 암호화하는 인코더
    private final PasswordEncoder passwordEncoder;

    @Transactional  // 데이터를 변경하는 작업이므로 트랜잭션 시작
    public UserResponse createUser(UserCreateRequest request) {
        // 같은 사용자명이 이미 있는지 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // 새 사용자 객체 생성
        User user = new User();
        user.setUsername(request.getUsername());
        // 비밀번호는 반드시 암호화해서 저장 (보안을 위해)
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        // 만약 반 ID가 제공되었다면 (학생인 경우)
        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            user.setCourse(course);  // 학생을 반에 배정
        }

        // 만약 선생님 ID가 제공되었다면 (학생인 경우)
        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            user.setTeacher(teacher);  // 학생에게 담임 선생님 배정
        }

        // 데이터베이스에 저장하고 응답 객체로 변환하여 반환
        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    @Transactional  // 데이터를 변경하는 작업이므로 트랜잭션 시작
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        // ID로 사용자를 찾음
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 새 비밀번호가 제공되었다면 암호화하여 업데이트
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // 새로운 반 ID가 제공되었다면 반 정보 업데이트
        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            user.setCourse(course);
        }

        // 새로운 선생님 ID가 제공되었다면 담임 선생님 정보 업데이트
        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            user.setTeacher(teacher);
        }

        // 변경된 정보를 응답 객체로 변환하여 반환
        return convertToResponse(user);
    }

    // 특정 사용자의 정보를 조회
    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToResponse(user);
    }

    // 모든 사용자 목록을 조회
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()  // 모든 사용자를 가져와서
                .map(this::convertToResponse)     // 응답 객체로 변환
                .collect(Collectors.toList());     // 리스트로 모아서 반환
    }

    @Transactional  // 데이터를 변경하는 작업이므로 트랜잭션 시작
    public void deleteUser(Long id) {
        userRepository.deleteById(id);  // ID로 사용자를 찾아 삭제
    }

    // User 엔티티를 UserResponse DTO로 변환하는 private 메서드
    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())                    // 사용자의 고유 ID
                .username(user.getUsername())        // 사용자명
                .role(user.getRole())               // 역할(ADMIN/MANAGER/USER)
                // 학생인 경우 반 이름 (없으면 null)
                .courseName(user.getCourse() != null ? user.getCourse().getName() : null)
                // 학생인 경우 담임 선생님 이름 (없으면 null)
                .teacherName(user.getTeacher() != null ? user.getTeacher().getUsername() : null)
                .createdAt(user.getCreatedAt())     // 계정 생성 시간
                .updatedAt(user.getUpdatedAt())     // 정보 수정 시간
                .build();
    }
}