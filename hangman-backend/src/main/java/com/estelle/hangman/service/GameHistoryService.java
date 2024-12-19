package com.estelle.hangman.service;

import com.estelle.hangman.domain.GameHistory;
import com.estelle.hangman.domain.User;
import com.estelle.hangman.repository.GameHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameHistoryService {

    private final GameHistoryRepository gameHistoryRepository;

    public List<GameHistory> getHistoriesByUser(User user) {
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

    @Transactional
    public void saveGameHistory(GameHistory history) {
        gameHistoryRepository.save(history);
    }

    public List<GameHistory> getStudentHistory(Long studentId) {
        return gameHistoryRepository.findAllByStudentId(studentId);
    }

    public List<GameHistory> getTeacherStudentsHistory(Long teacherId) {
        return gameHistoryRepository.findAllByStudentTeacherId(teacherId);
    }
}