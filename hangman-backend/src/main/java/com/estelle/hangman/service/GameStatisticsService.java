package com.estelle.hangman.service;

import com.estelle.hangman.domain.GameHistory;               // 게임 기록 도메인 엔티티
import com.estelle.hangman.domain.Role;                     // 사용자 역할(학생, 선생님, 관리자 등)을 나타내는 enum
import com.estelle.hangman.domain.User;                     // 사용자 도메인 엔티티
import com.estelle.hangman.domain.Word;                     // 단어 도메인 엔티티
import com.estelle.hangman.dto.GameStatisticsResponse;      // 게임 통계 응답 DTO
import com.estelle.hangman.repository.GameHistoryRepository;// 게임 기록 리포지토리
import com.estelle.hangman.repository.UserRepository;       // 사용자 리포지토리
import lombok.RequiredArgsConstructor;                      // Lombok: final 필드 생성자 자동 생성
import org.springframework.security.core.userdetails.UsernameNotFoundException; // 사용자 미존재 시 예외
import org.springframework.stereotype.Service;              // 스프링 서비스 계층 어노테이션
import org.springframework.transaction.annotation.Transactional; // 스프링 트랜잭션 처리 어노테이션

import java.time.*;                                         // 날짜/시간 관련 클래스
import java.util.*;                                         // 자바 유틸리티
import java.util.stream.Collectors;                         // 스트림 API의 Collectors

/**
 * 게임 통계를 계산하고 관리하는 서비스 클래스입니다.
 * 사용자별, 단어별, 시간대별 다양한 통계를 제공합니다.
 */
@Service                                 // 스프링의 서비스 컴포넌트(Bean)로 등록
@RequiredArgsConstructor                  // Lombok: final 필드에 대한 생성자를 자동 생성
@Transactional(readOnly = true)          // 기본적으로 이 클래스의 메서드는 읽기 전용 트랜잭션 사용
public class GameStatisticsService {

    /**
     * 게임 기록을 관리하는 리포지토리
     * (GameHistory 엔티티에 대한 CRUD 작업 담당)
     */
    private final GameHistoryRepository gameHistoryRepository;

    /**
     * 사용자 정보를 관리하는 리포지토리
     * (User 엔티티에 대한 CRUD 작업 담당)
     */
    private final UserRepository userRepository;

    /**
     * 추가적인 게임 기록 기능을 제공하는 서비스
     * (예: 특정 조건에 맞는 히스토리 조회 등)
     */
    private final GameHistoryService gameHistoryService;

    /**
     * 특정 사용자의 게임 통계를 조회합니다.
     * @param username 통계를 조회할 사용자의 이름
     * @return GameStatisticsResponse 해당 사용자의 게임 통계 정보
     */
    public GameStatisticsResponse getGameStatistics(String username) {
        // 1. username으로 사용자 정보를 DB에서 조회
        User user = userRepository.findByUsername(username)
                // 2. 사용자 정보가 없으면 예외 발생
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 3. 해당 사용자의 모든 게임 기록을 가져옴
        List<GameHistory> histories = gameHistoryService.getHistoriesByUser(user);

        // 4. 가져온 게임 기록과 사용자 역할을 기반으로 통계 생성
        return buildGameStatistics(histories, user.getRole());
    }

