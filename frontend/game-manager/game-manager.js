class ManagerWordExplorer {
  constructor() {
    // 현재 로그인한 선생님 정보
    this.currentUser = JSON.parse(localStorage.getItem('currentUser')) || {
      id: null,
      role: 'MANAGER',
      username: null,
    };

    // 선생님의 클래스 및 단어 데이터
    this.teacherData = {
      classes: JSON.parse(localStorage.getItem('classesData')) || [],
      teacherAssignments:
        JSON.parse(localStorage.getItem('teacherAssignments')) || [],
      wordsData: JSON.parse(localStorage.getItem('wordsData')) || [],
    };

    // 게임 설정
    this.maxAttempts = 10;
    this.currentWord = '';
    this.currentClass = null;
    this.foundLetters = new Set();
    this.missedLetters = new Set();
    this.canPlay = false;
    this.score = 0;

    this.initializeElements();
    this.setupEventListeners();
    this.loadTeacherClasses();
  }

  initializeElements() {
    // DOM 요소 초기화
    this.teacherNameSpan = document.getElementById('teacherName');
    this.classSelect = document.getElementById('classChoice');
    this.adventureZone = document.getElementById('adventureZone');
    this.wordZone = document.getElementById('wordZone');
    this.attemptedLetters = document.getElementById('attemptedLetters');
    this.resultScreen = document.getElementById('resultScreen');
    this.missionStatus = document.getElementById('missionStatus');
    this.alertBubble = document.getElementById('alertBubble');
    this.nextMissionBtn = document.getElementById('nextMission');
    this.avatarParts = document.querySelectorAll('.avatar-part');

    // 선생님 이름 표시
    this.teacherNameSpan.textContent = this.currentUser.username;
  }

  setupEventListeners() {
    // 클래스 선택 이벤트
    this.classSelect.addEventListener('change', () => this.handleClassSelect());

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

  loadTeacherClasses() {
    // 선생님에게 배정된 클래스만 필터링
    const assignedClasses = this.teacherData.classes.filter((c) =>
      this.teacherData.teacherAssignments.some(
        (a) => a.teacherId === this.currentUser.id && a.classId === c.id
      )
    );

    // 클래스 선택 옵션 업데이트
    this.classSelect.innerHTML = `
      <option value="">Select your class...</option>
      ${assignedClasses
        .map(
          (c) => `
        <option value="${c.id}">${c.name}</option>
      `
        )
        .join('')}
    `;
  }

  handleClassSelect() {
    const selectedClassId = parseInt(this.classSelect.value);
    if (selectedClassId) {
      this.currentClass = selectedClassId;
      this.adventureZone.classList.remove('hidden');
      this.startNewMission();
    } else {
      this.adventureZone.classList.add('hidden');
    }
  }

  startNewMission() {
    // 새로운 단어 선택
    const classWords = this.teacherData.wordsData.filter(
      (w) =>
        w.classId === this.currentClass && w.mentorId === this.currentUser.id
    );

    if (classWords.length === 0) {
      this.showAlert('No words available for this class!');
      return;
    }

    // 랜덤 단어 선택
    this.currentWord =
      classWords[Math.floor(Math.random() * classWords.length)].word;

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
      this.endMission(`Mission Complete! Score: ${this.score} 🎉`);
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
  window.managerExplorer = new ManagerWordExplorer();
});
