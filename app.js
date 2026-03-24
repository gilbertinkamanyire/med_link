/* =============================================
   MediLink - Digital Appointment & Health Reminder App
   JavaScript Application Logic
   ============================================= */

// ===== DATA STORE (localStorage-backed) =====
const STORAGE_KEY = 'medilink_data';

function getDefaultData() {
  return {
    user: null,
    appointments: [
      {
        id: 'appt-1',
        doctorName: 'Dr. Sarah Johnson',
        specialty: 'General Physician',
        clinic: 'City Health Clinic',
        date: getDateOffset(2),
        time: '09:00 AM',
        status: 'upcoming',
        color: '#008080'
      },
      {
        id: 'appt-2',
        doctorName: 'Dr. Emily Davis',
        specialty: 'Dermatology',
        clinic: 'SkinCare Center',
        date: getDateOffset(5),
        time: '02:30 PM',
        status: 'upcoming',
        color: '#7E57C2'
      },
      {
        id: 'appt-3',
        doctorName: 'Dr. Michael Chen',
        specialty: 'Cardiology',
        clinic: 'Heart & Vascular Institute',
        date: getDateOffset(-3),
        time: '11:00 AM',
        status: 'completed',
        color: '#FF7043'
      },
      {
        id: 'appt-4',
        doctorName: 'Dr. Lisa Adams',
        specialty: 'Pediatrics',
        clinic: 'Children\'s Wellness Center',
        date: getDateOffset(-7),
        time: '10:00 AM',
        status: 'cancelled',
        color: '#EF5350'
      }
    ],
    medications: [
      {
        id: 'med-1',
        name: 'Amoxicillin',
        dosage: '500mg',
        time: '08:00',
        frequency: 'twice',
        notes: 'Take with food',
        taken: false
      },
      {
        id: 'med-2',
        name: 'Vitamin D3',
        dosage: '1000 IU',
        time: '07:00',
        frequency: 'daily',
        notes: 'Take in the morning',
        taken: false
      },
      {
        id: 'med-3',
        name: 'Ibuprofen',
        dosage: '200mg',
        time: '20:00',
        frequency: 'as-needed',
        notes: 'Take if pain persists',
        taken: false
      }
    ],
    notifications: [
      {
        id: 'notif-1',
        type: 'appointment',
        title: 'Upcoming Appointment',
        message: 'Appointment with Dr. Sarah Johnson tomorrow at 9:00 AM',
        time: '2 hours ago',
        icon: 'calendar_month'
      },
      {
        id: 'notif-2',
        type: 'medication',
        title: 'Medication Reminder',
        message: 'Time to take Amoxicillin 500mg',
        time: '30 minutes ago',
        icon: 'medication'
      },
      {
        id: 'notif-3',
        type: 'alert',
        title: 'Health Tip',
        message: 'Regular exercise helps manage blood pressure effectively.',
        time: '1 hour ago',
        icon: 'tips_and_updates'
      }
    ]
  };
}

