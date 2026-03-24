package com.medilink.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.medilink.app.models.Appointment;
import com.medilink.app.models.Medication;
import com.medilink.app.models.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static final String PREF_NAME = "MediLinkPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER = "user";
    private static final String KEY_APPOINTMENTS = "appointments";
    private static final String KEY_MEDICATIONS = "medications";
    private static final String KEY_BASE_URL = "baseUrl";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private Gson gson;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }

    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER, gson.toJson(user));
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public User getUserDetails() {
        String userJson = pref.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    public void updateUserDetails(User user) {
        editor.putString(KEY_USER, gson.toJson(user));
        editor.commit();
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
    }

    // Appointment Methods
    public void saveAppointment(Appointment appointment) {
        List<Appointment> appointments = getAppointments();
        appointments.add(appointment);
        editor.putString(KEY_APPOINTMENTS, gson.toJson(appointments));
        editor.commit();
    }

    public List<Appointment> getAppointments() {
        String json = pref.getString(KEY_APPOINTMENTS, null);
        if (json != null) {
            Type type = new TypeToken<List<Appointment>>() {}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    public void updateAppointments(List<Appointment> appointments) {
        editor.putString(KEY_APPOINTMENTS, gson.toJson(appointments));
        editor.commit();
    }

    // Medication Methods
    public void saveMedication(Medication medication) {
        List<Medication> medications = getMedications();
        medications.add(medication);
        editor.putString(KEY_MEDICATIONS, gson.toJson(medications));
        editor.commit();
    }

    public List<Medication> getMedications() {
        String json = pref.getString(KEY_MEDICATIONS, null);
        if (json != null) {
            Type type = new TypeToken<List<Medication>>() {}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    public void updateMedications(List<Medication> medications) {
        editor.putString(KEY_MEDICATIONS, gson.toJson(medications));
        editor.commit();
    }

    public void setBaseUrl(String url) {
        editor.putString(KEY_BASE_URL, url);
        editor.apply();
    }

    public String getBaseUrl() {
        return pref.getString(KEY_BASE_URL, null);
    }
}
