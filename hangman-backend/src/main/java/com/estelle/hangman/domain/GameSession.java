package com.estelle.hangman.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * GameSession 클래스는 현재 진행 중인 행맨 게임의 상태를 관리합니다.
 * 각 게임 세션은 하나의 단어를 가지며, 학생이 그 단어를 맞추는 과정을 추적합니다.
 */
public class GameSession {
    /**
     * 이번 게임에서 맞춰야 할 단어입니다.
     * final로 선언되어 게임 중간에 단어가 바뀌는 것을 방지합니다.
     */
    private final Word word;

    /**
     * 학생이 지금까지 시도한 모든 알파벳을 저장하는 Set입니다.
     * HashSet을 사용하여 중복된 알파벳 시도를 방지합니다.
     * 예: [A, B, C, E] - 학생이 A, B, C, E를 시도했음을 나타냅니다.
     */
    private final Set<Character> guessedLetters = new HashSet<>();

    /**
     * 학생이 틀린 알파벳들만 따로 저장하는 Set입니다.
     * HashSet을 사용하여 중복을 방지합니다.
     * 예: [X, Y, Z] - 학생이 틀린 알파벳들입니다.
     */
    private final Set<Character> wrongLetters = new HashSet<>();

    /**
     * 최대 시도 가능 횟수입니다.
     * 10번으로 고정되어 있으며, 이는 행맨 그림의 완성 단계와 일치합니다.
     */
    private final int maxAttempts = 10;

    /**
     * 게임이 끝났는지를 나타내는 플래그입니다.
     * true: 게임 종료(성공 또는 실패), false: 게임 진행 중
     */
    private boolean isComplete = false;

    /**
     * 게임 성공 여부를 나타내는 플래그입니다.
     * true: 단어를 맞춤, false: 아직 못 맞췄거나 실패
     */
    private boolean isSuccess = false;

    /**
     * 새로운 게임 세션을 시작합니다.
     * @param word 이번 게임에서 맞춰야 할 단어
     */
    public GameSession(Word word) {
        this.word = word;
    }

    /**
     * 현재 게임의 상태를 가려진 형태로 반환합니다.
     * 맞춘 글자는 보여주고, 못 맞춘 글자는 '_'로 표시합니다.
     * 예: 단어가 "HELLO"이고 'H', 'L'을 맞췄다면 "H _ L L _" 반환
     * @return 가려진 형태의 단어 상태
     */
    public String getMaskedWord() {
        StringBuilder masked = new StringBuilder();
        // 단어의 각 글자를 하나씩 확인
        for (char c : word.getWord().toCharArray()) {
            if (guessedLetters.contains(Character.toUpperCase(c))) {
                // 이미 맞춘 글자라면 그대로 보여줌
                masked.append(c);
            } else {
                // 아직 못 맞춘 글자는 '_'로 표시
                masked.append("_");
            }
            // 글자 사이에 공백 추가 (가독성을 위해)
            masked.append(" ");
        }
        // 마지막 공백을 제거하고 결과 반환
        return masked.toString().trim();
    }

    /**
     * 학생이 추측한 알파벳을 처리합니다.
     * @param letter 학생이 추측한 알파벳
     * @return true: 정상 처리됨, false: 이미 시도한 알파벳
     */
    public boolean guessLetter(char letter) {
        // 입력된 알파벳을 대문자로 변환 (대소문자 구분 없이 처리)
        letter = Character.toUpperCase(letter);

        // 이미 시도한 알파벳인지 확인
        if (guessedLetters.contains(letter)) {
            return false;  // 중복 시도는 실패 처리
        }

        // 시도한 알파벳 목록에 추가
        guessedLetters.add(letter);

        // 단어에 이 알파벳이 없다면 틀린 목록에 추가
        if (!word.getWord().toUpperCase().contains(String.valueOf(letter))) {
            wrongLetters.add(letter);
        }

        // 게임 상태 업데이트 (종료 여부, 성공 여부 등 확인)
        checkGameStatus();
        return true;  // 정상 처리 완료
    }

    /**
     * 게임을 포기합니다.
     * 게임을 즉시 종료하고 실패로 처리합니다.
     */
    public void forfeit() {
        isComplete = true;   // 게임 종료 표시
        isSuccess = false;   // 실패로 처리
    }

    /**
     * 현재 게임 상태를 확인하고 업데이트합니다.
     * 1. 최대 시도 횟수 초과 여부 확인
     * 2. 모든 글자를 맞췄는지 확인
     */
    private void checkGameStatus() {
        // 틀린 횟수가 최대 시도 횟수를 넘으면 게임 종료
        if (wrongLetters.size() >= maxAttempts) {
            isComplete = true;
            return;
        }

        // 단어의 모든 알파벳을 맞췄는지 확인
        isSuccess = word.getWord().toUpperCase()  // 단어를 대문자로 변환
                .chars()                          // 문자열을 문자 스트림으로 변환
                .mapToObj(c -> (char) c)         // int를 char로 변환
                .allMatch(guessedLetters::contains);  // 모든 글자가 맞춰졌는지 확인

        // 성공했다면 게임 종료 표시
        isComplete = isSuccess;
    }

    // Getter 메소드들
    /**
     * @return 현재 게임의 단어
     */
    public Word getWord() { return word; }

    /**
     * @return 지금까지 시도한 모든 알파벳 Set
     */
    public Set<Character> getGuessedLetters() { return guessedLetters; }

    /**
     * @return 틀린 알파벳들의 Set
     */
    public Set<Character> getWrongLetters() { return wrongLetters; }

    /**
     * @return 최대 시도 가능 횟수 (10)
     */
    public int getMaxAttempts() { return maxAttempts; }

    /**
     * @return 게임 종료 여부
     */
    public boolean isComplete() { return isComplete; }

    /**
     * @return 게임 성공 여부
     */
    public boolean isSuccess() { return isSuccess; }
}