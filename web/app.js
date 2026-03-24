const API_URL = 'http://localhost:3000/api';
let currentUser = null;
let doctors = [];
let medsChart = null;

// Initialization
document.addEventListener('DOMContentLoaded', () => {
    const saved = localStorage.getItem('medilink_user');
    if (saved) {
        currentUser = JSON.parse(saved);
        initializeDashboard();
    }
});

// Navigation & UI
function toggleAuth() {
    document.getElementById('login-form').classList.toggle('hidden');
    document.getElementById('signup-form').classList.toggle('hidden');
}

function showTab(tabId) {
    document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
    
    document.getElementById(`tab-${tabId}`).classList.add('active');
    const navItem = Array.from(document.querySelectorAll('.nav-item')).find(i => i.textContent.toLowerCase().includes(tabId));
    if(navItem) navItem.classList.add('active');

    if(tabId === 'dashboard') loadDashboardData();
    if(tabId === 'booking') loadDoctors();
    if(tabId === 'medication') loadMedications();
    if(tabId === 'history') loadHistory();
    if(tabId === 'profile') loadProfile();
}

// Authentication
async function handleLogin() {
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        const data = await response.json();
        
        if (response.ok) {
            currentUser = data;
            localStorage.setItem('medilink_user', JSON.stringify(currentUser));
            initializeDashboard();
        } else {
            alert(data.error || "Login failed");
        }
    } catch (e) {
        alert("Server not reachable. Make sure backend is running.");
    }
}

async function handleRegister() {
    const name = document.getElementById('reg-name').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const role = document.querySelector('input[name="role"]:checked').value;

    try {
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password, role })
        });
        const data = await response.json();
        
        if (response.ok) {
            alert("Registration successful! Please login.");
            toggleAuth();
        } else {
            alert(data.error);
        }
    } catch (e) { alert("Registration failed."); }
}

function logout() {
    localStorage.removeItem('medilink_user');
    location.reload();
}

// Data Fetching & Rendering
function initializeDashboard() {
    document.getElementById('auth-screen').classList.add('hidden');
    
    if (currentUser.role === 'admin') {
        document.getElementById('admin-screen').classList.remove('hidden');
        loadAdminData();
    } else {
        document.getElementById('main-screen').classList.remove('hidden');
        document.getElementById('welcome-text').textContent = `Hello, ${currentUser.name.split(' ')[0]}!`;
        document.getElementById('current-date').textContent = new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' });
        if (currentUser.profilePic) {
            document.getElementById('header-profile-pic').src = currentUser.profilePic;
        }
        loadDashboardData();
        setupChart();
    }
}

async function loadDashboardData() {
    // Next Appointment
    const res = await fetch(`${API_URL}/appointments/${currentUser.id}`);
    const appts = await res.json();
    const nextText = document.getElementById('next-appt-card');
    
    if (appts.length > 0) {
        const next = appts[appts.length - 1]; // Mock assumption
        nextText.innerHTML = `<div class="appt-dash">
            <strong>${next.doctorName}</strong><br>
            <span style="font-size:0.8rem">${next.specialty} • ${next.date}, ${next.time}</span>
        </div>`;
        nextText.classList.remove('empty');
    }

    // Daily Meds
    const medRes = await fetch(`${API_URL}/medications/${currentUser.id}`);
    const meds = await medRes.json();
    const dashList = document.getElementById('dash-meds-list');
    dashList.innerHTML = meds.length ? '' : '<p class="empty-msg">No medications set for today.</p>';
    
    meds.forEach(m => {
        dashList.innerHTML += `
            <div class="list-item">
                <div class="item-info"><strong>${m.name}</strong><br><span>${m.dosage} • ${m.time}</span></div>
                <input type="checkbox" ${m.isTaken ? 'checked' : ''} onclick="toggleMed('${m.id}', this.checked)">
            </div>
        `;
    });
}

async function loadDoctors() {
    const res = await fetch(`${API_URL}/doctors`);
    doctors = await res.json();
    renderDoctors(doctors);
}

