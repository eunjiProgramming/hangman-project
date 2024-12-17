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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherAssignmentService {

    private final TeacherCourseAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public TeacherAssignmentResponse assignTeacher(TeacherAssignmentRequest request) {
        // 교사와 코스 존재 여부 확인
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found"));

        if (teacher.getRole() != Role.MANAGER) {
            throw new IllegalArgumentException("User is not a teacher");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException("Course not found"));

        // 이미 할당되어 있는지 확인
        if (assignmentRepository.existsByTeacherIdAndCourseId(
                request.getTeacherId(), request.getCourseId())) {
            throw new DuplicateAssignmentException("Teacher is already assigned to this course");
        }

        // 새로운 할당 생성
        TeacherCourseAssignment assignment = new TeacherCourseAssignment();
        assignment.setTeacher(teacher);
        assignment.setCourse(course);

        TeacherCourseAssignment savedAssignment = assignmentRepository.save(assignment);
        return convertToResponse(savedAssignment);
    }

    @Transactional
    public void removeAssignment(Long teacherId, Long courseId) {
        if (!assignmentRepository.existsByTeacherIdAndCourseId(teacherId, courseId)) {
            throw new IllegalArgumentException("Assignment not found");
        }
        assignmentRepository.deleteByTeacherIdAndCourseId(teacherId, courseId);
    }

    public List<TeacherAssignmentResponse> getAssignmentsByTeacher(Long teacherId) {
        if (!userRepository.existsById(teacherId)) {
            throw new TeacherNotFoundException("Teacher not found");
        }
        return assignmentRepository.findAllByTeacherId(teacherId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<TeacherAssignmentResponse> getAssignmentsByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new CourseNotFoundException("Course not found");
        }
        return assignmentRepository.findAllByCourseId(courseId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private TeacherAssignmentResponse convertToResponse(TeacherCourseAssignment assignment) {
        return TeacherAssignmentResponse.builder()
                .id(assignment.getId())
                .teacherName(assignment.getTeacher().getUsername())
                .courseName(assignment.getCourse().getName())
                .createdAt(assignment.getCreatedAt())
                .build();
    }
}