package com.estelle.hangman.repository;

import com.estelle.hangman.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// User 엔티티를 관리하는 리포지토리입니다.
// 사용자(관리자, 선생님, 학생)의 정보를 데이터베이스에서 관리합니다.
public interface UserRepository extends JpaRepository<User, Long> {

    // 사용자명으로 사용자를 찾는 메소드입니다.
    // Optional을 사용하여 사용자가 존재하지 않을 경우를 안전하게 처리합니다.
    // 주로 로그인 처리나 사용자 정보 조회 시 사용됩니다.
    Optional<User> findByUsername(String username);

    // 사용자명이 이미 존재하는지 확인하는 메소드입니다.
    // 새로운 사용자를 등록할 때 중복 확인용으로 사용됩니다.
    boolean existsByUsername(String username);
}