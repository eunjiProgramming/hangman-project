package com.estelle.hangman.service;

import com.estelle.hangman.domain.GameHistory;
import com.estelle.hangman.domain.User;
import com.estelle.hangman.repository.GameHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 게임 진행 기록을 관리하고 조회하는 서비스 클래스입니다.
// 이 클래스는 학생들의 게임 플레이 기록을 저장하고,
// 다양한 관점(학생별, 선생님별, 관리자)에서 기록을 조회할 수 있게 해줍니다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본적으로 조회 작업이 많으므로 읽기 전용으로 설정
public class GameHistoryService {

    // 게임 기록을 데이터베이스에서 관리하는 리포지토리입니다
    private final GameHistoryRepository gameHistoryRepository;

    // 사용자의 역할에 따라 적절한 게임 기록을 조회하는 메서드입니다
    // 관리자는 모든 기록을, 선생님은 자신의 학생들의 기록을, 학생은 자신의 기록만 볼 수 있습니다
    public List<GameHistory> getHistoriesByUser(User user) {
        // 사용자의 역할에 따라 다른 조회 방식을 사용합니다
        switch (user.getRole()) {
            case ADMIN:  // 관리자인 경우
                // 시스템의 모든 게임 기록을 조회합니다
                return gameHistoryRepository.findAll();

            case MANAGER:  // 선생님인 경우
                // 자신이 담당하는 학생들의 게임 기록만 조회합니다
                return gameHistoryRepository.findAllByStudentTeacherId(user.getId());

            case USER:  // 학생인 경우
                // 자신의 게임 기록만 조회합니다
                return gameHistoryRepository.findAllByStudentId(user.getId());

            default:
                // 알 수 없는 역할인 경우 예외를 발생시킵니다
                throw new IllegalStateException("알 수 없는 사용자 역할입니다");
        }
    }

    // 새로운 게임 기록을 저장하는 메서드입니다
    // 게임이 끝날 때마다 이 메서드가 호출되어 결과가 저장됩니다
    @Transactional  // 데이터를 변경하는 작업이므로 트랜잭션을 시작합니다
    public void saveGameHistory(GameHistory history) {
        // 게임 기록을 데이터베이스에 저장합니다
        gameHistoryRepository.save(history);
    }

    // 특정 학생의 게임 기록만 조회하는 메서드입니다
    // 주로 선생님이 특정 학생의 학습 진도를 확인할 때 사용됩니다
    public List<GameHistory> getStudentHistory(Long studentId) {
        // 지정된 학생의 모든 게임 기록을 조회합니다
        return gameHistoryRepository.findAllByStudentId(studentId);
    }

    // 특정 선생님의 모든 학생들의 게임 기록을 조회하는 메서드입니다
    // 선생님이 자신이 담당하는 전체 학생들의 진도를 파악할 때 사용됩니다
    public List<GameHistory> getTeacherStudentsHistory(Long teacherId) {
        // 지정된 선생님의 모든 학생들의 게임 기록을 조회합니다
        return gameHistoryRepository.findAllByStudentTeacherId(teacherId);
    }
}