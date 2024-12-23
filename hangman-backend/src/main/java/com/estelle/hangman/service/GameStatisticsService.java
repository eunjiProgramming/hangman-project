package com.estelle.hangman.service;

import com.estelle.hangman.domain.GameHistory;
import com.estelle.hangman.domain.Role;
import com.estelle.hangman.domain.User;
import com.estelle.hangman.domain.Word;
import com.estelle.hangman.dto.GameStatisticsResponse;
import com.estelle.hangman.repository.GameHistoryRepository;
import com.estelle.hangman.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameStatisticsService {

    private final GameHistoryRepository gameHistoryRepository;
    private final UserRepository userRepository;
    private final GameHistoryService gameHistoryService;

    public GameStatisticsResponse getGameStatistics(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<GameHistory> histories = gameHistoryService.getHistoriesByUser(user);

        return buildGameStatistics(histories, user.getRole());
    }

    public GameStatisticsResponse buildGameStatistics(List<GameHistory> histories, Role role) {
        if (histories.isEmpty()) {
            return createEmptyStatistics();
        }

        // MANAGER(선생님)인 경우 새로운 계산 방식 사용
        if (role == Role.MANAGER) {
            Double avgSuccess = gameHistoryRepository.calculateClassAverageByTeacherId(
                    histories.get(0).getStudent().getTeacher().getId());
            if (avgSuccess == null) {
                return createEmptyStatistics();
            }

            int totalGames = histories.size();
            double averageAttempts = calculateAverageAttempts(histories);

            // 단어별 성능 분석
            Map<Word, List<GameHistory>> wordHistories = histories.stream()
                    .collect(Collectors.groupingBy(GameHistory::getWord));

            String bestWord = findBestPerformingWord(wordHistories);
            String worstWord = findWorstPerformingWord(wordHistories);
            String mostMissedLetters = findMostMissedLetters(histories);

            // 시간별 통계
            Map<LocalDate, List<GameHistory>> dailyStats = histories.stream()
                    .collect(Collectors.groupingBy(h -> h.getPlayedAt().toLocalDate()));

            Map<String, Integer> timeDistribution = calculateTimeDistribution(histories);
            Map<String, Double> progressTrend = calculateProgressTrend(dailyStats);

            return GameStatisticsResponse.builder()
                    .totalGames(totalGames)
                    .gamesWon((int)(totalGames * avgSuccess)) // 새로운 계산 방식으로 승리 수 계산
                    .gamesLost((int)(totalGames * (1 - avgSuccess))) // 새로운 계산 방식으로 패배 수 계산
                    .winRate(avgSuccess * 100) // 백분율로 변환
                    .averageAttempts(averageAttempts)
                    .mostMissedLetters(mostMissedLetters)
                    .bestPerformingWord(bestWord)
                    .worstPerformingWord(worstWord)
                    .timeDistribution(timeDistribution)
                    .progressTrend(progressTrend)
                    .build();
        }

        // ADMIN이나 USER의 경우 기존 계산 방식 사용
        int totalGames = histories.size();
        int gamesWon = (int) histories.stream().filter(GameHistory::getIsSuccess).count();
        int gamesLost = totalGames - gamesWon;
        double winRate = calculateWinRate(histories);
        double averageAttempts = calculateAverageAttempts(histories);

        // 단어별 성능 분석
        Map<Word, List<GameHistory>> wordHistories = histories.stream()
                .collect(Collectors.groupingBy(GameHistory::getWord));

        String bestWord = findBestPerformingWord(wordHistories);
        String worstWord = findWorstPerformingWord(wordHistories);
        String mostMissedLetters = findMostMissedLetters(histories);

        // 시간별 통계
        Map<LocalDate, List<GameHistory>> dailyStats = histories.stream()
                .collect(Collectors.groupingBy(h -> h.getPlayedAt().toLocalDate()));

        Map<String, Integer> timeDistribution = calculateTimeDistribution(histories);
        Map<String, Double> progressTrend = calculateProgressTrend(dailyStats);

        return GameStatisticsResponse.builder()
                .totalGames(totalGames)
                .gamesWon(gamesWon)
                .gamesLost(gamesLost)
                .winRate(winRate)
                .averageAttempts(averageAttempts)
                .mostMissedLetters(mostMissedLetters)
                .bestPerformingWord(bestWord)
                .worstPerformingWord(worstWord)
                .timeDistribution(timeDistribution)
                .progressTrend(progressTrend)
                .build();
    }

    private double calculateWinRate(List<GameHistory> histories) {
        if (histories.isEmpty()) return 0.0;
        long wins = histories.stream().filter(GameHistory::getIsSuccess).count();
        return (double) wins / histories.size() * 100;
    }

    private double calculateAverageAttempts(List<GameHistory> histories) {
        if (histories.isEmpty()) return 0.0;
        return histories.stream()
                .mapToInt(GameHistory::getAttempts)
                .average()
                .orElse(0.0);
    }

    private String findMostMissedLetters(List<GameHistory> histories) {
        Map<Character, Integer> missCount = new HashMap<>();
        histories.stream()
                .map(GameHistory::getWrongLetters)
                .filter(Objects::nonNull)
                .forEach(letters -> {
                    for (String letter : letters.split(",")) {
                        if (!letter.isEmpty()) {
                            missCount.merge(letter.charAt(0), 1, Integer::sum);
                        }
                    }
                });

        return missCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .map(e -> String.valueOf(e.getKey()))
                .collect(Collectors.joining(","));
    }

    private String findBestPerformingWord(Map<Word, List<GameHistory>> wordHistories) {
        return wordHistories.entrySet().stream()
                .map(entry -> Map.entry(
                        entry.getKey(),
                        calculateWordSuccessRate(entry.getValue())
                ))
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey().getWord())
                .orElse("");
    }

    private String findWorstPerformingWord(Map<Word, List<GameHistory>> wordHistories) {
        return wordHistories.entrySet().stream()
                .map(entry -> Map.entry(
                        entry.getKey(),
                        calculateWordSuccessRate(entry.getValue())
                ))
                .min(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey().getWord())
                .orElse("");
    }

    private double calculateWordSuccessRate(List<GameHistory> histories) {
        if (histories.isEmpty()) return 0.0;
        return histories.stream()
                .filter(GameHistory::getIsSuccess)
                .count() / (double) histories.size();
    }

    private Map<String, Integer> calculateTimeDistribution(List<GameHistory> histories) {
        return histories.stream()
                .collect(Collectors.groupingBy(
                        h -> String.format("%02d:00", h.getPlayedAt().getHour()),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    private Map<String, Double> calculateProgressTrend(Map<LocalDate, List<GameHistory>> dailyStats) {
        return dailyStats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> calculateWinRate(entry.getValue()),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public GameStatisticsResponse createEmptyStatistics() {
        return GameStatisticsResponse.builder()
                .totalGames(0)
                .gamesWon(0)
                .gamesLost(0)
                .winRate(0.0)
                .averageAttempts(0.0)
                .mostMissedLetters("")
                .bestPerformingWord("")
                .worstPerformingWord("")
                .timeDistribution(new HashMap<>())
                .progressTrend(new HashMap<>())
                .build();
    }
}