const doctors = [
  {
    id: 'doc-1',
    name: 'Dr. Sarah Johnson',
    specialty: 'general',
    specialtyLabel: 'General Physician',
    clinic: 'City Health Clinic',
    rating: 4.8,
    reviews: 124,
    slotsAvailable: 5,
    nextSlot: 'Today, 2:00 PM',
    color: '#008080',
    initials: 'SJ',
    slots: ['09:00 AM', '10:00 AM', '11:30 AM', '02:00 PM', '03:30 PM', '04:00 PM']
  },
  {
    id: 'doc-2',
    name: 'Dr. Emily Davis',
    specialty: 'dermatology',
    specialtyLabel: 'Dermatologist',
    clinic: 'SkinCare Center',
    rating: 4.9,
    reviews: 98,
    slotsAvailable: 3,
    nextSlot: 'Tomorrow, 9:00 AM',
    color: '#7E57C2',
    initials: 'ED',
    slots: ['09:00 AM', '11:00 AM', '01:00 PM', '03:00 PM']
  },
  {
    id: 'doc-3',
    name: 'Dr. Michael Chen',
    specialty: 'cardiology',
    specialtyLabel: 'Cardiologist',
    clinic: 'Heart & Vascular Institute',
    rating: 4.7,
    reviews: 156,
    slotsAvailable: 4,
    nextSlot: 'Today, 4:00 PM',
    color: '#FF7043',
    initials: 'MC',
    slots: ['08:30 AM', '10:30 AM', '01:30 PM', '04:00 PM', '05:00 PM']
  },
  {
    id: 'doc-4',
    name: 'Dr. Lisa Adams',
    specialty: 'pediatrics',
    specialtyLabel: 'Pediatrician',
    clinic: 'Children\'s Wellness Center',
    rating: 4.9,
    reviews: 210,
    slotsAvailable: 6,
    nextSlot: 'Today, 11:00 AM',
    color: '#4CAF50',
    initials: 'LA',
    slots: ['08:00 AM', '09:30 AM', '11:00 AM', '01:00 PM', '02:30 PM', '04:30 PM']
  },
  {
    id: 'doc-5',
    name: 'Dr. James Wilson',
    specialty: 'neurology',
    specialtyLabel: 'Neurologist',
    clinic: 'BrainCare Neurology Clinic',
    rating: 4.6,
    reviews: 87,
    slotsAvailable: 2,
    nextSlot: 'Tomorrow, 10:00 AM',
    color: '#FF5722',
    initials: 'JW',
    slots: ['10:00 AM', '12:00 PM', '02:00 PM', '04:00 PM']
  },
  {
    id: 'doc-6',
    name: 'Dr. Amanda Roberts',
    specialty: 'general',
    specialtyLabel: 'General Physician',
    clinic: 'MediCare Family Clinic',
    rating: 4.5,
    reviews: 143,
    slotsAvailable: 7,
    nextSlot: 'Today, 10:30 AM',
    color: '#009688',
    initials: 'AR',
    slots: ['08:00 AM', '09:00 AM', '10:30 AM', '12:00 PM', '02:00 PM', '03:30 PM', '05:00 PM']
  }
];

const healthTips = [
  "Stay hydrated! Drinking at least 8 glasses of water daily helps maintain energy levels and supports your immune system.",
  "Regular exercise, even just a 30-minute walk, can significantly reduce stress and improve cardiovascular health.",
  "Getting 7-9 hours of quality sleep each night is crucial for your body's healing and cognitive function.",
  "Eating a balanced diet rich in fruits, vegetables, and whole grains can boost your immune system naturally.",
  "Practice mindfulness or meditation for 10 minutes daily to reduce anxiety and improve mental well-being.",
  "Wash your hands frequently to prevent the spread of infections and maintain good hygiene.",
  "Schedule regular health check-ups to catch potential issues early and maintain preventive care.",
  "Limit screen time before bed to improve sleep quality and reduce eye strain."
];

// ===== UTILITY FUNCTIONS =====
function getDateOffset(days) {
  const d = new Date();
  d.setDate(d.getDate() + days);
  return d.toISOString().split('T')[0];
}

function formatDate(dateStr) {
  const d = new Date(dateStr + 'T00:00:00');
  const today = new Date();
  today.setHours(0,0,0,0);
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);
  
  if (d.getTime() === today.getTime()) return 'Today';
  if (d.getTime() === tomorrow.getTime()) return 'Tomorrow';

  const options = { month: 'short', day: 'numeric', year: 'numeric' };
  return d.toLocaleDateString('en-US', options);
}

