package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.Map;

// 게임 통계 정보를 조회할 때 사용하는 클래스
@Getter
@Builder
public class GameStatisticsResponse {
    private int totalGames;          // 총 게임 수
    private int gamesWon;            // 이긴 게임 수
    private int gamesLost;           // 진 게임 수
    private double winRate;          // 승률 (%)
    private double averageAttempts;  // 평균 시도 횟수
    private String mostMissedLetters; // 가장 많이 틀린 알파벳들
    private String bestPerformingWord;  // 가장 잘 맞춘 단어
    private String worstPerformingWord; // 가장 못 맞춘 단어

    // 시간대별 게임 수 (예: {"09:00": 5, "10:00": 3})
    private Map<String, Integer> timeDistribution;

    // 날짜별 승률 추이 (예: {"2024-03-15": 75.0, "2024-03-16": 80.0})
    private Map<String, Double> progressTrend;

}