    /**
     * 여러 게임 기록(List<GameHistory>)을 받아서 통계를 계산합니다.
     * 선생님(MANAGER)과 학생/관리자(USER, ADMIN)은 통계 계산 방식이 다릅니다.
     *
     * @param histories 게임 기록 리스트
     * @param role      사용자 역할
     * @return GameStatisticsResponse 계산된 통계 정보
     */
    public GameStatisticsResponse buildGameStatistics(List<GameHistory> histories, Role role) {
        // 1. 만약 기록이 없다면(빈 리스트라면) 기본값으로 채워진 통계를 반환
        if (histories.isEmpty()) {
            return createEmptyStatistics();
        }

        // 2. 선생님(MANAGER) 역할일 경우, 다른 계산 로직 사용
        if (role == Role.MANAGER) {
            // 2-1. 선생님의 학생들 전체 평균 성공률 계산
            //      (첫 번째 기록의 student에서 teacher를 꺼낸 뒤 해당 teacher의 id로 계산)
            Double avgSuccess = gameHistoryRepository.calculateClassAverageByTeacherId(
                    histories.get(0).getStudent().getTeacher().getId()
            );

            // 2-2. 평균 성공률이 null이면(기록이 없거나 계산 실패 시) 빈 통계 반환
            if (avgSuccess == null) {
                return createEmptyStatistics();
            }

            // 2-3. 전체 게임 수
            int totalGames = histories.size();
            // 2-4. 평균 시도 횟수
            double averageAttempts = calculateAverageAttempts(histories);

            // 2-5. 단어별 기록을 그룹화하여 성능(성공률)을 분석하기 위한 자료 구조 생성
            Map<Word, List<GameHistory>> wordHistories = histories.stream()
                    .collect(Collectors.groupingBy(GameHistory::getWord));

            // 2-6. 가장 성공률이 높은 단어와 낮은 단어 찾기
            String bestWord = findBestPerformingWord(wordHistories);
            String worstWord = findWorstPerformingWord(wordHistories);

            // 2-7. 가장 많이 틀린 알파벳 찾기
            String mostMissedLetters = findMostMissedLetters(histories);

            // 2-8. 날짜별 기록을 그룹화하여 일별 통계를 계산하기 위한 자료 구조
            Map<LocalDate, List<GameHistory>> dailyStats = histories.stream()
                    .collect(Collectors.groupingBy(h -> h.getPlayedAt().toLocalDate()));

            // 2-9. 시간대별 분포 계산
            Map<String, Integer> timeDistribution = calculateTimeDistribution(histories);
            // 2-10. 날짜별 승률(진행 추이) 계산
            Map<String, Double> progressTrend = calculateProgressTrend(dailyStats);

            // 2-11. 통계 정보를 빌더 패턴으로 구성하여 반환
            return GameStatisticsResponse.builder()
                    .totalGames(totalGames)                            // 전체 게임 수
                    .gamesWon((int)(totalGames * avgSuccess))         // 평균 성공률로 승리 수 대략 추정
                    .gamesLost((int)(totalGames * (1 - avgSuccess)))  // 패배 수
                    .winRate(avgSuccess * 100)                        // 승률(백분율)
                    .averageAttempts(averageAttempts)                 // 평균 시도 횟수
                    .mostMissedLetters(mostMissedLetters)             // 가장 많이 틀린 알파벳
                    .bestPerformingWord(bestWord)                     // 성공률 최고 단어
                    .worstPerformingWord(worstWord)                   // 성공률 최저 단어
                    .timeDistribution(timeDistribution)               // 시간대별 분포
                    .progressTrend(progressTrend)                     // 일자별 승률 추이
                    .build();
        }

        // 3. 관리자(ADMIN)나 학생(USER)인 경우 기존 방식으로 계산
        // 3-1. 전체 게임 수
        int totalGames = histories.size();
        // 3-2. 이긴 게임 수
        int gamesWon = (int) histories.stream()
                .filter(GameHistory::getIsSuccess)                   // 성공한 기록만 필터
                .count();
        // 3-3. 진 게임 수
        int gamesLost = totalGames - gamesWon;
        // 3-4. 승률(백분율)
        double winRate = calculateWinRate(histories);
        // 3-5. 평균 시도 횟수
        double averageAttempts = calculateAverageAttempts(histories);

        // 3-6. 단어별 기록을 그룹화하여 성능 분석
        Map<Word, List<GameHistory>> wordHistories = histories.stream()
                .collect(Collectors.groupingBy(GameHistory::getWord));

        // 3-7. 가장 잘 맞춘 단어, 가장 못 맞춘 단어
        String bestWord = findBestPerformingWord(wordHistories);
        String worstWord = findWorstPerformingWord(wordHistories);

        // 3-8. 가장 많이 틀린 알파벳
        String mostMissedLetters = findMostMissedLetters(histories);

        // 3-9. 날짜별 기록 그룹화
        Map<LocalDate, List<GameHistory>> dailyStats = histories.stream()
                .collect(Collectors.groupingBy(h -> h.getPlayedAt().toLocalDate()));

        // 3-10. 시간대별 분포
        Map<String, Integer> timeDistribution = calculateTimeDistribution(histories);
        // 3-11. 날짜별 승률(진행 추이)
        Map<String, Double> progressTrend = calculateProgressTrend(dailyStats);

        // 3-12. 통계 DTO 생성 후 반환
        return GameStatisticsResponse.builder()
                .totalGames(totalGames)                 // 전체 게임 수
                .gamesWon(gamesWon)                     // 승리 횟수
                .gamesLost(gamesLost)                   // 패배 횟수
                .winRate(winRate)                       // 승률(백분율)
                .averageAttempts(averageAttempts)        // 평균 시도 횟수
                .mostMissedLetters(mostMissedLetters)    // 가장 많이 틀린 알파벳
                .bestPerformingWord(bestWord)            // 성공률 최고 단어
                .worstPerformingWord(worstWord)          // 성공률 최저 단어
                .timeDistribution(timeDistribution)      // 시간대별 분포
                .progressTrend(progressTrend)            // 일자별 승률 추이
                .build();
    }