function generateId(prefix) {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).substr(2, 5)}`;
}

function loadData() {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored) {
    try {
      return JSON.parse(stored);
    } catch (e) {
      return getDefaultData();
    }
  }
  return getDefaultData();
}

function saveData(data) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
}

let appData = loadData();

// ===== SPLASH SCREEN =====
document.addEventListener('DOMContentLoaded', () => {
  setTimeout(() => {
    document.getElementById('splash-screen').classList.remove('active');
    // Check if user is logged in
    if (appData.user) {
      showScreen('app-screen');
      initDashboard();
    } else {
      showScreen('auth-screen');
    }
  }, 3000);
});

function showScreen(screenId) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.getElementById(screenId).classList.add('active');
}

// ===== AUTH =====
function toggleAuthForm(form) {
  document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));
  document.getElementById(`${form}-form`).classList.add('active');
}

function showForgotPassword() {
  toggleAuthForm('forgot');
}

function togglePassword(inputId, el) {
  const input = document.getElementById(inputId);
  if (input.type === 'password') {
    input.type = 'text';
    el.textContent = 'visibility';
  } else {
    input.type = 'password';
    el.textContent = 'visibility_off';
  }
}

function handleLogin() {
  const email = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value.trim();

  if (!email || !password) {
    showToast('Please fill in all fields', 'error');
    return;
  }

  if (!email.includes('@')) {
    showToast('Please enter a valid email', 'error');
    return;
  }

  // Simulate login
  appData.user = {
    name: email.split('@')[0].replace(/[^a-zA-Z]/g, ' ').replace(/\b\w/g, l => l.toUpperCase()),
    email: email,
    phone: '',
    dob: '',
    gender: '',
    emergencyName: '',
    emergencyPhone: ''
  };
  saveData(appData);
  showScreen('app-screen');
  initDashboard();
  showToast('Welcome back!', 'success');
}

function handleSignup() {
  const name = document.getElementById('signup-name').value.trim();
  const email = document.getElementById('signup-email').value.trim();
  const phone = document.getElementById('signup-phone').value.trim();
  const password = document.getElementById('signup-password').value.trim();
  const confirmPassword = document.getElementById('signup-confirm-password').value.trim();

  if (!name || !email || !phone || !password || !confirmPassword) {
    showToast('Please fill in all fields', 'error');
    return;
  }

  if (!email.includes('@')) {
    showToast('Please enter a valid email', 'error');
    return;
  }

  if (password.length < 6) {
    showToast('Password must be at least 6 characters', 'error');
    return;
  }

  if (password !== confirmPassword) {
    showToast('Passwords do not match', 'error');
    return;
  }

  appData.user = {
    name: name,
    email: email,
    phone: phone,
    dob: '',
    gender: '',
    emergencyName: '',
    emergencyPhone: ''
  };
  saveData(appData);
  showScreen('app-screen');
  initDashboard();
  showToast('Account created successfully!', 'success');
}

function handleForgotPassword() {
  const email = document.getElementById('forgot-email').value.trim();
  if (!email || !email.includes('@')) {
    showToast('Please enter a valid email', 'error');
    return;
  }
  showToast('Reset link sent to your email', 'success');
  toggleAuthForm('login');
}

function handleLogout() {
  appData.user = null;
  saveData(appData);
  showScreen('auth-screen');
  toggleAuthForm('login');
  document.getElementById('login-email').value = '';
  document.getElementById('login-password').value = '';
  showToast('Signed out successfully', 'info');
}

// ===== NAVIGATION =====
function navigateTo(page) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.getElementById(`page-${page}`).classList.add('active');
  
  // Update bottom nav
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  const navBtn = document.getElementById(`nav-${page}`);
  if (navBtn) navBtn.classList.add('active');

  // Close notification panel
  document.getElementById('notification-panel').classList.remove('active');

  // Scroll to top
  document.querySelector('.app-content').scrollTop = 0;

  // Init page
  switch(page) {
    case 'dashboard': initDashboard(); break;
    case 'booking': renderDoctors(); break;
    case 'history': renderHistory('upcoming'); break;
    case 'reminders': renderMedications(); break;
    case 'profile': initProfile(); break;
    case 'about': break; // No specific init needed for about page
  }
}

// ===== DASHBOARD =====
function initDashboard() {
  updateGreeting();
  renderUpcomingAppointments();
  renderTodaysMeds();
  renderRandomHealthTip();
}

function updateGreeting() {
  const hour = new Date().getHours();
  let greeting = 'Good Morning,';
  if (hour >= 12 && hour < 17) greeting = 'Good Afternoon,';
  else if (hour >= 17) greeting = 'Good Evening,';

  document.getElementById('greeting-text').textContent = greeting;
  document.getElementById('user-name-display').textContent = appData.user ? appData.user.name : 'User';
}

function renderUpcomingAppointments() {
  const container = document.getElementById('upcoming-appointments');
  const upcoming = appData.appointments.filter(a => a.status === 'upcoming').slice(0, 3);

  if (upcoming.length === 0) {
    container.innerHTML = `
      <div class="no-data-msg">
        <span class="material-icons-outlined">event_busy</span>
        <p>No upcoming appointments</p>
      </div>`;
    return;
  }

  container.innerHTML = upcoming.map(apt => `
    <div class="appt-card">
      <div class="appt-avatar">
        <span class="material-icons-outlined">person</span>
      </div>
      <div class="appt-info">
        <h4>${apt.doctorName}</h4>
        <p>${apt.specialty} • ${apt.clinic}</p>
      </div>
      <div class="appt-time">
        <div class="date">${formatDate(apt.date)}</div>
        <div class="time">${apt.time}</div>
      </div>
    </div>
  `).join('');
}

function renderTodaysMeds() {
  const container = document.getElementById('todays-meds');
  const meds = appData.medications;

  if (meds.length === 0) {
    container.innerHTML = `
      <div class="no-data-msg">
        <span class="material-icons-outlined">medication</span>
        <p>No medications added yet</p>
      </div>`;
    return;
  }

  container.innerHTML = meds.map(med => `
    <div class="med-item">
      <div class="med-icon-wrap">
        <span class="material-icons-outlined">medication</span>
      </div>
      <div class="med-info">
        <h4>${med.name}</h4>
        <p>${med.dosage} • ${med.notes || 'No notes'}</p>
      </div>
      <div class="med-time-badge">${formatTime24to12(med.time)}</div>
      <button class="med-check ${med.taken ? 'checked' : ''}" onclick="toggleMedTaken('${med.id}')"></button>
    </div>
  `).join('');
}

function toggleMedTaken(medId) {
  const med = appData.medications.find(m => m.id === medId);
  if (med) {
    med.taken = !med.taken;
    saveData(appData);
    renderTodaysMeds();
    showToast(med.taken ? `${med.name} marked as taken ✓` : `${med.name} unmarked`, 'success');
  }
}

function formatTime24to12(time) {
  if (!time) return '';
  const [h, m] = time.split(':');
  const hour = parseInt(h);
  const ampm = hour >= 12 ? 'PM' : 'AM';
  const h12 = hour % 12 || 12;
  return `${h12}:${m} ${ampm}`;
}

function renderRandomHealthTip() {
  const tip = healthTips[Math.floor(Math.random() * healthTips.length)];
  document.getElementById('health-tip-text').textContent = tip;
}

// ===== BOOKING =====
let selectedDoctor = null;
let selectedSlot = null;

function renderDoctors(filteredList) {
  const container = document.getElementById('doctor-list');
  const list = filteredList || doctors;

  if (list.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <span class="material-icons-outlined">search_off</span>
        <h3>No doctors found</h3>
        <p>Try a different search or specialty</p>
      </div>`;
    return;
  }

  container.innerHTML = list.map(doc => `
    <div class="doctor-card" onclick="openBookingModal('${doc.id}')">
      <div class="doctor-avatar" style="background: ${doc.color};">
        ${doc.initials}
      </div>
      <div class="doctor-details">
        <h4>${doc.name}</h4>
        <p class="specialty">${doc.specialtyLabel}</p>
        <p class="clinic">${doc.clinic}</p>
        <div class="doctor-rating">
          <span class="material-icons-outlined">star</span>
          <span>${doc.rating} (${doc.reviews} reviews)</span>
        </div>
      </div>
      <div class="doctor-slots">
        <span class="slots-available">${doc.slotsAvailable} slots</span>
        <p class="next-slot">${doc.nextSlot}</p>
      </div>
    </div>
  `).join('');
}

