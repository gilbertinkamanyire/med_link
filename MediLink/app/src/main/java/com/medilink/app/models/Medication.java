package com.medilink.app.models;

import java.io.Serializable;

public class Medication implements Serializable {
    private String id;
    private String userId;
    private String name;
    private String dosage;
    private String time;
    private String frequency;
    private String notes;
    private boolean isTaken;

    public Medication(String id, String userId, String name, String dosage, String time, String frequency, String notes) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.frequency = frequency;
        this.notes = notes;
        this.isTaken = false;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getTime() { return time; }
    public String getFrequency() { return frequency; }
    public String getNotes() { return notes; }
    public boolean isTaken() { return isTaken; }
    public void setTaken(boolean taken) { isTaken = taken; }
}