function renderDoctors(list) {
    const grid = document.getElementById('doctors-grid');
    grid.innerHTML = '';
    list.forEach(doc => {
        grid.innerHTML += `
            <div class="doctor-card" onclick="openBooking('${doc.id}')">
                <div class="doc-header">
                    <div class="doc-avatar" style="background:${doc.color || '#008080'}">${doc.name.charAt(0)}</div>
                    <div class="doc-info">
                        <h4>${doc.name}</h4>
                        <p>${doc.specialty}</p>
                    </div>
                </div>
                <div class="doc-meta">
                    <p>📍 ${doc.clinic}</p>
                    <p>⭐ ${doc.rating} (${doc.reviews} reviews)</p>
                </div>
                <button class="btn-book">View Profile</button>
            </div>
        `;
    });
}

function filterDoctors() {
    const q = document.getElementById('doctor-search').value.toLowerCase();
    const filtered = doctors.filter(d => 
        d.name.toLowerCase().includes(q) || d.specialty.toLowerCase().includes(q)
    );
    renderDoctors(filtered);
}

// User Action Handlers
function openBooking(id) {
    const doc = doctors.find(d => d.id == id);
    const modal = document.getElementById('booking-modal');
    modal.classList.remove('hidden');
    document.getElementById('booking-details').innerHTML = `
        <h2>${doc.name}</h2>
        <p>${doc.specialty} Specialist</p>
    `;
    const slots = document.getElementById('modal-slots');
    slots.innerHTML = '';
    doc.timeSlots.forEach(s => {
        slots.innerHTML += `<button class="slot-btn" onclick="selectSlot(this)">${s}</button>`;
    });
    
    document.getElementById('btn-confirm-booking').onclick = () => confirmBooking(doc);
}

async function confirmBooking(doc) {
    const selected = document.querySelector('.slot-btn.active');
    if(!selected) return alert("Select a time slot");
    
    const appt = {
        userId: currentUser.id,
        doctorName: doc.name,
        specialty: doc.specialty,
        clinic: doc.clinic,
        date: "Today",
        time: selected.textContent,
        status: "upcoming"
    };

    const res = await fetch(`${API_URL}/appointments`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(appt)
    });
    if(res.ok) {
        alert("Appointment booked successfully!");
        closeModal('booking-modal');
        showTab('dashboard');
    }
}

function selectSlot(el) {
    document.querySelectorAll('.slot-btn').forEach(b => b.classList.remove('active'));
    el.classList.add('active');
}

function closeModal(id) { document.getElementById(id).classList.add('hidden'); }

// Profile Methods
async function saveProfileChanges() {
    const updated = {
        ...currentUser,
        phone: document.getElementById('p-phone').value,
        emergencyName: document.getElementById('p-em-name').value,
        emergencyPhone: document.getElementById('p-em-phone').value
    };
    
    const res = await fetch(`${API_URL}/auth/profile/${currentUser.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updated)
    });
    if(res.ok) {
        currentUser = await res.json();
        localStorage.setItem('medilink_user', JSON.stringify(currentUser));
        alert("Profile updated!");
    }
}

function loadProfile() {
    document.getElementById('p-name').textContent = currentUser.name;
    document.getElementById('p-email').textContent = currentUser.email;
    document.getElementById('p-phone').value = currentUser.phone || '';
    document.getElementById('p-em-name').value = currentUser.emergencyName || '';
    document.getElementById('p-em-phone').value = currentUser.emergencyPhone || '';
    if (currentUser.profilePic) {
        document.getElementById('p-avatar').src = currentUser.profilePic;
        document.getElementById('header-profile-pic').src = currentUser.profilePic;
    }
}

function uploadPhoto(input) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();
        reader.onload = async function (e) {
            document.getElementById('p-avatar').src = e.target.result;
            document.getElementById('header-profile-pic').src = e.target.result;
            
            // Save to currentUser and backend
            const updated = { ...currentUser, profilePic: e.target.result };
            const res = await fetch(`${API_URL}/auth/profile/${currentUser.id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(updated)
            });
            if (res.ok) {
                currentUser = await res.json();
                localStorage.setItem('medilink_user', JSON.stringify(currentUser));
                alert("Profile photo updated!");
            }
        };
        reader.readAsDataURL(input.files[0]);
    }
}

