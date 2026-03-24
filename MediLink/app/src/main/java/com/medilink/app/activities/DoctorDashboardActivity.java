package com.medilink.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.medilink.app.R;
import com.medilink.app.adapters.AppointmentAdapter;
import com.medilink.app.api.ApiClient;
import com.medilink.app.api.ApiService;
import com.medilink.app.models.Appointment;
import com.medilink.app.models.User;
import com.medilink.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorDashboardActivity extends AppCompatActivity {

    private RecyclerView rvAppointments;
    private AppointmentAdapter adapter;
    private SessionManager sessionManager;
    private ApiService apiService;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);
        currentUser = sessionManager.getUserDetails();

        if (currentUser == null || !"doctor".equals(currentUser.getRole())) {
            finish();
            return;
        }

        TextView greeting = findViewById(R.id.tv_doctor_greeting);
        greeting.setText("Welcome, " + currentUser.getName());

        rvAppointments = findViewById(R.id.rv_doctor_appointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(new ArrayList<>());
        rvAppointments.setAdapter(adapter);

        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        fetchAppointments();
    }

    private void fetchAppointments() {
        // In this mock, we use the user's ID to fetch appointments
        // The backend logic is set to return appointments where userId matches or doctor metadata matches
        apiService.getAppointments(currentUser.getId()).enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body());
                } else {
                    Toast.makeText(DoctorDashboardActivity.this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                Toast.makeText(DoctorDashboardActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
