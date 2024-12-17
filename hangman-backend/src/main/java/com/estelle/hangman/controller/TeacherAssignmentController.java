package com.estelle.hangman.controller;

import com.estelle.hangman.dto.TeacherAssignmentRequest;
import com.estelle.hangman.dto.TeacherAssignmentResponse;
import com.estelle.hangman.service.TeacherAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/assignments")
@RequiredArgsConstructor
public class TeacherAssignmentController {

    private final TeacherAssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<TeacherAssignmentResponse> assignTeacher(
            @RequestBody TeacherAssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.assignTeacher(request));
    }

    @DeleteMapping("/teacher/{teacherId}/course/{courseId}")
    public ResponseEntity<Void> removeAssignment(
            @PathVariable Long teacherId,
            @PathVariable Long courseId) {
        assignmentService.removeAssignment(teacherId, courseId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeacherAssignmentResponse>> getTeacherAssignments(
            @PathVariable Long teacherId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByTeacher(teacherId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<TeacherAssignmentResponse>> getCourseAssignments(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId));
    }
}