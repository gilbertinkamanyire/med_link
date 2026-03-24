package com.medilink.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.medilink.app.R;
import com.medilink.app.adapters.UserAdapter;
import com.medilink.app.api.ApiClient;
import com.medilink.app.api.ApiService;
import com.medilink.app.models.User;
import com.medilink.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private UserAdapter adapter;
    private ApiService apiService;
    private TextView tvStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        apiService = ApiClient.getClient().create(ApiService.class);
        tvStats = findViewById(R.id.tv_admin_stats);

        rvUsers = findViewById(R.id.rv_admin_users);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(new ArrayList<>());
        rvUsers.setAdapter(adapter);

        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            new SessionManager(this).logoutUser();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        fetchUsers();
    }

    private void fetchUsers() {
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> userList = response.body();
                    adapter.updateData(userList);
                    updateStats(userList);
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStats(List<User> users) {
        int patienceCount = 0;
        int doctorCount = 0;
        int adminCount = 0;

        for (User u : users) {
            if ("admin".equals(u.getRole())) adminCount++;
            else if ("doctor".equals(u.getRole())) doctorCount++;
            else patienceCount++;
        }

        tvStats.setText(String.format("Total: %d | Patients: %d | Doctors: %d | Admins: %d", 
                users.size(), patienceCount, doctorCount, adminCount));
    }
}
