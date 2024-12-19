package com.estelle.hangman.domain;

import java.util.HashSet;
import java.util.Set;

public class GameSession {
    private final Word word;  // 현재 게임에서 맞춰야 할 단어 (변경불가능하도록 final로 설정)

    private final Set<Character> guessedLetters = new HashSet<>();
    // 지금까지 시도한 모든 알파벳들을 저장하는 Set (중복 알파벳 방지)

    private final Set<Character> wrongLetters = new HashSet<>();
    // 틀린 알파벳들만 따로 저장하는 Set (중복 방지)

    private final int maxAttempts = 10;
    // 최대 시도 가능 횟수 (10번으로 고정)

    private boolean isComplete = false;
    // 게임이 끝났는지 여부 (성공이든 실패든)

    private boolean isSuccess = false;
    // 게임을 성공했는지 여부

    public GameSession(Word word) {
        this.word = word;  // 게임 시작할 때 단어를 받아서 저장
    }

    public String getMaskedWord() {
        StringBuilder masked = new StringBuilder();
        for (char c : word.getWord().toCharArray()) {  // 단어의 각 글자를 하나씩 확인
            if (guessedLetters.contains(Character.toUpperCase(c))) {  // 이미 맞춘 글자라면
                masked.append(c);  // 그 글자를 보여줌
            } else {
                masked.append("_");  // 아직 못 맞춘 글자는 "_"로 표시
            }
            masked.append(" ");  // 글자 사이에 공백 추가
        }
        return masked.toString().trim();  // 마지막 공백 제거하고 반환
    }

    public boolean guessLetter(char letter) {
        letter = Character.toUpperCase(letter);  // 입력된 알파벳을 대문자로 변환

        if (guessedLetters.contains(letter)) {  // 이미 시도했던 알파벳이면
            return false;  // false 반환 (중복 시도)
        }

        guessedLetters.add(letter);  // 시도한 알파벳 목록에 추가

        if (!word.getWord().toUpperCase().contains(String.valueOf(letter))) {  // 단어에 그 알파벳이 없으면
            wrongLetters.add(letter);  // 틀린 알파벳 목록에 추가
        }

        checkGameStatus();  // 게임 상태 확인 (끝났는지, 성공했는지 등)
        return true;  // 정상적으로 처리됐다는 의미로 true 반환
    }

    public void forfeit() {  // 게임 포기 메소드
        isComplete = true;   // 게임 종료 표시
        isSuccess = false;   // 실패로 처리
    }

    private void checkGameStatus() {  // 게임 상태 확인 메소드
        if (wrongLetters.size() >= maxAttempts) {  // 틀린 횟수가 최대 시도 횟수(10번)를 넘으면
            isComplete = true;  // 게임 종료
            return;
        }

        // 단어의 모든 알파벳을 맞췄는지 확인
        isSuccess = word.getWord().toUpperCase().chars()  // 단어를 대문자로 변환하고 한 글자씩
                .mapToObj(c -> (char) c)  // char로 변환
                .allMatch(guessedLetters::contains);  // 모든 글자가 맞춰졌는지 확인

        isComplete = isSuccess;  // 모두 맞췄으면 게임 종료
    }

    // Getters
    public Word getWord() { return word; }  // 현재 게임의 단어 반환
    public Set<Character> getGuessedLetters() { return guessedLetters; }  // 시도한 모든 알파벳 반환
    public Set<Character> getWrongLetters() { return wrongLetters; }  // 틀린 알파벳들 반환
    public int getMaxAttempts() { return maxAttempts; }  // 최대 시도 가능 횟수 반환
    public boolean isComplete() { return isComplete; }  // 게임 종료 여부 반환
    public boolean isSuccess() { return isSuccess; }  // 게임 성공 여부 반환
}