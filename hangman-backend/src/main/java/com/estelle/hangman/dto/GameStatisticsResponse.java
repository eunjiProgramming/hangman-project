package com.estelle.hangman.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Getter
@Builder
public class GameStatisticsResponse {
    private int totalGames;
    private int gamesWon;
    private int gamesLost;
    private double winRate;
    private double averageAttempts;
    private String mostMissedLetters;
    private String bestPerformingWord;
    private String worstPerformingWord;
    private Map<String, Integer> timeDistribution;    // 시간대별 게임 수
    private Map<String, Double> progressTrend;        // 날짜별 승률 추이
}