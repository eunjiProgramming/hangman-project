package com.estelle.hangman.service;

import com.estelle.hangman.domain.*;
import com.estelle.hangman.dto.*;
import com.estelle.hangman.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 게임의 핵심 로직을 담당하는 서비스 클래스
 */
@Service                                 // 스프링 서비스 계층을 나타내는 어노테이션
@RequiredArgsConstructor                  // Lombok: final 필드의 생성자를 자동으로 생성
@Transactional(readOnly = true)          // 메서드 실행 시 기본적으로 읽기 전용 트랜잭션을 적용
public class GameService {

    /**
     * 필요한 리포지토리, 서비스 등을 주입받는 부분
     */
    private final WordRepository wordRepository;               // 단어 관련 DB 작업 인터페이스
    private final UserRepository userRepository;               // 사용자 관련 DB 작업 인터페이스
    private final GameHistoryRepository gameHistoryRepository; // 게임 기록 관련 DB 작업 인터페이스
    private final CourseRepository courseRepository;           // 반 관련 DB 작업 인터페이스
    private final GameStatisticsService gameStatisticsService; // 게임 통계 기능 제공 서비스

    /**
     * 현재 진행 중인 게임 세션 정보를 저장하는 Map
     * Key: 게임 ID (Long), Value: 실제 게임 세션 객체 (GameSession)
     */
    private final Map<Long, GameSession> gameSessions = new HashMap<>();

    /**
     * 새로운 게임을 시작하는 메서드
     *
     * @param username 게임을 시작할 사용자 이름
     * @param request  게임 시작 시 필요한 정보를 담은 DTO
     * @return GameStartResponse 새 게임 시작 정보를 담은 응답 DTO
     */
    @Transactional // 쓰기 연산(게임 시작 시 DB의 상태 변경 가능)이므로 readOnly=false가 적용됨
    public GameStartResponse startGame(String username, GameStartRequest request) {
        // 1. 사용자 검증
        User user = getUserAndValidate(username);

        // 2. 적절한 단어 선택
        Word word = selectRandomWord(user, request);

        // 3. 새로운 게임 세션 생성
        GameSession session = new GameSession(word);

        // 4. 게임 ID 생성 (임시로 현재 시간 + 랜덤 숫자 조합)
        Long gameId = generateGameId();

        // 5. Map에 게임 ID와 세션 저장
        gameSessions.put(gameId, session);

        // 6. 사용자에게 반환할 게임 시작 응답 생성
        return GameStartResponse.builder()
                .gameId(gameId)                                      // 생성된 게임 ID
                .wordLength(word.getWord().length())                 // 단어 길이
                .maskedWord(session.getMaskedWord())                 // 현재 가려진 단어 예: "_ _ _"
                .maxAttempts(session.getMaxAttempts())               // 최대 시도 횟수
                .remainingAttempts(session.getMaxAttempts() - session.getWrongLetters().size()) // 남은 시도 횟수
                .build();
    }

    /**
     * 글자 추측을 처리하는 메서드
     *
     * @param username 게임에 참여하는 사용자 이름
     * @param request  사용자가 추측한 글자를 담은 DTO
     * @return GameGuessResponse 현재 게임의 상태를 담은 응답 DTO
     */
    @Transactional // 글자 추측 시 게임 상태(DB로 기록) 변경 가능성이 있으므로 트랜잭션 적용
    public GameGuessResponse guessLetter(String username, GameGuessRequest request) {
        // 1. 사용자가 이 게임에 접근할 권한이 있는지 확인
        validateGameAccess(username, request.getGameId());

        // 2. 게임 세션 가져오기
        GameSession session = getGameSession(request.getGameId());

        // 3. 이미 끝난 게임인지 확인 (게임이 이미 complete 상태이면 예외)
        if (session.isComplete()) {
            throw new IllegalStateException("Game is already complete");
        }

        // 4. 글자 추측 로직 수행
        session.guessLetter(request.getLetter());

        // 5. 글자 추측 후 게임이 끝났다면(성공 또는 실패) 게임 기록을 DB에 저장
        if (session.isComplete()) {
            saveGameHistory(username, session);
        }

        // 6. 현재 게임 상태 응답 생성
        return buildGameGuessResponse(session);
    }

