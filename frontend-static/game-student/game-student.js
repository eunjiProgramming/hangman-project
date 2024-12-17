class StudentWordExplorer {
  constructor() {
    // 현재 로그인한 학생 정보
    this.currentUser = JSON.parse(localStorage.getItem('currentUser')) || {
      id: null,
      role: 'USER',
      username: null,
      classId: null,
      teacherId: null,
    };

    // 게임 데이터
    this.gameData = {
      classes: JSON.parse(localStorage.getItem('classesData')) || [],
      teacherAssignments:
        JSON.parse(localStorage.getItem('teacherAssignments')) || [],
      wordsData: JSON.parse(localStorage.getItem('wordsData')) || [],
    };

    // 게임 설정
    this.maxAttempts = 10;
    this.currentWord = '';
    this.foundLetters = new Set();
    this.missedLetters = new Set();
    this.canPlay = false;
    this.score = 0;

    this.initializeElements();
    this.setupEventListeners();
    this.loadStudentInfo();
    this.startNewMission();
  }

  initializeElements() {
    // DOM 요소 초기화
    this.studentNameSpan = document.getElementById('studentName');
    this.classNameSpan = document.getElementById('className');
    this.currentScoreSpan = document.getElementById('currentScore');
    this.wordZone = document.getElementById('wordZone');
    this.attemptedLetters = document.getElementById('attemptedLetters');
    this.resultScreen = document.getElementById('resultScreen');
    this.missionStatus = document.getElementById('missionStatus');
    this.alertBubble = document.getElementById('alertBubble');
    this.nextMissionBtn = document.getElementById('nextMission');
    this.avatarParts = document.querySelectorAll('.avatar-part');
  }

  setupEventListeners() {
    // 키보드 입력 이벤트
    document.addEventListener('keypress', (e) => {
      if (this.canPlay && /^[A-Za-z]$/.test(e.key)) {
        this.processLetter(e.key.toUpperCase());
      }
    });

    // 다음 미션 버튼
    this.nextMissionBtn.addEventListener('click', () => this.startNewMission());

    // 로그아웃
    document
      .getElementById('logoutBtn')
      .addEventListener('click', () => this.handleLogout());
  }

  loadStudentInfo() {
    // 학생 이름 표시
    this.studentNameSpan.textContent = this.currentUser.username;

    // 학생의 클래스 정보 표시
    const studentClass = this.gameData.classes.find(
      (c) => c.id === this.currentUser.classId
    );
    if (studentClass) {
      this.classNameSpan.textContent = `Class: ${studentClass.name}`;
    }
  }

  startNewMission() {
    // 학생의 클래스에 해당하는 단어들 필터링
    const availableWords = this.gameData.wordsData.filter(
      (w) =>
        w.classId === this.currentUser.classId &&
        w.mentorId === this.currentUser.teacherId
    );

    if (availableWords.length === 0) {
      this.showAlert('No words available for your class!');
      return;
    }

    // 랜덤 단어 선택
    this.currentWord =
      availableWords[Math.floor(Math.random() * availableWords.length)].word;

    // 게임 상태 초기화
    this.foundLetters.clear();
    this.missedLetters.clear();
    this.canPlay = true;
    this.resultScreen.classList.add('hidden');

    // 아바타 초기화
    this.avatarParts.forEach((part) => (part.style.display = 'none'));

    // 화면 업데이트
    this.updateMissionDisplay();
  }

  processLetter(letter) {
    if (this.foundLetters.has(letter) || this.missedLetters.has(letter)) {
      this.showAlert('You already tried this letter!');
      return;
    }

    if (this.currentWord.includes(letter)) {
      this.foundLetters.add(letter);
    } else {
      this.missedLetters.add(letter);
      // 틀린 횟수만큼 아바타 부분 표시
      const partIndex = this.missedLetters.size;
      if (partIndex <= this.maxAttempts) {
        this.avatarParts[partIndex - 1].style.display = 'block';
      }
    }

    this.updateMissionDisplay();
    this.checkMissionStatus();
  }

  updateMissionDisplay() {
    // 단어 표시 업데이트
    this.wordZone.innerHTML = this.currentWord
      .split('')
      .map(
        (letter) => `
        <div class="letter-tile">
          ${this.foundLetters.has(letter) ? letter : ''}
        </div>
      `
      )
      .join('');

    // 틀린 글자들 표시
    this.attemptedLetters.textContent = Array.from(this.missedLetters).join(
      ' '
    );
  }

  checkMissionStatus() {
    // 승리 조건: 모든 글자를 찾음
    const isComplete = this.currentWord
      .split('')
      .every((letter) => this.foundLetters.has(letter));

    if (isComplete) {
      this.score += 1;
      this.currentScoreSpan.textContent = this.score;
      this.endMission(`Mission Complete! Your Score: ${this.score} 🎉`);
    }
    // 패배 조건: 최대 시도 횟수 초과
    else if (this.missedLetters.size >= this.maxAttempts) {
      this.endMission(`Game Over! The word was "${this.currentWord}" 🌿`);
    }
  }

  endMission(message) {
    this.canPlay = false;
    this.missionStatus.textContent = message;
    this.resultScreen.classList.remove('hidden');
  }

  showAlert(message) {
    this.alertBubble.textContent = message;
    this.alertBubble.classList.remove('hidden');
    setTimeout(() => {
      this.alertBubble.classList.add('hidden');
    }, 2000);
  }

  handleLogout() {
    if (confirm('로그아웃 하시겠습니까?')) {
      localStorage.removeItem('currentUser');
      window.location.href = '/login.html';
    }
  }
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
  window.studentExplorer = new StudentWordExplorer();
});