function filterDoctors() {
  const query = document.getElementById('doctor-search').value.toLowerCase();
  const filtered = doctors.filter(d =>
    d.name.toLowerCase().includes(query) ||
    d.specialtyLabel.toLowerCase().includes(query) ||
    d.clinic.toLowerCase().includes(query)
  );
  renderDoctors(filtered);
}

function filterBySpecialty(specialty, btn) {
  document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
  btn.classList.add('active');

  if (specialty === 'all') {
    renderDoctors();
  } else {
    const filtered = doctors.filter(d => d.specialty === specialty);
    renderDoctors(filtered);
  }
}

function openBookingModal(doctorId) {
  selectedDoctor = doctors.find(d => d.id === doctorId);
  selectedSlot = null;

  if (!selectedDoctor) return;

  const body = document.getElementById('booking-modal-body');
  body.innerHTML = `
    <div class="booking-summary">
      <div class="booking-doctor-info">
        <div class="doctor-avatar" style="background: ${selectedDoctor.color};">
          ${selectedDoctor.initials}
        </div>
        <div>
          <h4>${selectedDoctor.name}</h4>
          <p style="font-size:13px;color:var(--text-secondary);">${selectedDoctor.specialtyLabel}</p>
          <p style="font-size:12px;color:var(--text-muted);">${selectedDoctor.clinic}</p>
        </div>
      </div>
      <div class="booking-detail-row">
        <span class="material-icons-outlined">calendar_today</span>
        <span class="label">Date</span>
        <span class="value">${formatDate(getDateOffset(0))} - ${formatDate(getDateOffset(1))}</span>
      </div>
      <div>
        <p style="font-size:14px;font-weight:600;margin-bottom:8px;">Select a Time Slot:</p>
        <div class="time-slots-grid">
          ${selectedDoctor.slots.map((slot, i) => `
            <div class="time-slot" onclick="selectTimeSlot('${slot}', this)">${slot}</div>
          `).join('')}
        </div>
      </div>
    </div>
  `;

  document.getElementById('booking-modal').classList.add('active');
}

