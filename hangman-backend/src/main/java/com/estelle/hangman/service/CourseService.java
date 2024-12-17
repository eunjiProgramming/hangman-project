package com.estelle.hangman.service;

import com.estelle.hangman.domain.Course;
import com.estelle.hangman.domain.TeacherCourseAssignment;
import com.estelle.hangman.dto.CourseCreateRequest;
import com.estelle.hangman.dto.CourseResponse;
import com.estelle.hangman.dto.CourseUpdateRequest;
import com.estelle.hangman.repository.CourseRepository;
import com.estelle.hangman.repository.TeacherCourseAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final TeacherCourseAssignmentRepository teacherCourseAssignmentRepository;

    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request) {
        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setLevel(request.getLevel());

        Course savedCourse = courseRepository.save(course);
        return convertToResponse(savedCourse);
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseUpdateRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (request.getName() != null) {
            course.setName(request.getName());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getLevel() != null) {
            course.setLevel(request.getLevel());
        }

        return convertToResponse(course);
    }

    public CourseResponse getCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return convertToResponse(course);
    }

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCourse(Long id) {
        // 해당 코스의 교사 배정 정보도 함께 삭제
        teacherCourseAssignmentRepository.deleteAllByCourseId(id);
        courseRepository.deleteById(id);
    }

    private CourseResponse convertToResponse(Course course) {
        List<TeacherCourseAssignment> assignments =
                teacherCourseAssignmentRepository.findAllByCourseId(course.getId());

        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .level(course.getLevel())
                .teacherNames(assignments.stream()
                        .map(a -> a.getTeacher().getUsername())
                        .collect(Collectors.toList()))
                .studentCount(course.getStudents().size())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}