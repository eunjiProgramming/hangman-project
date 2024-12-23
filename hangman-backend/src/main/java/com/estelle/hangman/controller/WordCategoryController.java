package com.estelle.hangman.controller;

import com.estelle.hangman.dto.WordCategoryResponse;
import com.estelle.hangman.dto.WordWithCategoryResponse;
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

    @GetMapping("/categories")
    public ResponseEntity<List<WordCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(wordService.getAllCategories());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<WordWithCategoryResponse>> getWordsByCategory(
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
}