    /**
     * 주어진 게임 기록 목록에서 승률(백분율)을 계산합니다.
     * @param histories 게임 기록 리스트
     * @return 승률(백분율)
     */
    private double calculateWinRate(List<GameHistory> histories) {
        // 기록이 비어있으면 0
        if (histories.isEmpty()) return 0.0;
        // (성공한 게임 수 / 전체 게임 수) * 100
        long wins = histories.stream()
                .filter(GameHistory::getIsSuccess)   // 성공한 기록
                .count();
        return (double) wins / histories.size() * 100;
    }

    /**
     * 주어진 게임 기록 목록에서 평균 시도 횟수를 계산합니다.
     * @param histories 게임 기록 리스트
     * @return 평균 시도 횟수
     */
    private double calculateAverageAttempts(List<GameHistory> histories) {
        // 기록이 비어있으면 0
        if (histories.isEmpty()) return 0.0;
        // 각 기록의 attempts를 모아 평균값 계산
        return histories.stream()
                .mapToInt(GameHistory::getAttempts)
                .average()
                .orElse(0.0);
    }

    /**
     * 주어진 게임 기록들에서, 가장 많이 틀린 알파벳 3개를 추출합니다.
     * @param histories 게임 기록 리스트
     * @return 콤마로 구분된 알파벳 문자열 (예: "A,B,C")
     */
    private String findMostMissedLetters(List<GameHistory> histories) {
        // 알파벳별 누적 틀린 횟수를 저장할 Map
        Map<Character, Integer> missCount = new HashMap<>();

        // 1. 각 기록의 wrongLetters 필드를 가져옴 (예: "A,B,C")
        histories.stream()
                .map(GameHistory::getWrongLetters)
                .filter(Objects::nonNull) // null 체크
                .forEach(letters -> {
                    // 2. 콤마로 스플릿하여 개별 알파벳 처리
                    for (String letter : letters.split(",")) {
                        if (!letter.isEmpty()) {
                            // 3. Map에 카운트 누적
                            missCount.merge(letter.charAt(0), 1, Integer::sum);
                        }
                    }
                });

        // 4. missCount Map을 값(틀린 횟수) 기준으로 내림차순 정렬, 상위 3개 추출
        return missCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))  // 값 기준 내림차순
                .limit(3)
                .map(e -> String.valueOf(e.getKey())) // Character -> String
                .collect(Collectors.joining(","));
    }

    /**
     * 단어별 성공률을 비교하여, 가장 성공률이 높은 단어를 찾습니다.
     * @param wordHistories 단어를 Key, 해당 단어에 대한 게임 기록 목록을 Value로 하는 Map
     * @return 가장 성공률이 높은 단어 문자열
     */
    private String findBestPerformingWord(Map<Word, List<GameHistory>> wordHistories) {
        // 1. 각 단어별 성공률 계산 후, 가장 높은 값 찾기
        return wordHistories.entrySet().stream()
                .map(entry -> Map.entry(
                        entry.getKey(),
                        calculateWordSuccessRate(entry.getValue())
                ))
                .max(Map.Entry.comparingByValue())  // 성공률이 가장 높은 항목
                .map(entry -> entry.getKey().getWord()) // Word 객체에서 실제 단어 문자열 추출
                .orElse("");
    }

    /**
     * 단어별 성공률을 비교하여, 가장 성공률이 낮은 단어를 찾습니다.
     * @param wordHistories 단어를 Key, 해당 단어에 대한 게임 기록 목록을 Value로 하는 Map
     * @return 가장 성공률이 낮은 단어 문자열
     */
    private String findWorstPerformingWord(Map<Word, List<GameHistory>> wordHistories) {
        // 1. 각 단어별 성공률 계산 후, 가장 낮은 값 찾기
        return wordHistories.entrySet().stream()
                .map(entry -> Map.entry(
                        entry.getKey(),
                        calculateWordSuccessRate(entry.getValue())
                ))
                .min(Map.Entry.comparingByValue())  // 성공률이 가장 낮은 항목
                .map(entry -> entry.getKey().getWord()) // Word 객체에서 실제 단어 문자열 추출
                .orElse("");
    }

    /**
     * 주어진 게임 기록 목록의 성공률(0.0~1.0)을 계산합니다.
     * @param histories 특정 단어에 대한 게임 기록 리스트
     * @return 해당 단어의 성공률 (성공 게임 수 / 전체 게임 수)
     */
    private double calculateWordSuccessRate(List<GameHistory> histories) {
        // 기록이 없으면 성공률 0
        if (histories.isEmpty()) return 0.0;
        // (성공한 게임 수) / (전체 게임 수)
        return histories.stream()
                .filter(GameHistory::getIsSuccess)
                .count() / (double) histories.size();
    }

    /**
     * 시간대별 게임 횟수 분포를 계산합니다.
     * (예: "09:00"에 플레이된 게임이 5개면 {"09:00" -> 5})
     *
     * @param histories 게임 기록 리스트
     * @return 시간대(시 단위)를 Key, 해당 시간대 플레이 횟수를 Value로 하는 Map
     */
    private Map<String, Integer> calculateTimeDistribution(List<GameHistory> histories) {
        // playedAt 필드의 시간(시 단위)을 "HH:00" 형식으로 묶어서 카운팅
        return histories.stream()
                .collect(Collectors.groupingBy(
                        h -> String.format("%02d:00", h.getPlayedAt().getHour()),  // 예: 9시 -> "09:00"
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    /**
     * 날짜별 승률(진행 추이)을 계산합니다.
     * (예: "2024-03-15" -> 75.0% 승률)
     *
     * @param dailyStats 날짜(LocalDate)를 Key, 해당 날짜의 게임 기록 목록을 Value로 하는 Map
     * @return 날짜 문자열을 Key, 해당 날짜의 승률(백분율)을 Value로 하는 Map
     */
    private Map<String, Double> calculateProgressTrend(Map<LocalDate, List<GameHistory>> dailyStats) {
        // 1. 날짜(LocalDate) 기준으로 정렬하여 순차적으로 Map 생성
        return dailyStats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())  // 날짜 오름차순 정렬
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),   // LocalDate -> "yyyy-MM-dd" 문자열
                        entry -> calculateWinRate(entry.getValue()),  // 그 날짜에 대한 승률(백분율)
                        (e1, e2) -> e1,                       // 충돌 발생 시 기존 값 유지
                        LinkedHashMap::new                     // 순서가 유지되는 LinkedHashMap 사용
                ));
    }

    /**
     * 빈 통계 객체를 생성합니다.
     * 게임 기록이 없을 때 사용됩니다.
     *
     * @return 모든 값이 0 또는 빈 문자열로 초기화된 GameStatisticsResponse
     */
    public GameStatisticsResponse createEmptyStatistics() {
        // 빌더 패턴 사용: 각 필드를 0 혹은 빈 값으로 초기화
        return GameStatisticsResponse.builder()
                .totalGames(0)                  // 전체 게임 수
                .gamesWon(0)                    // 이긴 게임 수
                .gamesLost(0)                   // 진 게임 수
                .winRate(0.0)                   // 승률
                .averageAttempts(0.0)           // 평균 시도 횟수
                .mostMissedLetters("")          // 가장 많이 틀린 알파벳
                .bestPerformingWord("")         // 성공률 최고 단어
                .worstPerformingWord("")        // 성공률 최저 단어
                .timeDistribution(new HashMap<>()) // 시간대별 분포 (빈 맵)
                .progressTrend(new HashMap<>())     // 진행 추이 (빈 맵)
                .build();
    }
}
