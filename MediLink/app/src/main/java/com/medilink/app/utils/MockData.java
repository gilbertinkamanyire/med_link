package com.medilink.app.utils;

import com.medilink.app.models.Doctor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockData {

    public static List<Doctor> getDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        List<String> times = Arrays.asList("09:00 AM", "10:30 AM", "01:00 PM", "02:30 PM", "04:00 PM");

        doctors.add(new Doctor("1", "Dr. Sarah Johnson", "General", "City Health Clinic", 4.8, 124, "Today, 10:30 AM", "#008080", times));
        doctors.add(new Doctor("2", "Dr. Emily Davis", "Dermatology", "SkinCare Center", 4.9, 89, "Tomorrow, 09:00 AM", "#7E57C2", times));
        doctors.add(new Doctor("3", "Dr. Michael Chen", "Cardiology", "Heart Institute", 4.7, 210, "Mar 16, 02:00 PM", "#FF7043", times));
        doctors.add(new Doctor("4", "Dr. James Wilson", "Pediatrics", "Kids Med Care", 4.9, 156, "Today, 04:00 PM", "#4CAF50", times));
        doctors.add(new Doctor("5", "Dr. Lisa Wong", "Neurology", "Neuro Health", 4.6, 92, "Mar 18, 11:00 AM", "#FF5722", times));
        doctors.add(new Doctor("6", "Dr. Robert Smith", "General", "Community Care", 4.5, 310, "Today, 01:00 PM", "#009688", times));

        return doctors;
    }

    public static String getInitials(String name) {
        String[] parts = name.replace("Dr. ", "").split(" ");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else if (parts.length == 1 && parts[0].length() >= 2) {
            return parts[0].substring(0, 2).toUpperCase();
        }
        return "DR";
    }
}