// Medication Handlers
async function loadMedications() {
    const res = await fetch(`${API_URL}/medications/${currentUser.id}`);
    const meds = await res.json();
    const list = document.getElementById('meds-list');
    list.innerHTML = '';
    if(meds.length === 0) {
        list.innerHTML = '<p class="empty-msg">No medications added.</p>';
        return;
    }
    meds.forEach(m => {
        list.innerHTML += `
            <div class="list-item">
                <div class="item-info"><strong>${m.name}</strong><br><span>${m.dosage} • ${m.time}</span></div>
                <input type="checkbox" ${m.isTaken ? 'checked' : ''} onclick="toggleMed('${m.id}', this.checked)">
            </div>
        `;
    });
}

function showAddMedDialog() {
    document.getElementById('med-modal').classList.remove('hidden');
}

async function addMedication() {
    const name = document.getElementById('m-name').value;
    const dosage = document.getElementById('m-dosage').value;
    const time = document.getElementById('m-time').value;

    if (!name || !dosage || !time) {
        alert("Please fill in all medication details.");
        return;
    }

    const med = {
        userId: currentUser.id,
        name,
        dosage,
        time,
        isTaken: false
    };

    const res = await fetch(`${API_URL}/medications`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(med)
    });
    
    if(res.ok) {
        alert("Medication added!");
        closeModal('med-modal');
        loadMedications();
        if(document.getElementById('tab-dashboard').classList.contains('active')) {
            loadDashboardData();
        }
    }
}

async function toggleMed(id, isTaken) {
    const res = await fetch(`${API_URL}/medications/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ isTaken })
    });
    if(res.ok) {
        if(document.getElementById('tab-medication').classList.contains('active')) {
            loadMedications();
        } else {
            loadDashboardData();
        }
    }
}

// History Methods
async function loadHistory() {
    const res = await fetch(`${API_URL}/appointments/${currentUser.id}`);
    const appts = await res.json();
    const list = document.getElementById('history-list');
    list.innerHTML = '';
    if(appts.length === 0) {
        list.innerHTML = '<p class="empty-msg">No appointment history.</p>';
        return;
    }
    appts.forEach(a => {
        list.innerHTML += `
            <div class="list-item">
                <div class="item-info">
                    <strong>${a.doctorName}</strong>
                    <br><span>${a.specialty} • ${a.date}, ${a.time}</span>
                </div>
                <div><span class="badge" style="background:${a.status === 'upcoming' ? '#008080' : '#E2E8F0'}; color: ${a.status === 'upcoming' ? 'white' : 'black'}; padding: 4px 8px; border-radius: 4px; font-size: 0.8rem;">${a.status.toUpperCase()}</span></div>
            </div>
        `;
    });
}

// Admin Logic
async function loadAdminData() {
    const res = await fetch(`${API_URL}/users`);
    const users = await res.json();
    document.getElementById('admin-stats-summary').textContent = `Total System Users: ${users.length}`;
    
    const rows = document.getElementById('user-rows');
    rows.innerHTML = '';
    users.forEach(u => {
        rows.innerHTML += `
            <tr>
                <td><strong>${u.name}</strong></td>
                <td>${u.email}</td>
                <td><span class="badge badge-${u.role}">${u.role}</span></td>
                <td>${new Date().toLocaleDateString()}</td>
            </tr>
        `;
    });
}

function setupChart() {
    const ctx = document.getElementById('medsChart').getContext('2d');
    medsChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Taken', 'Pending'],
            datasets: [{
                data: [65, 35],
                backgroundColor: ['#008080', '#E2E8F0'],
                borderWidth: 0
            }]
        },
        options: { cutout: '80%', plugins: { legend: { display: false } } }
    });
}
