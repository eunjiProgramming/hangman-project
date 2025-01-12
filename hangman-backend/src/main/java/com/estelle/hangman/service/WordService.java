package com.estelle.hangman.service;

import com.estelle.hangman.domain.Role;
import com.estelle.hangman.domain.User;
import com.estelle.hangman.domain.Word;
import com.estelle.hangman.dto.*;
import com.estelle.hangman.repository.CourseRepository;
import com.estelle.hangman.repository.UserRepository;
import com.estelle.hangman.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 단어 관리를 담당하는 서비스 클래스
 * 초등학생 영어 학습을 위한 단어들을 관리하며,
 * 난이도별, 카테고리별로 단어를 분류하고 관리합니다.
 */
@Service  // 스프링의 서비스 계층 컴포넌트임을 표시
@RequiredArgsConstructor  // final 필드를 위한 생성자를 자동으로 만들어줌
@Transactional(readOnly = true)  // 기본적으로 모든 메서드는 읽기 전용으로 설정
public class WordService {

    // 필요한 리포지토리들을 주입받음
    private final WordRepository wordRepository;        // 단어 저장소
    private final UserRepository userRepository;        // 사용자 저장소
    private final CourseRepository courseRepository;    // 반 저장소

    /**
     * 시스템에 등록된 모든 단어 카테고리와 통계 정보를 조회합니다.
     * 각 카테고리별 단어 수와 평균 난이도를 계산합니다.
     */
    public List<WordCategoryResponse> getAllCategories() {
        // 모든 단어를 가져옴
        List<Word> allWords = wordRepository.findAll();

        // 카테고리별로 단어들을 그룹화
        Map<String, List<Word>> wordsByCategory = allWords.stream()
                .collect(Collectors.groupingBy(Word::getCategory));

        // 각 카테고리별로 통계 정보를 생성
        return wordsByCategory.entrySet().stream()
                .map(entry -> {
                    List<Word> words = entry.getValue();
                    // 카테고리의 평균 난이도 계산
                    double avgDifficulty = words.stream()
                            .mapToInt(Word::getDifficulty)
                            .average()
                            .orElse(0.0);

                    // 응답 객체 생성
                    return WordCategoryResponse.builder()
                            .category(entry.getKey())
                            .wordCount(words.size())
                            .averageDifficulty(avgDifficulty)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 카테고리와 난이도에 해당하는 단어들을 조회합니다.
     * 사용자의 역할(관리자/선생님/학생)에 따라 접근 가능한 단어가 다릅니다.
     */
    public List<WordResponse> getWordsByCategory(
            String category, Integer difficulty, String username) {

        // 현재 사용자 정보 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Word> words;
        // 사용자 역할에 따라 다른 단어 목록 제공
        if (user.getRole() == Role.ADMIN) {
            // 관리자는 모든 단어 접근 가능
            words = wordRepository.findByCategoryAndDifficulty(category, difficulty);
        } else if (user.getRole() == Role.MANAGER) {
            // 선생님은 자신이 등록한 단어만 접근 가능
            words = wordRepository.findByTeacherIdAndCategory(user.getId(), category);
        } else {
            // 학생은 자신의 반의 단어만 접근 가능
            words = wordRepository.findByCourseIdAndCategory(user.getCourse().getId(), category);
        }

        // 단어 목록을 응답 형식으로 변환
        return words.stream()
                .map(this::convertToWordResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 선생님이 관리하는 단어 카테고리 목록을 조회합니다.
     * 각 카테고리별 통계 정보도 함께 제공합니다.
     */
    public List<WordCategoryResponse> getCategoriesByTeacher(String username) {
        // 교사 정보 확인
        User teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // 실제로 교사인지 확인
        if (teacher.getRole() != Role.MANAGER) {
            throw new RuntimeException("User is not a teacher");
        }

        // 교사가 등록한 단어들 가져오기
        List<Word> teacherWords = wordRepository.findByTeacherId(teacher.getId());

        // 카테고리별로 단어 그룹화
        Map<String, List<Word>> wordsByCategory = teacherWords.stream()
                .collect(Collectors.groupingBy(Word::getCategory));

        // 카테고리별 통계 정보 생성
        return wordsByCategory.entrySet().stream()
                .map(entry -> WordCategoryResponse.builder()
                        .category(entry.getKey())
                        .wordCount(entry.getValue().size())
                        .averageDifficulty(calculateAverageDifficulty(entry.getValue()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 새로운 단어를 생성합니다.
     * 단어는 항상 대문자로 저장되며, 난이도와 카테고리 정보를 포함합니다.
     */
    @Transactional  // 데이터를 변경하는 작업이므로 트랜잭션 시작
    public WordResponse createWord(WordCreateRequest request) {
        Word word = new Word();
        // 단어는 항상 대문자로 저장 (일관성을 위해)
        word.setWord(request.getWord().toUpperCase());
        word.setCategory(request.getCategory());
        word.setDifficulty(request.getDifficulty());

        // 교사 정보 설정
        word.setTeacher(userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("선생님을 찾을 수 없습니다")));

        // 반 정보 설정
        word.setCourse(courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("반을 찾을 수 없습니다")));

        // 저장하고 응답 형식으로 변환하여 반환
        return convertToWordResponse(wordRepository.save(word));
    }

    /**
     * 기존 단어의 정보를 수정합니다.
     * 요청에 포함된 필드만 선택적으로 업데이트됩니다.
     */
    @Transactional
    public WordResponse updateWord(Long id, WordUpdateRequest request) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Word not found"));

        // 제공된 정보만 선택적으로 업데이트
        if (request.getWord() != null) {
            word.setWord(request.getWord().toUpperCase());
        }
        if (request.getCategory() != null) {
            word.setCategory(request.getCategory());
        }
        if (request.getDifficulty() != null) {
            word.setDifficulty(request.getDifficulty());
        }

        return convertToWordResponse(word);
    }

    /**
     * 특정 단어를 삭제합니다.
     */
    @Transactional
    public void deleteWord(Long id) {
        wordRepository.deleteById(id);
    }

    /**
     * 단어를 검색합니다.
     * 키워드, 카테고리, 난이도를 기준으로 검색할 수 있습니다.
     */
    public List<WordResponse> searchWords(
            String keyword, String category, Integer difficulty) {
        List<Word> words = wordRepository.searchWords(keyword, category, difficulty);
        return words.stream()
                .map(this::convertToWordResponse)
                .collect(Collectors.toList());
    }

    /**
     * 단어 목록의 평균 난이도를 계산하는 유틸리티 메서드
     */
    private double calculateAverageDifficulty(List<Word> words) {
        return words.stream()
                .mapToInt(Word::getDifficulty)
                .average()
                .orElse(0.0);
    }

    /**
     * Word 엔티티를 WordResponse DTO로 변환하는 유틸리티 메서드
     */
    private WordResponse convertToWordResponse(Word word) {
        return WordResponse.builder()
                .id(word.getId())                    // 단어의 고유 ID
                .word(word.getWord())                // 단어
                .category(word.getCategory())        // 카테고리
                .difficulty(word.getDifficulty())    // 난이도
                .courseName(word.getCourse().getName())   // 반 이름
                .teacherName(word.getTeacher().getUsername())  // 선생님 이름
                .createdAt(word.getCreatedAt())      // 생성 시간
                .updatedAt(word.getUpdatedAt())      // 수정 시간
                .build();
    }
}