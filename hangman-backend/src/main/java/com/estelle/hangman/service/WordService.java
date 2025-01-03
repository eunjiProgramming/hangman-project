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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordService {
    private final WordRepository wordRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public List<WordCategoryResponse> getAllCategories() {
        List<Word> allWords = wordRepository.findAll();

        Map<String, List<Word>> wordsByCategory = allWords.stream()
                .collect(Collectors.groupingBy(Word::getCategory));

        return wordsByCategory.entrySet().stream()
                .map(entry -> {
                    List<Word> words = entry.getValue();
                    double avgDifficulty = words.stream()
                            .mapToInt(Word::getDifficulty)
                            .average()
                            .orElse(0.0);

                    return WordCategoryResponse.builder()
                            .category(entry.getKey())
                            .wordCount(words.size())
                            .averageDifficulty(avgDifficulty)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // WordWithCategoryResponse를 WordResponse로 변경
    public List<WordResponse> getWordsByCategory(
            String category, Integer difficulty, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Word> words;
        if (user.getRole() == Role.ADMIN) {
            words = wordRepository.findByCategoryAndDifficulty(category, difficulty);
        } else if (user.getRole() == Role.MANAGER) {
            words = wordRepository.findByTeacherIdAndCategory(user.getId(), category);
        } else {
            words = wordRepository.findByCourseIdAndCategory(user.getCourse().getId(), category);
        }

        return words.stream()
                .map(this::convertToWordResponse)  // convertToWordWithCategoryResponse 대신 convertToWordResponse 사용
                .collect(Collectors.toList());
    }

    public List<WordCategoryResponse> getCategoriesByTeacher(String username) {
        User teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if (teacher.getRole() != Role.MANAGER) {
            throw new RuntimeException("User is not a teacher");
        }

        List<Word> teacherWords = wordRepository.findByTeacherId(teacher.getId());

        Map<String, List<Word>> wordsByCategory = teacherWords.stream()
                .collect(Collectors.groupingBy(Word::getCategory));

        return wordsByCategory.entrySet().stream()
                .map(entry -> WordCategoryResponse.builder()
                        .category(entry.getKey())
                        .wordCount(entry.getValue().size())
                        .averageDifficulty(calculateAverageDifficulty(entry.getValue()))
                        .build())
                .collect(Collectors.toList());
    }

    // convertToWordWithCategoryResponse 메소드 삭제

    private double calculateAverageDifficulty(List<Word> words) {
        return words.stream()
                .mapToInt(Word::getDifficulty)
                .average()
                .orElse(0.0);
    }

    @Transactional
    public WordResponse createWord(WordCreateRequest request) {
        Word word = new Word();
        word.setWord(request.getWord().toUpperCase());
        word.setCategory(request.getCategory());
        word.setDifficulty(request.getDifficulty());

        word.setTeacher(userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("선생님을 찾을 수 없습니다")));

        word.setCourse(courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("반을 찾을 수 없습니다")));

        return convertToWordResponse(wordRepository.save(word));
    }

    public WordResponse getWord(Long id) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Word not found"));
        return convertToWordResponse(word);
    }

    @Transactional
    public void deleteWord(Long id) {
        wordRepository.deleteById(id);
    }

    private WordResponse convertToWordResponse(Word word) {
        return WordResponse.builder()
                .id(word.getId())
                .word(word.getWord())
                .category(word.getCategory())
                .difficulty(word.getDifficulty())
                .courseName(word.getCourse().getName())
                .teacherName(word.getTeacher().getUsername())
                .createdAt(word.getCreatedAt())
                .updatedAt(word.getUpdatedAt())
                .build();
    }

    @Transactional
    public WordResponse updateWord(Long id, WordUpdateRequest request) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Word not found"));

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

    public List<WordResponse> searchWords(
            String keyword, String category, Integer difficulty) {
        List<Word> words = wordRepository.searchWords(keyword, category, difficulty);
        return words.stream()
                .map(this::convertToWordResponse)
                .collect(Collectors.toList());
    }
}