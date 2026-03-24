package com.medilink.app.models;

import java.io.Serializable;

public class Appointment implements Serializable {
    private String id;
    private String userId;
    private String doctorName;
    private String specialty;
    private String clinic;
    private String date;
    private String time;
    private String status; // upcoming, completed, cancelled
    private String color;

    public Appointment(String id, String userId, String doctorName, String specialty, String clinic, String date, String time, String status, String color) {
        this.id = id;
        this.userId = userId;
        this.doctorName = doctorName;
        this.specialty = specialty;
        this.clinic = clinic;
        this.date = date;
        this.time = time;
        this.status = status;
        this.color = color;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getDoctorName() { return doctorName; }
    public String getSpecialty() { return specialty; }
    public String getClinic() { return clinic; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getColor() { return color; }
}
