class ManagerPanel {
  constructor() {
    this.loadFromLocalStorage();
    this.initializeElements();
    this.setupEventListeners();
    this.loadInitialData();
  }

  loadFromLocalStorage() {
    // 현재 로그인한 매니저(선생님) 정보 - 실제로는 로그인 시스템에서 받아와야 함
    this.currentUser = JSON.parse(localStorage.getItem('currentManager')) || {
      id: 1,
      role: 'MANAGER',
      username: 'teacher1',
    };

    // 공유 데이터 로드 (admin과 공유)
    this.classesData = JSON.parse(localStorage.getItem('classesData')) || [];
    this.teacherAssignments =
      JSON.parse(localStorage.getItem('teacherAssignments')) || [];
    this.wordsData = JSON.parse(localStorage.getItem('wordsData')) || [];
  }

  saveToLocalStorage() {
    localStorage.setItem('wordsData', JSON.stringify(this.wordsData));
    // classesData와 teacherAssignments는 admin만 수정 가능
  }

  initializeElements() {
    // Words Management elements
    this.wordsTableBody = document.getElementById('wordsTableBody');
    this.addWordBtn = document.getElementById('addWordBtn');
    this.wordModal = document.getElementById('wordModal');
    this.wordForm = document.getElementById('wordForm');
    this.classFilter = document.getElementById('classFilter');

    // Common elements
    this.logoutBtn = document.getElementById('logoutBtn');
  }

  setupEventListeners() {
    // Words Management
    this.addWordBtn.addEventListener('click', () => this.openWordModal());
    this.wordForm.addEventListener('submit', (e) => this.handleWordSubmit(e));
    this.classFilter.addEventListener('change', () => this.loadWords());

    // Logout
    this.logoutBtn.addEventListener('click', () => this.handleLogout());
  }

  loadInitialData() {
    this.updateClassFilters();
    this.loadWords();
  }

  // Class Filter Management
  updateClassFilters() {
    // 선생님에게 배정된 클래스만 필터링
    const assignedClasses = this.classesData.filter((c) =>
      this.teacherAssignments.some(
        (a) => a.teacherId === this.currentUser.id && a.classId === c.id
      )
    );

    const filterHTML = assignedClasses
      .map((c) => `<option value="${c.id}">${c.name}</option>`)
      .join('');

    this.classFilter.innerHTML = `
      <option value="">All My Classes</option>
      ${filterHTML}
    `;
  }

  // Words Management
  loadWords() {
    const filteredWords = this.filterWordsByClass();

    this.wordsTableBody.innerHTML = filteredWords
      .map((word) => {
        const classData = this.classesData.find((c) => c.id === word.classId);

        return `
        <tr>
          <td>${word.id}</td>
          <td>${word.word}</td>
          <td>${classData ? classData.name : 'Unknown'}</td>
          <td>
            <button onclick="managerPanel.editWord(${
              word.id
            })" class="btn btn-secondary">Edit</button>
            <button onclick="managerPanel.deleteWord(${
              word.id
            })" class="btn btn-danger">Delete</button>
          </td>
        </tr>
      `;
      })
      .join('');
  }

  filterWordsByClass() {
    const selectedClassId = parseInt(this.classFilter.value);
    return this.wordsData.filter(
      (word) =>
        (!selectedClassId || word.classId === selectedClassId) &&
        word.mentorId === this.currentUser.id
    );
  }

  openWordModal(wordId = null) {
    const modalTitle = document.getElementById('wordModalTitle');
    const wordForm = document.getElementById('wordForm');
    const classSelect = document.getElementById('classSelect');

    // 담당 클래스 옵션 업데이트
    const assignedClasses = this.classesData.filter((c) =>
      this.teacherAssignments.some(
        (a) => a.teacherId === this.currentUser.id && a.classId === c.id
      )
    );

    classSelect.innerHTML = assignedClasses
      .map((c) => `<option value="${c.id}">${c.name}</option>`)
      .join('');

    if (wordId) {
      const wordData = this.wordsData.find((w) => w.id === wordId);
      modalTitle.textContent = 'Edit Word';
      wordForm.wordInput.value = wordData.word;
      wordForm.classSelect.value = wordData.classId;
      wordForm.wordId.value = wordId;
    } else {
      modalTitle.textContent = 'Add New Word';
      wordForm.reset();
    }

    this.wordModal.classList.remove('hidden');
  }

  handleWordSubmit(e) {
    e.preventDefault();

    const wordData = {
      word: e.target.wordInput.value.toUpperCase(),
      classId: parseInt(e.target.classSelect.value),
      mentorId: this.currentUser.id,
      id: parseInt(e.target.wordId.value) || this.getNextWordId(),
    };

    if (e.target.wordId.value) {
      // Edit existing word
      const index = this.wordsData.findIndex(
        (w) => w.id === parseInt(wordData.id)
      );
      this.wordsData[index] = wordData;
    } else {
      // Add new word
      this.wordsData.push(wordData);
    }

    this.saveToLocalStorage();
    this.loadWords();
    this.closeWordModal();
  }

  getNextWordId() {
    return Math.max(0, ...this.wordsData.map((w) => w.id)) + 1;
  }

  editWord(wordId) {
    this.openWordModal(wordId);
  }

  deleteWord(wordId) {
    if (confirm('Are you sure you want to delete this word?')) {
      this.wordsData = this.wordsData.filter((w) => w.id !== wordId);
      this.saveToLocalStorage();
      this.loadWords();
    }
  }

  // Modal Management
  closeWordModal() {
    this.wordModal.classList.add('hidden');
    this.wordForm.reset();
  }

  handleLogout() {
    if (confirm('로그아웃 하시겠습니까?')) {
      window.location.href = '/login.html';
    }
  }
}

// Initialize manager panel when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
  window.managerPanel = new ManagerPanel();
});