function selectTimeSlot(slot, el) {
  document.querySelectorAll('.time-slot').forEach(s => s.classList.remove('selected'));
  el.classList.add('selected');
  selectedSlot = slot;
}

function closeBookingModal() {
  document.getElementById('booking-modal').classList.remove('active');
  selectedDoctor = null;
  selectedSlot = null;
}

function confirmBooking() {
  if (!selectedDoctor || !selectedSlot) {
    showToast('Please select a time slot', 'error');
    return;
  }

  const newAppt = {
    id: generateId('appt'),
    doctorName: selectedDoctor.name,
    specialty: selectedDoctor.specialtyLabel,
    clinic: selectedDoctor.clinic,
    date: getDateOffset(0),
    time: selectedSlot,
    status: 'upcoming',
    color: selectedDoctor.color
  };

  appData.appointments.unshift(newAppt);

  // Add notification
  appData.notifications.unshift({
    id: generateId('notif'),
    type: 'appointment',
    title: 'Appointment Confirmed',
    message: `Your appointment with ${selectedDoctor.name} at ${selectedSlot} has been confirmed.`,
    time: 'Just now',
    icon: 'calendar_month'
  });

  saveData(appData);
  closeBookingModal();
  showToast('Appointment booked successfully!', 'success');
  updateNotificationBadge();
}

