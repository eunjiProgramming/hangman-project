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

// 반(수업) 관리를 담당하는 서비스 클래스입니다.
// 반 생성, 수정, 조회, 삭제 등의 모든 반 관련 비즈니스 로직을 처리합니다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본적으로 모든 메서드는 읽기 전용으로 설정됩니다
public class CourseService {

    // 반 정보를 데이터베이스에서 관리하는 리포지토리입니다
    private final CourseRepository courseRepository;

    // 교사-반 매핑 정보를 관리하는 리포지토리입니다
    private final TeacherCourseAssignmentRepository teacherCourseAssignmentRepository;

    // 새로운 반을 생성하는 메서드입니다
    // @Transactional 어노테이션으로 데이터 변경이 가능하도록 설정합니다
    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request) {
        // 새로운 반 객체를 생성합니다
        Course course = new Course();
        // 요청에서 받은 정보로 반 정보를 설정합니다
        course.setName(request.getName());          // 반 이름 (예: "Phonics 1A")
        course.setDescription(request.getDescription()); // 반 설명
        course.setLevel(request.getLevel());        // 수업 레벨 (예: "Beginner")

        // 데이터베이스에 반 정보를 저장하고
        // 저장된 정보를 CourseResponse 형태로 변환하여 반환합니다
        Course savedCourse = courseRepository.save(course);
        return convertToResponse(savedCourse);
    }

    // 기존 반 정보를 수정하는 메서드입니다
    @Transactional
    public CourseResponse updateCourse(Long id, CourseUpdateRequest request) {
        // 주어진 ID로 반 정보를 찾습니다
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 반을 찾을 수 없습니다"));

        // 요청에 포함된 정보만 선택적으로 업데이트합니다
        // 각 필드가 null이 아닐 때만 업데이트하여 부분 수정이 가능하도록 합니다
        if (request.getName() != null) {
            course.setName(request.getName());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getLevel() != null) {
            course.setLevel(request.getLevel());
        }

        // 수정된 정보를 CourseResponse 형태로 변환하여 반환합니다
        return convertToResponse(course);
    }

    // 특정 반의 정보를 조회하는 메서드입니다
    public CourseResponse getCourse(Long id) {
        // 주어진 ID로 반 정보를 찾아서 반환합니다
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 반을 찾을 수 없습니다"));
        return convertToResponse(course);
    }

    // 모든 반의 목록을 조회하는 메서드입니다
    public List<CourseResponse> getAllCourses() {
        // 모든 반 정보를 가져와서 CourseResponse 형태로 변환하여 반환합니다
        return courseRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 특정 반을 삭제하는 메서드입니다
    @Transactional
    public void deleteCourse(Long id) {
        // 해당 반에 대한 모든 교사 배정 정보를 먼저 삭제합니다
        teacherCourseAssignmentRepository.deleteAllByCourseId(id);
        // 그 다음 반 정보를 삭제합니다
        courseRepository.deleteById(id);
    }

    // Course 엔티티를 CourseResponse DTO로 변환하는 private 메서드입니다
    private CourseResponse convertToResponse(Course course) {
        // 해당 반에 배정된 모든 교사 정보를 가져옵니다
        List<TeacherCourseAssignment> assignments =
                teacherCourseAssignmentRepository.findAllByCourseId(course.getId());

        // CourseResponse 객체를 생성하여 반환합니다
        return CourseResponse.builder()
                .id(course.getId())                    // 반 ID
                .name(course.getName())                // 반 이름
                .description(course.getDescription())  // 반 설명
                .level(course.getLevel())             // 수업 레벨
                .teacherNames(assignments.stream()     // 배정된 교사 이름 목록
                        .map(a -> a.getTeacher().getUsername())
                        .collect(Collectors.toList()))
                .studentCount(course.getStudents().size())  // 학생 수
                .createdAt(course.getCreatedAt())     // 반 생성 시간
                .updatedAt(course.getUpdatedAt())     // 마지막 수정 시간
                .build();
    }
}