const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');

const app = express();
app.use(cors());
app.use(bodyParser.json({limit: '10mb'})); // Increase limit for base64 images

// In-memory Database
let users = [
    { id: "admin1", name: "System Admin", email: "admin@medilink.com", password: "admin", role: "admin" }
];
let appointments = [];
let medications = [];

// Base mocked doctors, we will also merge these with signed up doctors
const mockedDoctors = [
    { id: "1", name: "Dr. Sarah Johnson", specialty: "General", clinic: "City Health Clinic", rating: 4.8, reviews: 124, nextAvailable: "Today, 10:30 AM", color: "#008080", timeSlots: ["09:00 AM", "10:30 AM", "01:00 PM"] },
    { id: "2", name: "Dr. Emily Davis", specialty: "Dermatology", clinic: "SkinCare Center", rating: 4.9, reviews: 89, nextAvailable: "Tomorrow, 09:00 AM", color: "#7E57C2", timeSlots: ["09:00 AM", "11:00 AM", "02:30 PM"] }
];

// --- AUTH ROUTES ---
app.post('/api/auth/register', (req, res) => {
    const { name, email, phone, password, role } = req.body;
    if (users.find(u => u.email === email)) {
        return res.status(400).json({ error: "Email already exists" });
    }
    // Default role is patient if not provided
    const userRole = role || 'patient';
    const newUser = { 
        id: Date.now().toString(), 
        name, 
        email, 
        phone, 
        password, 
        role: userRole,
        dob: "", 
        gender: "", 
        emergencyName: "", 
        emergencyPhone: "",
        profilePic: "",
        specialty: userRole === 'doctor' ? "General" : undefined,
        clinic: userRole === 'doctor' ? "MediLink Clinic" : undefined
    };
    users.push(newUser);
    res.status(201).json(newUser);
});

app.post('/api/auth/login', (req, res) => {
    const { email, password } = req.body;
    const user = users.find(u => u.email === email && u.password === password);
    if (!user) {
        return res.status(401).json({ error: "Invalid credentials" });
    }
    res.status(200).json(user);
});

app.put('/api/auth/profile/:id', (req, res) => {
    const index = users.findIndex(u => u.id === req.params.id);
    if (index === -1) return res.status(404).json({ error: "User not found" });
    users[index] = { ...users[index], ...req.body };
    res.status(200).json(users[index]);
});

// Admin Route
app.get('/api/users', (req, res) => {
    res.status(200).json(users);
});

// --- DOCTOR ROUTES ---
app.get('/api/doctors', (req, res) => {
    // Combine mocked doctors with registered doctors
    const registeredDoctors = users
        .filter(u => u.role === 'doctor')
        .map(d => ({
            id: d.id,
            name: d.name,
            specialty: d.specialty || "General",
            clinic: d.clinic || "Independent",
            rating: 5.0,
            reviews: 0,
            nextAvailable: "Today",
            color: "#4CAF50",
            timeSlots: ["09:00 AM", "12:00 PM"]
        }));
    res.status(200).json([...mockedDoctors, ...registeredDoctors]);
});

// --- APPOINTMENT ROUTES ---
app.get('/api/appointments/:userId', (req, res) => {
    // Returns appointments where the user is either the patient or the doctor
    const userAppts = appointments.filter(a => a.userId === req.params.userId || a.doctorName.includes(req.params.userId));
    res.status(200).json(userAppts);
});

app.post('/api/appointments', (req, res) => {
    const appt = { id: Date.now().toString(), ...req.body };
    appointments.push(appt);
    res.status(201).json(appt);
});

// --- MEDICATION ROUTES ---
app.get('/api/medications/:userId', (req, res) => {
    const userMeds = medications.filter(m => m.userId === req.params.userId);
    res.status(200).json(userMeds);
});

app.post('/api/medications', (req, res) => {
    const med = { id: Date.now().toString(), isTaken: false, ...req.body };
    medications.push(med);
    res.status(201).json(med);
});

app.put('/api/medications/:id', (req, res) => {
    const index = medications.findIndex(m => m.id === req.params.id);
    if (index === -1) return res.status(404).json({ error: "Medication not found" });
    medications[index] = { ...medications[index], ...req.body };
    res.status(200).json(medications[index]);
});

const PORT = 3000;
app.listen(PORT, () => console.log(`Backend server running on port ${PORT}`));