    /**
     * 게임 기록을 조회하는 메서드
     *
     * @param username 조회하려는 사용자 이름
     * @return List<GameHistoryResponse> 게임 기록 목록
     */
    public List<GameHistoryResponse> getGameHistory(String username) {
        // 1. 사용자 검증
        User user = getUserAndValidate(username);

        // 2. 사용자 역할에 따라 조회해야 할 게임 기록 목록 가져오기
        List<GameHistory> histories = getHistoriesByUserRole(user);

        // 3. GameHistory 엔티티를 Response DTO로 변환
        return convertToGameHistoryResponses(histories);
    }

    /**
     * 특정 학생의 게임 기록을 조회하는 메서드 (선생님/관리자 용)
     *
     * @param username   조회를 요청한 선생님(또는 관리자) 계정 이름
     * @param studentId  조회 대상 학생의 ID
     * @return List<GameHistoryResponse> 학생의 게임 기록 목록
     */
    public List<GameHistoryResponse> getStudentGameHistory(String username, Long studentId) {
        // 1. 요청 사용자(선생님/관리자) 검증
        User teacher = getUserAndValidate(username);

        // 2. 학생 데이터 접근 권한 검증
        validateTeacherAccess(teacher, studentId);

        // 3. 해당 학생의 게임 기록을 조회 후 DTO로 변환
        return gameHistoryRepository.findAllByStudentId(studentId).stream()
                .map(this::convertToGameHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * 게임 통계를 조회하는 메서드
     *
     * @param username 통계를 조회하는 사용자 이름
     * @return GameStatisticsResponse 게임 통계 정보
     */
    public GameStatisticsResponse getGameStatistics(String username) {
        // 1. 사용자 검증
        User user = getUserAndValidate(username);

        // 2. 사용자 역할에 따라 조회해야 할 게임 기록 목록 가져오기
        List<GameHistory> histories = getHistoriesByUserRole(user);

        // 3. 가져온 기록들을 통계를 계산하는 서비스로 전달하여 통계 생성
        return gameStatisticsService.buildGameStatistics(histories, user.getRole());
    }

    /**
     * 특정 학생의 특정 기간 게임 기록을 조회
     *
     * @param username  조회를 요청한 선생님/관리자 계정 이름
     * @param studentId 조회 대상 학생의 ID
     * @param startDate 기간 시작 날짜
     * @param endDate   기간 종료 날짜
     * @return List<GameHistoryResponse> 해당 기간의 게임 기록 목록
     */
    public List<GameHistoryResponse> getStudentGameHistoryByPeriod(
            String username, Long studentId, LocalDate startDate, LocalDate endDate) {
        // 1. 선생님/관리자 검증
        User teacher = getUserAndValidate(username);

        // 2. 학생 접근 권한 검증
        validateTeacherAccess(teacher, studentId);

        // 3. DB에서 특정 기간에 해당하는 게임 기록 조회
        List<GameHistory> histories = gameHistoryRepository.findByStudentIdAndPlayedAtBetween(
                studentId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());

        // 4. 조회된 기록들을 Response DTO로 변환
        return convertToGameHistoryResponses(histories);
    }

    /**
     * 반별 통계를 조회하는 메서드 (선생님/관리자용)
     *
     * @param username 조회를 요청한 선생님/관리자 계정 이름
     * @param courseId 조회하려는 반 ID
     * @return GameStatisticsResponse 반의 게임 통계 정보
     */
    public GameStatisticsResponse getClassStatistics(String username, Long courseId) {
        // 1. 선생님/관리자 검증
        User teacher = getUserAndValidate(username);

        // 2. 해당 반에 대한 접근 권한 검증
        validateTeacherCourseAccess(teacher, courseId);

        // 3. 해당 반의 게임 기록을 모두 조회
        List<GameHistory> histories = gameHistoryRepository.findByCourseId(courseId);

        // 4. 통계 생성
        return gameStatisticsService.buildGameStatistics(histories, Role.MANAGER);
    }

    /**
     * 현재 진행 중인 게임의 상태를 조회하는 메서드
     *
     * @param username 게임에 접근하려는 사용자 이름
     * @param gameId   조회할 게임의 ID
     * @return GameGuessResponse 현재 게임의 상태 정보
     */
    public GameGuessResponse getCurrentGameStatus(String username, Long gameId) {
        // 1. 해당 게임에 대한 접근 권한 검증
        validateGameAccess(username, gameId);

        // 2. 게임 세션 가져오기
        GameSession session = getGameSession(gameId);

        // 3. 게임 상태 응답 생성
        return buildGameGuessResponse(session);
    }

    /**
     * 게임을 포기(중도 종료)하는 메서드
     *
     * @param username 게임을 포기하려는 사용자 이름
     * @param gameId   포기할 게임의 ID
     * @return GameGuessResponse 포기 처리 후의 게임 상태 정보
     */
    @Transactional
    public GameGuessResponse forfeitGame(String username, Long gameId) {
        // 1. 게임 접근 권한 검증
        validateGameAccess(username, gameId);

        // 2. 게임 세션 가져오기
        GameSession session = getGameSession(gameId);

        // 3. 아직 게임이 끝나지 않았다면 포기 처리
        if (!session.isComplete()) {
            session.forfeit();        // 포기 시도
            saveGameHistory(username, session); // 게임 기록 저장
        }

        // 4. 최종 게임 상태 반환
        return buildGameGuessResponse(session);
    }

    /**
     * 카테고리별 통계를 조회하는 메서드
     *
     * @param username 통계를 조회하는 사용자 이름
     * @param category 조회할 카테고리
     * @return GameStatisticsResponse 해당 카테고리의 게임 통계
     */
    public GameStatisticsResponse getCategoryStatistics(String username, String category) {
        // 1. 사용자 검증
        User user = getUserAndValidate(username);

        // 2. 사용자 역할에 따라 조회해야 할 게임 기록을 불러온 뒤, 해당 카테고리만 필터링
        List<GameHistory> histories = getHistoriesByUserRole(user).stream()
                .filter(h -> category.equals(h.getWord().getCategory())) // 카테고리 일치 여부
                .collect(Collectors.toList());

        // 3. 필터링된 기록으로 통계 생성
        return gameStatisticsService.buildGameStatistics(histories, user.getRole());
    }

    /**
     * 사용자 정보를 가져오고 검증하는 private 메서드
     *
     * @param username 사용자 이름
     * @return User 엔티티
     */
    private User getUserAndValidate(String username) {
        // 1. username으로 사용자 조회
        return userRepository.findByUsername(username)
                // 2. 없으면 예외 던지기
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * 게임에 사용할 랜덤 단어를 선택하는 메서드
     *
     * @param user    현재 게임을 시작하는 사용자
     * @param request 게임 시작 시 함께 들어온 요청 정보
     * @return Word 선택된 단어 엔티티
     */
    private Word selectRandomWord(User user, GameStartRequest request) {
        // 1. 사용자 역할과 요청 정보를 바탕으로 가져올 단어 목록 결정
        List<Word> availableWords;

        if (user.getRole() == Role.ADMIN && request != null && request.getTeacherId() != null) {
            // 관리자: 특정 teacherId가 주어지면, 해당 선생님의 단어만 조회
            availableWords = wordRepository.findByTeacherId(request.getTeacherId());
        } else if (user.getRole() == Role.MANAGER && request != null && request.getCourseId() != null) {
            // 선생님: 특정 courseId가 주어지면, 해당 반의 단어만 조회
            availableWords = wordRepository.findByCourseId(request.getCourseId());
        } else if (user.getRole() == Role.USER) {
            // 학생: 자신의 반과 선생님의 단어만 조회
            availableWords = wordRepository.findByCourseIdAndTeacherId(
                    user.getCourse().getId(), user.getTeacher().getId());
        } else {
            // 그 외에는 적절한 요청이 아님
            throw new IllegalStateException("Invalid game start request");
        }

        // 2. 사용 가능한 단어가 없으면 예외
        if (availableWords.isEmpty()) {
            throw new IllegalStateException("No words available");
        }

        // 3. 랜덤하게 하나를 선택
        Random random = new Random();
        return availableWords.get(random.nextInt(availableWords.size()));
    }

    /**
     * 게임 접근 권한을 검증하는 메서드
     *
     * @param username 게임에 접근하려는 사용자 이름
     * @param gameId   접근하려는 게임 ID
     */
    private void validateGameAccess(String username, Long gameId) {
        // 1. 사용자 엔티티 조회
        User user = getUserAndValidate(username);

        // 2. 게임 세션 가져오기
        GameSession session = getGameSession(gameId);

        // 3. 학생(USER)인 경우, 자신의 반에 속한 단어인지 확인
        //    (여기서는 게임 세션에 담긴 word의 course와 사용자의 course가 같아야 접근 가능)
        if (user.getRole() == Role.USER && !session.getWord().getCourse().equals(user.getCourse())) {
            throw new AccessDeniedException("Not authorized to access this game");
        }
        // 4. 선생님(MANAGER)이나 관리자(ADMIN)는 여기서 별도 검증 로직을 추가하거나
        //    필요 시 이미 다른 곳에서 검증을 수행할 수 있음
    }

    /**
     * 게임 세션을 가져오는 메서드
     *
     * @param gameId 게임 ID
     * @return GameSession 해당 ID에 대응하는 게임 세션
     */
    private GameSession getGameSession(Long gameId) {
        // 1. gameSessions에서 gameId에 해당하는 세션을 꺼냄
        GameSession session = gameSessions.get(gameId);

        // 2. 세션이 존재하지 않으면 예외
        if (session == null) {
            throw new IllegalArgumentException("Game session not found");
        }
        return session;
    }

    /**
     * 선생님의 학생 접근 권한을 검증하는 메서드
     *
     * @param teacher   선생님(또는 관리자) 유저 엔티티
     * @param studentId 조회하려는 학생의 ID
     */
    private void validateTeacherAccess(User teacher, Long studentId) {
        // 1. 관리자(ADMIN) 또는 선생님(MANAGER)만 접근 가능
        if (teacher.getRole() != Role.MANAGER && teacher.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only teachers can access student histories");
        }

        // 2. 만약 선생님(MANAGER)이라면, 자신의 학생만 접근 가능해야 함
        if (teacher.getRole() == Role.MANAGER) {
            // studentId를 가진 유저 조회
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found"));

            // 3. 조회한 학생의 teacher와 현재 선생님이 같은지 확인
            if (!student.getTeacher().equals(teacher)) {
                throw new AccessDeniedException("Not authorized to access this student's data");
            }
        }
    }

    /**
     * 선생님의 반 접근 권한을 검증하는 메서드
     *
     * @param teacher  선생님(또는 관리자) 유저 엔티티
     * @param courseId 조회하려는 반 ID
     */
    private void validateTeacherCourseAccess(User teacher, Long courseId) {
        // 1. 관리자(ADMIN) 또는 선생님(MANAGER)만 접근 가능
        if (teacher.getRole() != Role.MANAGER && teacher.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only teachers can access class statistics");
        }

        // 2. 선생님(MANAGER)라면, 자신의 반(course)만 접근 가능
        if (teacher.getRole() == Role.MANAGER) {
            // courseRepository에서 teacherId와 courseId가 일치하는 반이 있는지 확인
            boolean hasAccess = courseRepository.existsByIdAndTeacherId(courseId, teacher.getId());
            if (!hasAccess) {
                throw new AccessDeniedException("Not authorized to access this class's statistics");
            }
        }
    }

    /**
     * 임의의 게임 ID를 생성하는 private 메서드
     *
     * @return Long 생성된 게임 ID
     */
    private Long generateGameId() {
        // 현재 시간을 밀리초 단위로 가져오고, 거기에 0~999 사이 랜덤 숫자를 더해 유니크한 ID 생성
        return System.currentTimeMillis() + new Random().nextInt(1000);
    }

    /**
     * 게임 기록을 DB에 저장하는 private 메서드
     *
     * @param username  게임을 진행한 사용자 이름
     * @param session   게임 세션 객체
     */
    @Transactional
    protected void saveGameHistory(String username, GameSession session) {
        // 1. 사용자 조회
        User user = getUserAndValidate(username);

        // 2. GameHistory 엔티티 생성 및 값 설정
        GameHistory history = new GameHistory();
        history.setStudent(user);                          // 게임을 진행한 학생
        history.setWord(session.getWord());                // 사용된 단어
        history.setIsSuccess(session.isSuccess());         // 게임 성공 여부
        history.setAttempts(session.getWrongLetters().size()); // 틀린 시도 횟수
        history.setWrongLetters(String.join(",",
                session.getWrongLetters().stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList())));   // 틀린 글자들을 문자열로 변환, 예: "A,B,C"

        // 3. DB에 저장
        gameHistoryRepository.save(history);
    }

    /**
     * GameSession 객체를 기반으로 GameGuessResponse DTO를 빌드하는 private 메서드
     *
     * @param session 현재 게임 세션
     * @return GameGuessResponse 게임 상태 응답 DTO
     */
    private GameGuessResponse buildGameGuessResponse(GameSession session) {
        // 남은 시도 횟수 = 최대 시도 횟수 - (틀린 글자 개수)
        int remainingAttempts = session.getMaxAttempts() - session.getWrongLetters().size();

        // GameGuessResponse 객체 생성 후 반환
        return GameGuessResponse.builder()
                .maskedWord(session.getMaskedWord())                 // 현재 가려진 단어
                .remainingAttempts(remainingAttempts)                // 남은 시도 횟수
                .guessedLetters(new ArrayList<>(session.getGuessedLetters())) // 추측한 글자 목록
                .wrongLetters(new ArrayList<>(session.getWrongLetters()))     // 틀린 글자 목록
                .isComplete(session.isComplete())                    // 게임 종료 여부
                .isSuccess(session.isSuccess())                      // 게임 성공 여부
                .build();
    }

    /**
     * 사용자 역할에 따라 조회해야 할 게임 기록을 가져오는 private 메서드
     *
     * @param user 조회 주체 사용자
     * @return List<GameHistory> 조회된 게임 기록 목록
     */
    private List<GameHistory> getHistoriesByUserRole(User user) {
        switch (user.getRole()) {
            case ADMIN:
                // 관리자는 모든 게임 기록을 볼 수 있음
                return gameHistoryRepository.findAll();
            case MANAGER:
                // 선생님은 자신의 학생들(teacherId가 자기 자신인)의 게임 기록을 볼 수 있음
                return gameHistoryRepository.findAllByStudentTeacherId(user.getId());
            case USER:
                // 학생은 자신의 게임 기록만 볼 수 있음
                return gameHistoryRepository.findAllByStudentId(user.getId());
            default:
                // 그 외 역할은 없다고 가정
                throw new IllegalStateException("Invalid user role");
        }
    }

    /**
     * 여러 게임 기록 엔티티를 응답 DTO로 변환하는 private 메서드
     *
     * @param histories 변환하려는 GameHistory 목록
     * @return List<GameHistoryResponse> 변환된 응답 DTO 목록
     */
    private List<GameHistoryResponse> convertToGameHistoryResponses(List<GameHistory> histories) {
        // Stream API를 사용하여 각 GameHistory를 DTO로 변환
        return histories.stream()
                .map(this::convertToGameHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * 단일 게임 기록 엔티티를 응답 DTO로 변환하는 private 메서드
     *
     * @param history 변환하려는 GameHistory 엔티티
     * @return GameHistoryResponse 변환된 응답 DTO
     */
    private GameHistoryResponse convertToGameHistoryResponse(GameHistory history) {
        return GameHistoryResponse.builder()
                .id(history.getId())                            // 게임 기록의 고유 ID
                .word(history.getWord().getWord())              // 플레이한 단어
                .success(history.getIsSuccess())                // 게임 성공 여부
                .attempts(history.getAttempts())                // 틀린 시도 횟수
                .wrongLetters(history.getWrongLetters())         // 틀린 알파벳들
                .playedAt(history.getPlayedAt())                // 게임 플레이 시간
                .build();
    }
}
