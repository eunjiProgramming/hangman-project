package com.estelle.hangman.controller;

import com.estelle.hangman.dto.*;
import com.estelle.hangman.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/start")
    public ResponseEntity<GameStartResponse> startGame(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) GameStartRequest request) {
        return ResponseEntity.ok(gameService.startGame(userDetails.getUsername(), request));
    }

    @PostMapping("/guess")
    public ResponseEntity<GameGuessResponse> guessLetter(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody GameGuessRequest request) {
        return ResponseEntity.ok(gameService.guessLetter(userDetails.getUsername(), request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<GameHistoryResponse>> getGameHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gameService.getGameHistory(userDetails.getUsername()));
    }

    @GetMapping("/history/{studentId}")
    public ResponseEntity<List<GameHistoryResponse>> getStudentGameHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studentId) {
        return ResponseEntity.ok(gameService.getStudentGameHistory(userDetails.getUsername(), studentId));
    }

    @GetMapping("/statistics")
    public ResponseEntity<GameStatisticsResponse> getGameStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gameService.getGameStatistics(userDetails.getUsername()));
    }
    // 학생별 기간 지정 게임 기록 조회
    @GetMapping("/history/{studentId}/period")
    public ResponseEntity<List<GameHistoryResponse>> getStudentGameHistoryByPeriod(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studentId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(gameService.getStudentGameHistoryByPeriod(
                userDetails.getUsername(), studentId, startDate, endDate));
    }

    // 클래스별 통계 조회 (교사용)
    @GetMapping("/statistics/class/{courseId}")
    public ResponseEntity<GameStatisticsResponse> getClassStatistics(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(gameService.getClassStatistics(userDetails.getUsername(), courseId));
    }

    // 현재 진행중인 게임 상태 조회
    @GetMapping("/current/{gameId}")
    public ResponseEntity<GameGuessResponse> getCurrentGameStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.getCurrentGameStatus(userDetails.getUsername(), gameId));
    }

    // 게임 포기/종료
    @PostMapping("/forfeit/{gameId}")
    public ResponseEntity<GameGuessResponse> forfeitGame(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.forfeitGame(userDetails.getUsername(), gameId));
    }

    // 단어 카테고리별 통계
    @GetMapping("/statistics/category/{category}")
    public ResponseEntity<GameStatisticsResponse> getCategoryStatistics(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String category) {
        return ResponseEntity.ok(gameService.getCategoryStatistics(userDetails.getUsername(), category));
    }
}