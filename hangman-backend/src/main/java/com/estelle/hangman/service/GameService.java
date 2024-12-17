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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final WordRepository wordRepository;
    private final UserRepository userRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final CourseRepository courseRepository;
    private final Map<Long, GameSession> gameSessions = new HashMap<>();

    @Transactional
    public GameStartResponse startGame(String username, GameStartRequest request) {
        User user = getUserAndValidate(username);
        Word word = selectRandomWord(user, request);

        GameSession session = new GameSession(word);
        Long gameId = generateGameId();
        gameSessions.put(gameId, session);

        return GameStartResponse.builder()
                .gameId(gameId)
                .wordLength(word.getWord().length())
                .maskedWord(session.getMaskedWord())
                .maxAttempts(session.getMaxAttempts())
                .remainingAttempts(session.getMaxAttempts() - session.getWrongLetters().size())
                .build();
    }

    @Transactional
    public GameGuessResponse guessLetter(String username, GameGuessRequest request) {
        validateGameAccess(username, request.getGameId());
        GameSession session = getGameSession(request.getGameId());

        if (session.isComplete()) {
            throw new IllegalStateException("Game is already complete");
        }

        session.guessLetter(request.getLetter());

        if (session.isComplete()) {
            saveGameHistory(username, session);
        }

        return buildGameGuessResponse(session);
    }

    public List<GameHistoryResponse> getGameHistory(String username) {
        User user = getUserAndValidate(username);
        List<GameHistory> histories = getHistoriesByUserRole(user);
        return convertToGameHistoryResponses(histories);
    }

    public List<GameHistoryResponse> getStudentGameHistory(String username, Long studentId) {
        User teacher = getUserAndValidate(username);
        validateTeacherAccess(teacher, studentId);

        return gameHistoryRepository.findAllByStudentId(studentId).stream()
                .map(this::convertToGameHistoryResponse)
                .collect(Collectors.toList());
    }

    public GameStatisticsResponse getGameStatistics(String username) {
        User user = getUserAndValidate(username);
        List<GameHistory> histories = getHistoriesByUserRole(user);
        return buildGameStatistics(histories, user.getRole());
    }

    public List<GameHistoryResponse> getStudentGameHistoryByPeriod(
            String username, Long studentId, LocalDate startDate, LocalDate endDate) {
        User teacher = getUserAndValidate(username);
        validateTeacherAccess(teacher, studentId);

        List<GameHistory> histories = gameHistoryRepository.findByStudentIdAndPlayedAtBetween(
                studentId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());

        return convertToGameHistoryResponses(histories);
    }

    public GameStatisticsResponse getClassStatistics(String username, Long courseId) {
        User teacher = getUserAndValidate(username);
        validateTeacherCourseAccess(teacher, courseId);

        List<GameHistory> histories = gameHistoryRepository.findByCourseId(courseId);
        return buildGameStatistics(histories, Role.MANAGER);
    }

    public GameGuessResponse getCurrentGameStatus(String username, Long gameId) {
        validateGameAccess(username, gameId);
        GameSession session = getGameSession(gameId);
        return buildGameGuessResponse(session);
    }

    @Transactional
    public GameGuessResponse forfeitGame(String username, Long gameId) {
        validateGameAccess(username, gameId);
        GameSession session = getGameSession(gameId);

        if (!session.isComplete()) {
            session.forfeit();
            saveGameHistory(username, session);
        }

        return buildGameGuessResponse(session);
    }

    public GameStatisticsResponse getCategoryStatistics(String username, String category) {
        User user = getUserAndValidate(username);
        List<GameHistory> histories = getHistoriesByUserRole(user)
                .stream()
                .filter(h -> category.equals(h.getWord().getCategory()))
                .collect(Collectors.toList());

        return buildGameStatistics(histories, user.getRole());
    }

    // Private helper methods
    private User getUserAndValidate(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private Word selectRandomWord(User user, GameStartRequest request) {
        List<Word> availableWords;

        if (user.getRole() == Role.ADMIN && request != null && request.getTeacherId() != null) {
            availableWords = wordRepository.findByTeacherId(request.getTeacherId());
        } else if (user.getRole() == Role.MANAGER && request != null && request.getCourseId() != null) {
            availableWords = wordRepository.findByCourseId(request.getCourseId());
        } else if (user.getRole() == Role.USER) {
            availableWords = wordRepository.findByCourseIdAndTeacherId(
                    user.getCourse().getId(), user.getTeacher().getId());
        } else {
            throw new IllegalStateException("Invalid game start request");
        }

        if (availableWords.isEmpty()) {
            throw new IllegalStateException("No words available");
        }

        Random random = new Random();
        return availableWords.get(random.nextInt(availableWords.size()));
    }

    private void validateGameAccess(String username, Long gameId) {
        User user = getUserAndValidate(username);
        GameSession session = getGameSession(gameId);

        if (user.getRole() == Role.USER && !session.getWord().getCourse().equals(user.getCourse())) {
            throw new AccessDeniedException("Not authorized to access this game");
        }
    }

    private GameSession getGameSession(Long gameId) {
        GameSession session = gameSessions.get(gameId);
        if (session == null) {
            throw new IllegalArgumentException("Game session not found");
        }
        return session;
    }

    private void validateTeacherAccess(User teacher, Long studentId) {
        if (teacher.getRole() != Role.MANAGER && teacher.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only teachers can access student histories");
        }

        if (teacher.getRole() == Role.MANAGER) {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found"));
            if (!student.getTeacher().equals(teacher)) {
                throw new AccessDeniedException("Not authorized to access this student's data");
            }
        }
    }

    private void validateTeacherCourseAccess(User teacher, Long courseId) {
        if (teacher.getRole() != Role.MANAGER && teacher.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only teachers can access class statistics");
        }

        if (teacher.getRole() == Role.MANAGER) {
            boolean hasAccess = courseRepository.existsByIdAndTeacherId(courseId, teacher.getId());
            if (!hasAccess) {
                throw new AccessDeniedException("Not authorized to access this class's statistics");
            }
        }
    }

    private Long generateGameId() {
        return System.currentTimeMillis() + new Random().nextInt(1000);
    }

    @Transactional
    protected void saveGameHistory(String username, GameSession session) {
        User user = getUserAndValidate(username);

        GameHistory history = new GameHistory();
        history.setStudent(user);
        history.setWord(session.getWord());
        history.setIsSuccess(session.isSuccess());
        history.setAttempts(session.getWrongLetters().size());
        history.setWrongLetters(String.join(",",
                session.getWrongLetters().stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList())));

        gameHistoryRepository.save(history);
    }

    private GameGuessResponse buildGameGuessResponse(GameSession session) {
        return GameGuessResponse.builder()
                .maskedWord(session.getMaskedWord())
                .remainingAttempts(session.getMaxAttempts() - session.getWrongLetters().size())
                .guessedLetters(new ArrayList<>(session.getGuessedLetters()))
                .wrongLetters(new ArrayList<>(session.getWrongLetters()))
                .isComplete(session.isComplete())
                .isSuccess(session.isSuccess())
                .build();
    }

    private List<GameHistory> getHistoriesByUserRole(User user) {
        switch (user.getRole()) {
            case ADMIN:
                return gameHistoryRepository.findAll();
            case MANAGER:
                return gameHistoryRepository.findAllByStudentTeacherId(user.getId());
            case USER:
                return gameHistoryRepository.findAllByStudentId(user.getId());
            default:
                throw new IllegalStateException("Invalid user role");
        }
    }

    private List<GameHistoryResponse> convertToGameHistoryResponses(List<GameHistory> histories) {
        return histories.stream()
                .map(this::convertToGameHistoryResponse)
                .collect(Collectors.toList());
    }

    private GameHistoryResponse convertToGameHistoryResponse(GameHistory history) {
        return GameHistoryResponse.builder()
                .id(history.getId())
                .word(history.getWord().getWord())
                .success(history.getIsSuccess())
                .attempts(history.getAttempts())
                .wrongLetters(history.getWrongLetters())
                .playedAt(history.getPlayedAt())
                .build();
    }

    private GameStatisticsResponse buildGameStatistics(List<GameHistory> histories, Role role) {
        if (histories.isEmpty()) {
            return GameStatisticsResponse.builder()
                    .totalGames(0)
                    .gamesWon(0)
                    .gamesLost(0)
                    .winRate(0.0)
                    .averageAttempts(0.0)
                    .build();
        }

        int totalGames = histories.size();
        int gamesWon = (int) histories.stream().filter(GameHistory::getIsSuccess).count();
        double winRate = (double) gamesWon / totalGames * 100;
        double avgAttempts = histories.stream()
                .mapToInt(GameHistory::getAttempts)
                .average()
                .orElse(0.0);

        return GameStatisticsResponse.builder()
                .totalGames(totalGames)
                .gamesWon(gamesWon)
                .gamesLost(totalGames - gamesWon)
                .winRate(winRate)
                .averageAttempts(avgAttempts)
                .build();
    }
}