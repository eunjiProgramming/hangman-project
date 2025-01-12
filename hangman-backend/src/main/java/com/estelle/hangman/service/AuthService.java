package com.estelle.hangman.service;

import com.estelle.hangman.domain.Course;
import com.estelle.hangman.domain.Role;
import com.estelle.hangman.domain.User;
import com.estelle.hangman.dto.AuthRequest;
import com.estelle.hangman.dto.AuthResponse;
import com.estelle.hangman.dto.UserCreateRequest;
import com.estelle.hangman.dto.UserResponse;
import com.estelle.hangman.repository.CourseRepository;
import com.estelle.hangman.repository.UserRepository;
import com.estelle.hangman.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

// 사용자 인증(로그인)과 등록(회원가입)을 처리하는 서비스 클래스입니다.
// @Service: 이 클래스가 스프링의 서비스 계층 컴포넌트임을 나타냅니다.
// @RequiredArgsConstructor: final 필드에 대한 생성자를 자동으로 만들어줍니다.
// @Transactional(readOnly = true): 모든 메서드를 기본적으로 읽기 전용으로 설정합니다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    // 사용자 정보를 데이터베이스에서 조회/저장하는 리포지토리
    private final UserRepository userRepository;

    // 반(클래스) 정보를 데이터베이스에서 조회/저장하는 리포지토리
    private final CourseRepository courseRepository;

    // 비밀번호를 안전하게 암호화하는 인코더
    private final PasswordEncoder passwordEncoder;

    // JWT 토큰을 생성하고 검증하는 유틸리티
    private final JwtTokenProvider tokenProvider;

    // 스프링 시큐리티의 인증을 처리하는 매니저
    private final AuthenticationManager authenticationManager;

    // 로그인 요청을 처리하는 메서드
    // @Transactional: 데이터베이스 트랜잭션을 시작합니다.
    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        try {
            // 스프링 시큐리티의 인증 토큰을 생성하고 인증을 시도합니다.
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // 인증된 사용자의 정보를 데이터베이스에서 가져옵니다.
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));

            // JWT 토큰을 생성합니다.
            String token = tokenProvider.generateToken(authentication);

            // 추가 정보를 담을 Map을 생성합니다.
            Map<String, Object> additionalInfo = new HashMap<>();

            // 만약 로그인한 사용자가 학생(USER)이라면
            if (user.getRole() == Role.USER) {
                // 학생이 속한 반 정보를 가져옵니다.
                Course course = user.getCourse();
                // 학생의 담당 선생님 정보를 가져옵니다.
                User teacher = user.getTeacher();

                // 반 정보가 있다면 추가 정보에 포함시킵니다.
                if (course != null) {
                    additionalInfo.put("courseId", course.getId());
                    additionalInfo.put("courseName", course.getName());
                }
                // 선생님 정보가 있다면 추가 정보에 포함시킵니다.
                if (teacher != null) {
                    additionalInfo.put("teacherId", teacher.getId());
                    additionalInfo.put("teacherName", teacher.getUsername());
                }
            }

            // 인증 응답 객체를 생성하여 반환합니다.
            return AuthResponse.builder()
                    .token(token)  // JWT 토큰
                    .username(user.getUsername())  // 사용자 이름
                    .role(user.getRole().name())  // 사용자 역할(ADMIN/MANAGER/USER)
                    .additionalInfo(additionalInfo)  // 추가 정보
                    .build();

        } catch (AuthenticationException e) {
            // 인증 실패시 BadCredentialsException을 발생시킵니다.
            throw new BadCredentialsException("아이디나 비밀번호가 잘못되었습니다");
        }
    }

    // 회원가입 요청을 처리하는 메서드
    @Transactional
    public UserResponse register(UserCreateRequest request) {
        // 동일한 사용자명이 이미 존재하는지 확인합니다.
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용중인 사용자명입니다");
        }

        // 사용자 역할과 필요한 정보들이 올바른지 검증합니다.
        validateRoleAndAssignments(request);

        // 새로운 사용자 객체를 생성합니다.
        User user = new User();
        user.setUsername(request.getUsername());
        // 비밀번호를 암호화하여 저장합니다.
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        // 학생인 경우 반과 선생님 정보를 설정합니다.
        if (request.getRole() == Role.USER) {
            setCourseAndTeacher(user, request);
        }

        // 사용자 정보를 데이터베이스에 저장합니다.
        User savedUser = userRepository.save(user);
        // 저장된 정보를 응답 형식으로 변환하여 반환합니다.
        return convertToUserResponse(savedUser);
    }

    // 사용자 역할과 필요한 정보들이 올바른지 검증하는 private 메서드
    private void validateRoleAndAssignments(UserCreateRequest request) {
        // 학생으로 가입하는 경우
        if (request.getRole() == Role.USER) {
            // 반과 선생님 정보가 모두 필요합니다
            if (request.getCourseId() == null || request.getTeacherId() == null) {
                throw new IllegalArgumentException("학생은 반과 선생님 정보가 필요합니다");
            }
        }
    }

    // 학생의 반과 선생님 정보를 설정하는 private 메서드
    private void setCourseAndTeacher(User user, UserCreateRequest request) {
        // 요청된 반 ID로 반 정보를 찾습니다.
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("해당 반을 찾을 수 없습니다"));

        // 요청된 선생님 ID로 선생님 정보를 찾습니다.
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("해당 선생님을 찾을 수 없습니다"));

        // 선택된 선생님이 실제로 선생님 역할인지 확인합니다.
        if (teacher.getRole() != Role.MANAGER) {
            throw new IllegalArgumentException("선택된 사용자가 선생님이 아닙니다");
        }

        // 학생에게 반과 선생님 정보를 설정합니다.
        user.setCourse(course);
        user.setTeacher(teacher);
    }

    // User 엔티티를 UserResponse DTO로 변환하는 private 메서드
    private UserResponse convertToUserResponse(User user) {
        // UserResponse 객체를 생성하여 반환합니다.
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .courseName(user.getCourse() != null ? user.getCourse().getName() : null)
                .teacherName(user.getTeacher() != null ? user.getTeacher().getUsername() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}