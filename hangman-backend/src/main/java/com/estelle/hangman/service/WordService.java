package com.estelle.hangman.service;

import com.estelle.hangman.domain.Role;
import com.estelle.hangman.domain.User;
import com.estelle.hangman.domain.Word;
import com.estelle.hangman.dto.WordCategoryResponse;
import com.estelle.hangman.dto.WordResponse;
import com.estelle.hangman.dto.WordWithCategoryResponse;
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

    public List<WordWithCategoryResponse> getWordsByCategory(
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
                .map(this::convertToWordWithCategoryResponse)
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

    private WordWithCategoryResponse convertToWordWithCategoryResponse(Word word) {
        return WordWithCategoryResponse.builder()
                .id(word.getId())
                .word(word.getWord())
                .category(word.getCategory())
                .difficulty(word.getDifficulty())
                .courseName(word.getCourse().getName())
                .teacherName(word.getTeacher().getUsername())
                .build();
    }

    private double calculateAverageDifficulty(List<Word> words) {
        return words.stream()
                .mapToInt(Word::getDifficulty)
                .average()
                .orElse(0.0);
    }

    // 기본적인 CRUD 메서드들도 추가
    @Transactional
    public WordResponse createWord(Word word) {
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
                .build();
    }
}