// ===== APPOINTMENT HISTORY =====
function renderHistory(filter) {
  const container = document.getElementById('history-list');
  const filtered = appData.appointments.filter(a => a.status === filter);

  // Update active tab
  document.querySelectorAll('.tab-btn').forEach(t => t.classList.remove('active'));
  event.target.classList.add('active');

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <span class="material-icons-outlined">event_busy</span>
        <h3>No ${filter} appointments</h3>
        <p>Your ${filter} appointments will appear here</p>
      </div>`;
    return;
  }

  container.innerHTML = filtered.map(apt => `
    <div class="history-card">
      <div class="status-dot ${apt.status}"></div>
      <div class="history-info">
        <h4>${apt.doctorName}</h4>
        <p>${apt.specialty} • ${formatDate(apt.date)} at ${apt.time}</p>
      </div>
      <div class="history-actions">
        ${apt.status === 'upcoming' ? `
          <button class="history-action-btn cancel-btn" onclick="cancelAppointment('${apt.id}')" title="Cancel">
            <span class="material-icons-outlined">close</span>
          </button>
        ` : ''}
      </div>
    </div>
  `).join('');
}

function filterHistory(filter, btn) {
  const container = document.getElementById('history-list');
  const filtered = appData.appointments.filter(a => a.status === filter);

  document.querySelectorAll('.tab-btn').forEach(t => t.classList.remove('active'));
  btn.classList.add('active');

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <span class="material-icons-outlined">event_busy</span>
        <h3>No ${filter} appointments</h3>
        <p>Your ${filter} appointments will appear here</p>
      </div>`;
    return;
  }

  container.innerHTML = filtered.map(apt => `
    <div class="history-card">
      <div class="status-dot ${apt.status}"></div>
      <div class="history-info">
        <h4>${apt.doctorName}</h4>
        <p>${apt.specialty} • ${formatDate(apt.date)} at ${apt.time}</p>
      </div>
      <div class="history-actions">
        ${apt.status === 'upcoming' ? `
          <button class="history-action-btn cancel-btn" onclick="cancelAppointment('${apt.id}')" title="Cancel">
            <span class="material-icons-outlined">close</span>
          </button>
        ` : ''}
      </div>
    </div>
  `).join('');
}

function cancelAppointment(id) {
  const apt = appData.appointments.find(a => a.id === id);
  if (apt) {
    apt.status = 'cancelled';
    saveData(appData);
    filterHistory('upcoming', document.querySelector('.tab-btn.active'));
    showToast('Appointment cancelled', 'info');
  }
}

