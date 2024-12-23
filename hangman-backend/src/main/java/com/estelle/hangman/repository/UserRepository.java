package com.estelle.hangman.repository;

import com.estelle.hangman.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 사용자명으로 사용자 찾기 (로그인 시 사용)
    Optional<User> findByUsername(String username);

    // 사용자명 중복 체크
    boolean existsByUsername(String username);
}