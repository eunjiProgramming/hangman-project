class WordExplorer {
  constructor() {
    // 현재 로그인한 관리자 정보
    this.currentUser = JSON.parse(localStorage.getItem('currentUser')) || {
      id: null,
      role: 'ADMIN',
      username: null,
    };

    // 게임에서 사용할 데이터 (추후 API로 대체 예정)
    this.gameData = {
      classA: {
        id: 1,
        mentors: {
          alpha: {
            id: 1,
            name: 'Mr. Alpha',
            words: ['ADVENTURE', 'EXPLORE', 'JOURNEY', 'DREAM', 'ACHIEVE'],
          },
          beta: {
            id: 2,
            name: 'Ms. Beta',
            words: ['CHALLENGE', 'VICTORY', 'SUCCESS', 'EFFORT', 'WIN'],
          },
          gamma: {
            id: 3,
            name: 'Mr. Gamma',
            words: ['NATURE', 'FOREST', 'RIVER', 'EARTH', 'LIFE'],
          },
        },
      },
      classB: {
        id: 2,
        mentors: {
          alpha: {
            id: 4,
            name: 'Mr. Alpha',
            words: ['DISCOVER', 'TREASURE', 'MYSTERY', 'WONDER', 'MAGIC'],
          },
          beta: {
            id: 5,
            name: 'Ms. Beta',
            words: ['ACHIEVE', 'INSPIRE', 'DREAM', 'FOCUS', 'GOAL'],
          },
          gamma: {
            id: 6,
            name: 'Mr. Gamma',
            words: ['MOUNTAIN', 'VALLEY', 'OCEAN', 'STAR', 'SKY'],
          },
        },
      },
      classC: {
        id: 3,
        mentors: {
          alpha: {
            id: 7,
            name: 'Mr. Alpha',
            words: ['COMPASS', 'MISSION', 'QUEST', 'VOYAGE', 'PATH'],
          },
          beta: {
            id: 8,
            name: 'Ms. Beta',
            words: ['WISDOM', 'TALENT', 'SKILL', 'LEARN', 'GROW'],
          },
          gamma: {
            id: 9,
            name: 'Mr. Gamma',
            words: ['RANGER', 'GUIDE', 'TRAIL', 'MAP', 'CAMP'],
          },
        },
      },
    };

    // 게임 설정
    this.maxAttempts = 10;
    this.currentWord = '';
    this.currentClass = null;
    this.currentMentor = null;
    this.foundLetters = new Set();
    this.missedLetters = new Set();
    this.canPlay = true;
    this.score = 0;

    this.initializeElements();
    this.setupEventListeners();
  }

  initializeElements() {
    // DOM 요소 초기화
    this.adminNameSpan = document.getElementById('adminName');
    this.classSelect = document.getElementById('classChoice');
    this.mentorSelect = document.getElementById('mentorChoice');
    this.mentorBox = document.getElementById('mentorBox');
    this.adventureZone = document.getElementById('adventureZone');
    this.wordZone = document.getElementById('wordZone');
    this.attemptedLetters = document.getElementById('attemptedLetters');
    this.resultScreen = document.getElementById('resultScreen');
    this.missionStatus = document.getElementById('missionStatus');
    this.alertBubble = document.getElementById('alertBubble');
    this.nextMissionBtn = document.getElementById('nextMission');
    this.avatarParts = document.querySelectorAll('.avatar-part');

    // 관리자 이름 표시
    this.adminNameSpan.textContent = this.currentUser.username;
  }

  setupEventListeners() {
    // 클래스 선택 이벤트
    this.classSelect.addEventListener('change', () => this.handleClassSelect());

    // 멘토 선택 이벤트
    this.mentorSelect.addEventListener('change', () =>
      this.handleMentorSelect()
    );

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

  handleClassSelect() {
    const selectedClass = this.classSelect.value;
    if (!selectedClass) return;

    this.currentClass = selectedClass;
    this.currentMentor = null;
    this.resetMentorSelect();

    const classData = this.gameData[selectedClass];
    Object.entries(classData.mentors).forEach(([mentorKey, mentorData]) => {
      const option = document.createElement('option');
      option.value = mentorKey;
      option.textContent = mentorData.name;
      this.mentorSelect.appendChild(option);
    });

    this.mentorBox.classList.remove('hidden');
  }

  handleMentorSelect() {
    const selectedMentor = this.mentorSelect.value;
    if (!selectedMentor) return;

    this.currentMentor = selectedMentor;
    this.startNewMission();
  }

  resetMentorSelect() {
    this.mentorSelect.innerHTML =
      '<option value="">Choose your mentor...</option>';
  }

  selectRandomWord(words) {
    return words[Math.floor(Math.random() * words.length)];
  }

  startNewMission() {
    if (this.currentClass && this.currentMentor) {
      const mentorData =
        this.gameData[this.currentClass].mentors[this.currentMentor];
      this.currentWord = this.selectRandomWord(mentorData.words);
    }

    this.foundLetters.clear();
    this.missedLetters.clear();
    this.canPlay = true;

    this.avatarParts.forEach((part) => (part.style.display = 'none'));

    this.adventureZone.classList.remove('hidden');
    this.resultScreen.classList.add('hidden');
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
      const partIndex = this.missedLetters.size;
      if (partIndex <= this.maxAttempts) {
        this.avatarParts[partIndex - 1].style.display = 'block';
      }
    }

    this.updateMissionDisplay();
    this.checkMissionStatus();
  }

  updateMissionDisplay() {
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

    this.attemptedLetters.textContent = Array.from(this.missedLetters).join(
      ' '
    );
  }

  checkMissionStatus() {
    const isComplete = this.currentWord
      .split('')
      .every((letter) => this.foundLetters.has(letter));

    if (isComplete) {
      this.score += 1;
      this.endMission(`Mission Complete! Score: ${this.score} 🎉`);
    } else if (this.missedLetters.size >= this.maxAttempts) {
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
  window.adminExplorer = new WordExplorer();
});
