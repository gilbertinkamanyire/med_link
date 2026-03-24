package com.medilink.app.api;

import com.medilink.app.models.Appointment;
import com.medilink.app.models.Doctor;
import com.medilink.app.models.Medication;
import com.medilink.app.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // Auth
    @POST("auth/register")
    Call<User> register(@Body User user);

    @POST("auth/login")
    Call<User> login(@Body User user);

    @PUT("auth/profile/{id}")
    Call<User> updateProfile(@Path("id") String id, @Body User user);

    // Users
    @GET("users")
    Call<List<User>> getUsers();

    // Doctors
    @GET("doctors")
    Call<List<Doctor>> getDoctors();

    // Appointments
    @GET("appointments/{userId}")
    Call<List<Appointment>> getAppointments(@Path("userId") String userId);

    @POST("appointments")
    Call<Appointment> createAppointment(@Body Appointment appointment);

    // Medications
    @GET("medications/{userId}")
    Call<List<Medication>> getMedications(@Path("userId") String userId);

    @POST("medications")
    Call<Medication> addMedication(@Body Medication medication);

    @PUT("medications/{id}")
    Call<Medication> updateMedication(@Path("id") String id, @Body Medication medication);
}