// ===== MEDICATION REMINDERS =====
function renderMedications() {
  const container = document.getElementById('medication-list');

  if (appData.medications.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <span class="material-icons-outlined">medication</span>
        <h3>No medications added</h3>
        <p>Add your first medication reminder</p>
      </div>`;
    return;
  }

  container.innerHTML = appData.medications.map(med => `
    <div class="medication-card">
      <div class="medication-card-header">
        <h4>
          <span class="material-icons-outlined">medication</span>
          ${med.name}
        </h4>
        <button class="med-delete-btn" onclick="deleteMedication('${med.id}')">
          <span class="material-icons-outlined">delete</span>
        </button>
      </div>
      <div class="medication-card-body">
        <div class="med-tag">
          <span class="material-icons-outlined">science</span>
          ${med.dosage}
        </div>
        <div class="med-tag">
          <span class="material-icons-outlined">schedule</span>
          ${formatTime24to12(med.time)}
        </div>
        <div class="med-tag">
          <span class="material-icons-outlined">repeat</span>
          ${capitalizeFirst(med.frequency)}
        </div>
        ${med.notes ? `
          <div class="med-tag">
            <span class="material-icons-outlined">notes</span>
            ${med.notes}
          </div>
        ` : ''}
      </div>
    </div>
  `).join('');
}

function capitalizeFirst(str) {
  if (!str) return '';
  const map = { 'daily': 'Daily', 'twice': 'Twice Daily', 'weekly': 'Weekly', 'as-needed': 'As Needed' };
  return map[str] || str.charAt(0).toUpperCase() + str.slice(1);
}

function openAddMedModal() {
  document.getElementById('med-name').value = '';
  document.getElementById('med-dosage').value = '';
  document.getElementById('med-time').value = '';
  document.getElementById('med-frequency').value = 'daily';
  document.getElementById('med-notes').value = '';
  document.getElementById('add-med-modal').classList.add('active');
}

function closeAddMedModal() {
  document.getElementById('add-med-modal').classList.remove('active');
}

function addMedication() {
  const name = document.getElementById('med-name').value.trim();
  const dosage = document.getElementById('med-dosage').value.trim();
  const time = document.getElementById('med-time').value;
  const frequency = document.getElementById('med-frequency').value;
  const notes = document.getElementById('med-notes').value.trim();

  if (!name || !dosage || !time) {
    showToast('Please fill in medication name, dosage, and time', 'error');
    return;
  }

  const newMed = {
    id: generateId('med'),
    name,
    dosage,
    time,
    frequency,
    notes,
    taken: false
  };

  appData.medications.push(newMed);
  saveData(appData);
  closeAddMedModal();
  renderMedications();
  showToast(`${name} reminder added!`, 'success');
}

function deleteMedication(id) {
  appData.medications = appData.medications.filter(m => m.id !== id);
  saveData(appData);
  renderMedications();
  showToast('Medication removed', 'info');
}

// ===== NOTIFICATIONS =====
function toggleNotifications() {
  const panel = document.getElementById('notification-panel');
  panel.classList.toggle('active');
  if (panel.classList.contains('active')) {
    renderNotifications();
  }
}

function renderNotifications() {
  const container = document.getElementById('notif-list');

  if (appData.notifications.length === 0) {
    container.innerHTML = `
      <div style="padding:40px 20px;text-align:center;color:var(--text-muted);">
        <span class="material-icons-outlined" style="font-size:40px;margin-bottom:8px;">notifications_none</span>
        <p>No notifications</p>
      </div>`;
    return;
  }

  container.innerHTML = appData.notifications.map(notif => `
    <div class="notif-item">
      <div class="notif-icon-wrap ${notif.type}">
        <span class="material-icons-outlined">${notif.icon}</span>
      </div>
      <div class="notif-text">
        <h4>${notif.title}</h4>
        <p>${notif.message}</p>
        <p class="notif-time">${notif.time}</p>
      </div>
    </div>
  `).join('');
}

function clearNotifications() {
  appData.notifications = [];
  saveData(appData);
  renderNotifications();
  updateNotificationBadge();
  showToast('Notifications cleared', 'info');
}

function updateNotificationBadge() {
  const badge = document.getElementById('notif-badge');
  const count = appData.notifications.length;
  badge.textContent = count;
  badge.classList.toggle('hidden', count === 0);
}

// ===== PROFILE =====
function initProfile() {
  if (!appData.user) return;
  const u = appData.user;
  document.getElementById('profile-name').textContent = u.name;
  document.getElementById('profile-email').textContent = u.email;
  document.getElementById('profile-fullname').value = u.name || '';
  document.getElementById('profile-phone').value = u.phone || '';
  document.getElementById('profile-dob').value = u.dob || '';
  document.getElementById('profile-gender').value = u.gender || '';
  document.getElementById('emergency-name').value = u.emergencyName || '';
  document.getElementById('emergency-phone').value = u.emergencyPhone || '';
}

function saveProfile() {
  if (!appData.user) return;
  appData.user.name = document.getElementById('profile-fullname').value.trim();
  appData.user.phone = document.getElementById('profile-phone').value.trim();
  appData.user.dob = document.getElementById('profile-dob').value;
  appData.user.gender = document.getElementById('profile-gender').value;
  appData.user.emergencyName = document.getElementById('emergency-name').value.trim();
  appData.user.emergencyPhone = document.getElementById('emergency-phone').value.trim();

  saveData(appData);
  document.getElementById('profile-name').textContent = appData.user.name;
  document.getElementById('user-name-display').textContent = appData.user.name;
  showToast('Profile saved successfully!', 'success');
}

// ===== TOAST =====
function showToast(message, type = 'success') {
  const toast = document.getElementById('toast');
  const icon = document.getElementById('toast-icon');
  const msg = document.getElementById('toast-message');

  toast.className = 'toast';

  switch(type) {
    case 'success':
      icon.textContent = 'check_circle';
      toast.classList.add('success');
      break;
    case 'error':
      icon.textContent = 'error';
      toast.classList.add('error');
      break;
    case 'info':
      icon.textContent = 'info';
      toast.classList.add('info');
      break;
  }

  msg.textContent = message;
  toast.classList.add('show');

  setTimeout(() => {
    toast.classList.remove('show');
  }, 3000);
}

// ===== CLOSE panels on outside clicks =====
document.addEventListener('click', (e) => {
  const panel = document.getElementById('notification-panel');
  const bell = document.querySelector('.notification-bell');
  if (panel && panel.classList.contains('active') && !panel.contains(e.target) && !bell.contains(e.target)) {
    panel.classList.remove('active');
  }
});

// ===== INIT =====
updateNotificationBadge();
