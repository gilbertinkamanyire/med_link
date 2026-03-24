package com.medilink.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.medilink.app.R;
import com.medilink.app.adapters.AppointmentAdapter;
import com.medilink.app.database.DatabaseHelper;
import com.medilink.app.api.ApiClient;
import com.medilink.app.api.ApiService;
import com.medilink.app.models.Appointment;
import com.medilink.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private ApiService apiService;
    private RecyclerView rvHistory;
    private AppointmentAdapter adapter;
    private LinearLayout llEmptyState;
    private List<Appointment> allAppointments;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);
        apiService = ApiClient.getClient().create(ApiService.class);
        
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        rvHistory = findViewById(R.id.rv_history);
        llEmptyState = findViewById(R.id.ll_empty_state);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        allAppointments = new ArrayList<>();
        adapter = new AppointmentAdapter(new ArrayList<>());
        rvHistory.setAdapter(adapter);

        setupTabs();
        fetchHistory();
    }

    private void fetchHistory() {
        String userId = sessionManager.getUserDetails().getId();
        
        // Initial load from local
        allAppointments = dbHelper.getAppointments(userId);
        applyTabFilter();

        apiService.getAppointments(userId).enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allAppointments = response.body();
                    dbHelper.syncAppointments(allAppointments); // Sync
                    applyTabFilter();
                }
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                Toast.makeText(HistoryActivity.this, "Offline Mode: Showing cached history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyTabFilter() {
        if (tabLayout == null) return;
        int pos = tabLayout.getSelectedTabPosition();
        String status = pos == 0 ? "upcoming" : (pos == 1 ? "completed" : "cancelled");
        filterAppointments(status);
    }

    private void setupTabs() {
        tabLayout = findViewById(R.id.tab_layout);
        
        tabLayout.addTab(tabLayout.newTab().setText(R.string.upcoming));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.completed));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.cancelled));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: filterAppointments("upcoming"); break;
                    case 1: filterAppointments("completed"); break;
                    case 2: filterAppointments("cancelled"); break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterAppointments(String status) {
        List<Appointment> filtered = new ArrayList<>();
        if (allAppointments != null) {
            for (Appointment a : allAppointments) {
                if (status.equals(a.getStatus())) {
                    filtered.add(a);
                }
            }
        }

        if (filtered.isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvHistory.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
            adapter.updateData(filtered);
        }
    }
}
