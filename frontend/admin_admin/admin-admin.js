class AdminPanel {
  constructor() {
    this.loadFromLocalStorage();
    this.initializeElements();
    this.setupEventListeners();
    this.loadInitialData();
  }

  loadFromLocalStorage() {
    // 현재 사용자 정보
    this.currentUser = {
      role: 'ADMIN',
      username: 'admin',
    };

    // localStorage에서 데이터 로드 또는 초기값 설정
    this.classesData = JSON.parse(localStorage.getItem('classesData')) || [
      { id: 1, name: 'Class A', description: 'Advanced Level' },
      { id: 2, name: 'Class B', description: 'Intermediate Level' },
      { id: 3, name: 'Class C', description: 'Beginner Level' },
    ];

    this.teacherAssignments = JSON.parse(
      localStorage.getItem('teacherAssignments')
    ) || [
      { teacherId: 1, classId: 1 },
      { teacherId: 2, classId: 1 },
      { teacherId: 1, classId: 2 },
    ];

    this.usersData = JSON.parse(localStorage.getItem('usersData')) || [
      { id: 1, username: 'teacher1', role: 'MANAGER', password: 'encrypted1' },
      { id: 2, username: 'teacher2', role: 'MANAGER', password: 'encrypted2' },
      { id: 3, username: 'student1', role: 'USER', password: 'encrypted3' },
      { id: 4, username: 'admin', role: 'ADMIN', password: 'encrypted4' },
    ];

    this.wordsData = JSON.parse(localStorage.getItem('wordsData')) || [
      { id: 1, word: 'ADVENTURE', classId: 1, mentorId: 1 },
      { id: 2, word: 'CHALLENGE', classId: 1, mentorId: 2 },
    ];

    this.studentAssignments = JSON.parse(
      localStorage.getItem('studentAssignments')
    ) || [{ studentId: 3, classId: 1, teacherId: 1 }];
  }

  saveToLocalStorage() {
    localStorage.setItem('classesData', JSON.stringify(this.classesData));
    localStorage.setItem(
      'teacherAssignments',
      JSON.stringify(this.teacherAssignments)
    );
    localStorage.setItem('usersData', JSON.stringify(this.usersData));
    localStorage.setItem('wordsData', JSON.stringify(this.wordsData));
    localStorage.setItem(
      'studentAssignments',
      JSON.stringify(this.studentAssignments)
    );
  }

  initializeElements() {
    // Tab elements
    this.tabButtons = document.querySelectorAll('.tab-btn');
    this.managementSections = document.querySelectorAll('.management-section');

    // Words Management elements
    this.wordsTableBody = document.getElementById('wordsTableBody');
    this.addWordBtn = document.getElementById('addWordBtn');
    this.wordModal = document.getElementById('wordModal');
    this.wordForm = document.getElementById('wordForm');

    // Users Management elements
    this.usersTableBody = document.getElementById('usersTableBody');
    this.registerUserBtn = document.getElementById('registerUserBtn');
    this.userModal = document.getElementById('userModal');
    this.userForm = document.getElementById('userForm');

    // Classes Management elements
    this.classesTableBody = document.getElementById('classesTableBody');
    this.addClassBtn = document.getElementById('addClassBtn');
    this.assignTeacherBtn = document.getElementById('assignTeacherBtn');
    this.classModal = document.getElementById('classModal');
    this.classForm = document.getElementById('classForm');
    this.teacherAssignModal = document.getElementById('teacherAssignModal');
    this.teacherAssignForm = document.getElementById('teacherAssignForm');

    // Students Management elements
    this.studentsTableBody = document.getElementById('studentsTableBody');
    this.addStudentBtn = document.getElementById('addStudentBtn');
    this.studentModal = document.getElementById('studentModal');
    this.studentForm = document.getElementById('studentForm');
    this.studentClassFilter = document.getElementById('studentClassFilter');
    this.studentTeacherFilter = document.getElementById('studentTeacherFilter');

    // Common elements
    this.logoutBtn = document.getElementById('logoutBtn');
  }

  setupEventListeners() {
    // Tab navigation
    this.tabButtons.forEach((button) => {
      button.addEventListener('click', () => this.handleTabChange(button));
    });

    // Words Management
    this.addWordBtn.addEventListener('click', () => this.openWordModal());
    this.wordForm.addEventListener('submit', (e) => this.handleWordSubmit(e));

    // Users Management
    this.registerUserBtn.addEventListener('click', () => this.openUserModal());
    this.userForm.addEventListener('submit', (e) => this.handleUserSubmit(e));

    // Classes Management
    this.addClassBtn.addEventListener('click', () => this.openClassModal());
    this.assignTeacherBtn.addEventListener('click', () =>
      this.openTeacherAssignModal()
    );
    this.classForm.addEventListener('submit', (e) => this.handleClassSubmit(e));
    this.teacherAssignForm.addEventListener('submit', (e) =>
      this.handleTeacherAssign(e)
    );

    // Students Management
    this.addStudentBtn.addEventListener('click', () => this.openStudentModal());
    this.studentForm.addEventListener('submit', (e) =>
      this.handleStudentSubmit(e)
    );
    this.studentClassFilter.addEventListener('change', () =>
      this.loadStudents()
    );
    this.studentTeacherFilter.addEventListener('change', () =>
      this.loadStudents()
    );

    // Logout
    this.logoutBtn.addEventListener('click', () => this.handleLogout());

    // Modal close buttons
    document.querySelectorAll('.modal-close').forEach((button) => {
      button.addEventListener('click', () => {
        const modal = button.closest('.modal');
        if (modal) {
          modal.classList.add('hidden');
        }
      });
    });
  }

  loadInitialData() {
    this.loadClasses();
    this.loadUsers();
    this.loadWords();
    this.loadStudents();
    this.updateFilters();
  }

  updateFilters() {
    // Update class filter
    const classFilter = document.getElementById('classFilter');
    classFilter.innerHTML =
      '<option value="">All Classes</option>' +
      this.classesData
        .map((c) => `<option value="${c.id}">${c.name}</option>`)
        .join('');

    // Update mentor filter (only MANAGER role users)
    const mentorFilter = document.getElementById('mentorFilter');
    const teachers = this.usersData.filter((u) => u.role === 'MANAGER');
    mentorFilter.innerHTML =
      '<option value="">All Mentors</option>' +
      teachers
        .map((t) => `<option value="${t.id}">${t.username}</option>`)
        .join('');

    // Update student filters
    this.updateStudentFilters();
  }

  updateStudentFilters() {
    // Class filter
    this.studentClassFilter.innerHTML =
      '<option value="">All Classes</option>' +
      this.classesData
        .map((c) => `<option value="${c.id}">${c.name}</option>`)
        .join('');

    // Teacher filter (only MANAGER role users)
    const teachers = this.usersData.filter((u) => u.role === 'MANAGER');
    this.studentTeacherFilter.innerHTML =
      '<option value="">All Teachers</option>' +
      teachers
        .map((t) => `<option value="${t.id}">${t.username}</option>`)
        .join('');
  }

  // Tab Management
  handleTabChange(clickedTab) {
    const tabName = clickedTab.dataset.tab;

    this.tabButtons.forEach((button) => {
      button.classList.remove('active');
    });

    this.managementSections.forEach((section) => {
      section.classList.remove('active');
    });

    clickedTab.classList.add('active');
    document.getElementById(`${tabName}Section`).classList.add('active');
  }

  // Words Management
  loadWords() {
    this.wordsTableBody.innerHTML = this.wordsData
      .map((word) => {
        const classData = this.classesData.find((c) => c.id === word.classId);
        const mentor = this.usersData.find((u) => u.id === word.mentorId);

        return `
        <tr>
          <td>${word.id}</td>
          <td>${word.word}</td>
          <td>${classData ? classData.name : 'Unknown'}</td>
          <td>${mentor ? mentor.username : 'Unknown'}</td>
          <td>
            <button onclick="adminPanel.editWord(${
              word.id
            })" class="btn btn-secondary">Edit</button>
            <button onclick="adminPanel.deleteWord(${
              word.id
            })" class="btn btn-danger">Delete</button>
          </td>
        </tr>
      `;
      })
      .join('');
  }

  openWordModal(wordId = null) {
    const modalTitle = document.getElementById('wordModalTitle');
    const wordForm = document.getElementById('wordForm');
    const classSelect = document.getElementById('classSelect');
    const mentorSelect = document.getElementById('mentorSelect');

    // Update class options
    classSelect.innerHTML = this.classesData
      .map((c) => `<option value="${c.id}">${c.name}</option>`)
      .join('');

    // Update mentor options (only MANAGER role users)
    const teachers = this.usersData.filter((u) => u.role === 'MANAGER');
    mentorSelect.innerHTML = teachers
      .map((t) => `<option value="${t.id}">${t.username}</option>`)
      .join('');

    if (wordId) {
      const wordData = this.wordsData.find((w) => w.id === wordId);
      modalTitle.textContent = 'Edit Word';
      wordForm.wordInput.value = wordData.word;
      wordForm.classSelect.value = wordData.classId;
      wordForm.mentorSelect.value = wordData.mentorId;
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
      mentorId: parseInt(e.target.mentorSelect.value),
      id: parseInt(e.target.wordId.value) || this.getNextWordId(),
    };

    // Validate that the selected mentor is assigned to the selected class
    const isTeacherAssigned = this.teacherAssignments.some(
      (a) => a.teacherId === wordData.mentorId && a.classId === wordData.classId
    );

    if (!isTeacherAssigned) {
      alert('Selected mentor is not assigned to this class');
      return;
    }

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

  deleteWord(wordId) {
    if (confirm('Are you sure you want to delete this word?')) {
      this.wordsData = this.wordsData.filter((w) => w.id !== wordId);
      this.saveToLocalStorage();
      this.loadWords();
    }
  }

  // Users Management
  loadUsers() {
    this.usersTableBody.innerHTML = this.usersData
      .map(
        (user) => `
      <tr>
        <td>${user.username}</td>
        <td>${user.role}</td>
        <td>
          <button onclick="adminPanel.editUser(${user.id})" class="btn btn-secondary">Edit</button>
          <button onclick="adminPanel.deleteUser(${user.id})" class="btn btn-danger">Delete</button>
        </td>
      </tr>
    `
      )
      .join('');
  }

  openUserModal(userId = null) {
    const modalTitle = document.getElementById('userModalTitle');
    const userForm = document.getElementById('userForm');

    if (userId) {
      const userData = this.usersData.find((u) => u.id === userId);
      modalTitle.textContent = 'Edit User';
      userForm.username.value = userData.username;
      userForm.role.value = userData.role;
      userForm.userId.value = userId;
      userForm.password.required = false;
    } else {
      modalTitle.textContent = 'Register New User';
      userForm.reset();
      userForm.password.required = true;
    }

    this.userModal.classList.remove('hidden');
  }

  handleUserSubmit(e) {
    e.preventDefault();

    const userData = {
      username: e.target.username.value,
      role: e.target.role.value,
      id: parseInt(e.target.userId.value) || this.getNextUserId(),
    };

    if (e.target.password.value) {
      userData.password = e.target.password.value; // 실제로는 암호화 필요
    }

    if (e.target.userId.value) {
      // Edit existing user
      const index = this.usersData.findIndex(
        (u) => u.id === parseInt(userData.id)
      );
      this.usersData[index] = {
        ...this.usersData[index],
        ...userData,
      };
    } else {
      // Add new user
      this.usersData.push(userData);
    }

    this.saveToLocalStorage();
    this.loadUsers();
    this.closeUserModal();
  }

  getNextUserId() {
    return Math.max(0, ...this.usersData.map((u) => u.id)) + 1;
  }

  deleteUser(userId) {
    if (confirm('Are you sure you want to delete this user?')) {
      // Check if user has any assignments
      const hasTeacherAssignments = this.teacherAssignments.some(
        (a) => a.teacherId === userId
      );
      const hasStudentAssignments = this.studentAssignments.some(
        (a) => a.studentId === userId
      );

      if (hasTeacherAssignments || hasStudentAssignments) {
        alert(
          'Cannot delete user with active assignments. Please remove assignments first.'
        );
        return;
      }

      this.usersData = this.usersData.filter((u) => u.id !== userId);
      this.saveToLocalStorage();
      this.loadUsers();
    }
  }

  // Classes Management
  loadClasses() {
    this.classesTableBody.innerHTML = this.classesData
      .map(
        (classItem) => `
      <tr>
        <td>${classItem.name}</td>
        <td>${classItem.description}</td>
        <td>${this.getAssignedTeachers(classItem.id)}</td>
        <td>
          <button onclick="adminPanel.editClass(${
            classItem.id
          })" class="btn btn-secondary">Edit</button>
          <button onclick="adminPanel.deleteClass(${
            classItem.id
          })" class="btn btn-danger">Delete</button>
        </td>
      </tr>
    `
      )
      .join('');
  }

  getAssignedTeachers(classId) {
    const assignments = this.teacherAssignments.filter(
      (a) => a.classId === classId
    );
    return assignments
      .map((a) => {
        const teacher = this.usersData.find((u) => u.id === a.teacherId);
        return teacher ? teacher.username : 'Unknown';
      })
      .join(', ');
  }

  openClassModal(classId = null) {
    const modalTitle = document.getElementById('classModalTitle');
    const classNameInput = document.getElementById('className');
    const classDescInput = document.getElementById('classDescription');
    const classIdInput = document.getElementById('classId');

    if (classId) {
      const classData = this.classesData.find((c) => c.id === classId);
      modalTitle.textContent = 'Edit Class';
      classNameInput.value = classData.name;
      classDescInput.value = classData.description;
      classIdInput.value = classId;
    } else {
      modalTitle.textContent = 'Add New Class';
      classNameInput.value = '';
      classDescInput.value = '';
      classIdInput.value = '';
    }

    this.classModal.classList.remove('hidden');
  }

  handleClassSubmit(e) {
    e.preventDefault();

    const classData = {
      name: document.getElementById('className').value,
      description: document.getElementById('classDescription').value,
      id:
        parseInt(document.getElementById('classId').value) ||
        this.getNextClassId(),
    };

    if (document.getElementById('classId').value) {
      // Edit existing class
      const index = this.classesData.findIndex(
        (c) => c.id === parseInt(classData.id)
      );
      this.classesData[index] = classData;
    } else {
      // Add new class
      this.classesData.push(classData);
    }

    this.saveToLocalStorage();
    this.loadClasses();
    this.closeClassModal();
  }

  getNextClassId() {
    return Math.max(0, ...this.classesData.map((c) => c.id)) + 1;
  }

  deleteClass(classId) {
    if (confirm('Are you sure you want to delete this class?')) {
      // Check if there are any assignments
      const hasTeacherAssignments = this.teacherAssignments.some(
        (a) => a.classId === classId
      );
      const hasStudentAssignments = this.studentAssignments.some(
        (a) => a.classId === classId
      );

      if (hasTeacherAssignments || hasStudentAssignments) {
        alert(
          'Cannot delete class with active assignments. Please remove assignments first.'
        );
        return;
      }

      this.classesData = this.classesData.filter((c) => c.id !== classId);
      this.saveToLocalStorage();
      this.loadClasses();
    }
  }

  // Teacher Assignment Management
  openTeacherAssignModal() {
    const teacherSelect = document.getElementById('teacherSelect');
    const assignClassSelect = document.getElementById('assignClassSelect');

    // Get only MANAGER role users
    const teachers = this.usersData.filter((u) => u.role === 'MANAGER');

    // Update teacher options
    teacherSelect.innerHTML = `
      <option value="">Select Teacher</option>
      ${teachers
        .map((t) => `<option value="${t.id}">${t.username}</option>`)
        .join('')}
    `;

    // Update class options
    assignClassSelect.innerHTML = `
      <option value="">Select Class</option>
      ${this.classesData
        .map((c) => `<option value="${c.id}">${c.name}</option>`)
        .join('')}
    `;

    this.teacherAssignModal.classList.remove('hidden');
  }

  handleTeacherAssign(e) {
    e.preventDefault();

    const teacherId = parseInt(document.getElementById('teacherSelect').value);
    const classId = parseInt(
      document.getElementById('assignClassSelect').value
    );

    if (!teacherId || !classId) {
      alert('Please select both teacher and class');
      return;
    }

    // Check if assignment already exists
    const existingAssignment = this.teacherAssignments.find(
      (a) => a.teacherId === teacherId && a.classId === classId
    );

    if (existingAssignment) {
      alert('This teacher is already assigned to this class');
      return;
    }

    // Add new assignment
    this.teacherAssignments.push({ teacherId, classId });
    this.saveToLocalStorage();

    // Update UI
    this.loadClasses();
    this.closeTeacherAssignModal();
  }

  // Students Management
  loadStudents() {
    const selectedClassId = parseInt(this.studentClassFilter.value);
    const selectedTeacherId = parseInt(this.studentTeacherFilter.value);

    // Get students (users with role USER)
    const students = this.usersData.filter((u) => u.role === 'USER');

    this.studentsTableBody.innerHTML = students
      .map((student) => {
        const assignment = this.studentAssignments.find(
          (a) => a.studentId === student.id
        );
        if (!assignment) return '';

        const classData = this.classesData.find(
          (c) => c.id === assignment.classId
        );
        const teacher = this.usersData.find(
          (u) => u.id === assignment.teacherId
        );

        // Apply filters
        if (selectedClassId && assignment.classId !== selectedClassId)
          return '';
        if (selectedTeacherId && assignment.teacherId !== selectedTeacherId)
          return '';

        return `
        <tr>
          <td>${student.username}</td>
          <td>${classData ? classData.name : 'Unknown'}</td>
          <td>${teacher ? teacher.username : 'Unknown'}</td>
          <td>
            <button onclick="adminPanel.editStudent(${
              student.id
            })" class="btn btn-secondary">Edit</button>
            <button onclick="adminPanel.deleteStudent(${
              student.id
            })" class="btn btn-danger">Delete</button>
          </td>
        </tr>
      `;
      })
      .join('');
  }

  openStudentModal(studentId = null) {
    const modalTitle = document.getElementById('studentModalTitle');
    const studentForm = document.getElementById('studentForm');
    const classSelect = document.getElementById('studentClassSelect');

    // Update class options
    classSelect.innerHTML = this.classesData
      .map((c) => `<option value="${c.id}">${c.name}</option>`)
      .join('');

    if (studentId) {
      const studentData = this.usersData.find((u) => u.id === studentId);
      const assignment = this.studentAssignments.find(
        (a) => a.studentId === studentId
      );

      modalTitle.textContent = 'Edit Student';
      studentForm.studentUsername.value = studentData.username;
      studentForm.studentId.value = studentId;
      studentForm.studentPassword.required = false;

      if (assignment) {
        studentForm.studentClassSelect.value = assignment.classId;
        this.updateTeacherOptions(assignment.classId, assignment.teacherId);
      }
    } else {
      modalTitle.textContent = 'Add New Student';
      studentForm.reset();
      studentForm.studentPassword.required = true;
      this.updateTeacherOptions();
    }

    this.studentModal.classList.remove('hidden');
  }

  updateTeacherOptions(classId = null, selectedTeacherId = null) {
    const teacherSelect = document.getElementById('studentTeacherSelect');
    teacherSelect.innerHTML = '<option value="">Select Teacher</option>';

    if (!classId) {
      classId = document.getElementById('studentClassSelect').value;
    }

    if (classId) {
      // Get teachers assigned to the selected class
      const assignedTeachers = this.teacherAssignments
        .filter((a) => a.classId === parseInt(classId))
        .map((a) => this.usersData.find((u) => u.id === a.teacherId))
        .filter((t) => t); // Remove null values

      teacherSelect.innerHTML += assignedTeachers
        .map(
          (t) =>
            `<option value="${t.id}" ${
              t.id === selectedTeacherId ? 'selected' : ''
            }>${t.username}</option>`
        )
        .join('');
    }
  }

  handleStudentSubmit(e) {
    e.preventDefault();

    const studentData = {
      username: e.target.studentUsername.value,
      role: 'USER',
      id: parseInt(e.target.studentId.value) || this.getNextUserId(),
    };

    if (e.target.studentPassword.value) {
      studentData.password = e.target.studentPassword.value; // 실제로는 암호화 필요
    }

    const classId = parseInt(e.target.studentClassSelect.value);
    const teacherId = parseInt(e.target.studentTeacherSelect.value);

    if (!classId || !teacherId) {
      alert('Please select both class and teacher');
      return;
    }

    if (e.target.studentId.value) {
      // Edit existing student
      const index = this.usersData.findIndex(
        (u) => u.id === parseInt(studentData.id)
      );
      this.usersData[index] = {
        ...this.usersData[index],
        ...studentData,
      };

      // Update assignment
      const assignmentIndex = this.studentAssignments.findIndex(
        (a) => a.studentId === studentData.id
      );
      if (assignmentIndex !== -1) {
        this.studentAssignments[assignmentIndex] = {
          studentId: studentData.id,
          classId,
          teacherId,
        };
      } else {
        this.studentAssignments.push({
          studentId: studentData.id,
          classId,
          teacherId,
        });
      }
    } else {
      // Add new student
      this.usersData.push(studentData);
      this.studentAssignments.push({
        studentId: studentData.id,
        classId,
        teacherId,
      });
    }

    this.saveToLocalStorage();
    this.loadStudents();
    this.closeStudentModal();
  }

  deleteStudent(studentId) {
    if (confirm('Are you sure you want to delete this student?')) {
      this.usersData = this.usersData.filter((u) => u.id !== studentId);
      this.studentAssignments = this.studentAssignments.filter(
        (a) => a.studentId !== studentId
      );
      this.saveToLocalStorage();
      this.loadStudents();
    }
  }

  // Modal Management
  closeWordModal() {
    this.wordModal.classList.add('hidden');
    this.wordForm.reset();
  }

  closeUserModal() {
    this.userModal.classList.add('hidden');
    this.userForm.reset();
  }

  closeClassModal() {
    this.classModal.classList.add('hidden');
    this.classForm.reset();
  }

  closeTeacherAssignModal() {
    this.teacherAssignModal.classList.add('hidden');
    this.teacherAssignForm.reset();
  }

  closeStudentModal() {
    this.studentModal.classList.add('hidden');
    this.studentForm.reset();
  }

  handleLogout() {
    if (confirm('Are you sure you want to logout?')) {
      window.location.href = '/login.html';
    }
  }
}

// Initialize admin panel when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
  window.adminPanel = new AdminPanel();
});
