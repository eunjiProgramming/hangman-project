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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String token = tokenProvider.generateToken(authentication);

            Map<String, Object> additionalInfo = new HashMap<>();
            if (user.getRole() == Role.USER) {
                Course course = user.getCourse();
                User teacher = user.getTeacher();
                if (course != null) {
                    additionalInfo.put("courseId", course.getId());
                    additionalInfo.put("courseName", course.getName());
                }
                if (teacher != null) {
                    additionalInfo.put("teacherId", teacher.getId());
                    additionalInfo.put("teacherName", teacher.getUsername());
                }
            }

            return AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .role(user.getRole().name())
                    .additionalInfo(additionalInfo)
                    .build();
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username/password");
        }
    }

    @Transactional
    public UserResponse register(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        validateRoleAndAssignments(request);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        if (request.getRole() == Role.USER) {
            setCourseAndTeacher(user, request);
        }

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    private void validateRoleAndAssignments(UserCreateRequest request) {
        if (request.getRole() == Role.USER) {
            if (request.getCourseId() == null || request.getTeacherId() == null) {
                throw new IllegalArgumentException("Student requires course and teacher assignment");
            }
        }
    }

    private void setCourseAndTeacher(User user, UserCreateRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        if (teacher.getRole() != Role.MANAGER) {
            throw new IllegalArgumentException("Assigned teacher must have MANAGER role");
        }

        user.setCourse(course);
        user.setTeacher(teacher);
    }

    private UserResponse convertToUserResponse(User user) {
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