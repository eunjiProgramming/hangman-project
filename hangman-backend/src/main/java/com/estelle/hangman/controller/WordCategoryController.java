package com.estelle.hangman.controller;

import com.estelle.hangman.domain.Role;
import com.estelle.hangman.domain.User;
import com.estelle.hangman.dto.*;
import com.estelle.hangman.exception.UnauthorizedWordAccessException;
import com.estelle.hangman.repository.CourseRepository;
import com.estelle.hangman.repository.UserRepository;
import com.estelle.hangman.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordCategoryController {

    private final WordService wordService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @GetMapping("/categories")
    public ResponseEntity<List<WordCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(wordService.getAllCategories());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<WordResponse>> getWordsByCategory(
            @PathVariable String category,
            @RequestParam(required = false) Integer difficulty,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                wordService.getWordsByCategory(category, difficulty, userDetails.getUsername())
        );
    }

    @GetMapping("/teacher/categories")
    public ResponseEntity<List<WordCategoryResponse>> getTeacherCategories(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                wordService.getCategoriesByTeacher(userDetails.getUsername())
        );
    }

    @PostMapping
    public ResponseEntity<WordResponse> createWord(
            @RequestBody WordCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Admin이나 Manager만 단어를 생성할 수 있도록 체크
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.MANAGER) {
            throw new UnauthorizedWordAccessException("Only admin or teacher can create words");
        }

        // Manager인 경우 자신의 반에만 단어를 생성할 수 있도록 체크
        if (user.getRole() == Role.MANAGER) {
            boolean hasAccess = courseRepository.existsByIdAndTeacherId(
                    request.getCourseId(), user.getId());
            if (!hasAccess) {
                throw new UnauthorizedWordAccessException(
                        "Teacher can only create words for their own classes");
            }
        }

        request.setTeacherId(user.getId());  // 현재 로그인한 사용자를 teacher로 설정
        return ResponseEntity.ok(wordService.createWord(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WordResponse> updateWord(
            @PathVariable Long id,
            @RequestBody WordUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(wordService.updateWord(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWord(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        wordService.deleteWord(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<WordResponse>> searchWords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer difficulty) {
        return ResponseEntity.ok(wordService.searchWords(keyword, category, difficulty));
    }
}