package com.medilink.app.models;

import java.io.Serializable;
import java.util.List;

public class Doctor implements Serializable {
    private String id;
    private String name;
    private String specialty;
    private String clinic;
    private double rating;
    private int reviews;
    private String nextAvailable;
    private String color;
    private List<String> timeSlots;

    public Doctor(String id, String name, String specialty, String clinic, double rating, int reviews, String nextAvailable, String color, List<String> timeSlots) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.clinic = clinic;
        this.rating = rating;
        this.reviews = reviews;
        this.nextAvailable = nextAvailable;
        this.color = color;
        this.timeSlots = timeSlots;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpecialty() { return specialty; }
    public String getClinic() { return clinic; }
    public double getRating() { return rating; }
    public int getReviews() { return reviews; }
    public String getNextAvailable() { return nextAvailable; }
    public String getColor() { return color; }
    public List<String> getTimeSlots() { return timeSlots; }
}
