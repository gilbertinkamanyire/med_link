package com.medilink.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.medilink.app.R;
import com.medilink.app.adapters.AppointmentAdapter;
import com.medilink.app.adapters.MedicationAdapter;
import com.medilink.app.api.ApiClient;
import com.medilink.app.api.ApiService;
import com.medilink.app.models.Appointment;
import com.medilink.app.models.Medication;
import com.medilink.app.models.User;
import com.medilink.app.database.DatabaseHelper;
import com.medilink.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private ApiService apiService;
    private User currentUser;

    private AppointmentAdapter appointmentAdapter;
    private MedicationAdapter medicationAdapter;

    private RecyclerView rvAppointments, rvMedications;
    private TextView tvNoAppointments, tvNoMeds;

    private final ActivityResultLauncher<Intent> scannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String scanData = result.getData().getStringExtra("SCAN_RESULT");
                    handleScanResult(scanData);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String storedUrl = sessionManager.getBaseUrl();
        if (storedUrl != null) {
            ApiClient.setBaseUrl(storedUrl);
        }

        currentUser = sessionManager.getUserDetails();

        setupGreeting();
        setupQuickActions();
        setupRecyclerViews();
        setupBottomNavigation();

        findViewById(R.id.tv_view_all).setOnClickListener(v -> 
            startActivity(new Intent(MainActivity.this, HistoryActivity.class)));
            
        findViewById(R.id.tv_manage_meds).setOnClickListener(v -> 
            startActivity(new Intent(MainActivity.this, MedicationActivity.class)));

        findViewById(R.id.iv_profile_icon).setOnClickListener(v -> 
            startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        findViewById(R.id.iv_notifications).setOnClickListener(v -> 
            Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show());

        findViewById(R.id.iv_scan_qr).setOnClickListener(v -> 
            scannerLauncher.launch(new Intent(this, ScannerActivity.class)));
    }

    private void handleScanResult(String data) {
        if (data != null && data.startsWith("API_URL:")) {
            String newUrl = data.substring(8).trim();
            if (!newUrl.endsWith("/")) newUrl += "/";
            
            sessionManager.setBaseUrl(newUrl);
            ApiClient.setBaseUrl(newUrl);
            
            new AlertDialog.Builder(this)
                    .setTitle("Configuration Updated")
                    .setMessage("Server URL updated to:\n" + newUrl + "\n\nPlease restart the app if you encounter issues.")
                    .setPositiveButton("OK", (dialog, which) -> fetchDashboardData())
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Smart Campus - Scan Result")
                    .setMessage("Scanned Data:\n" + data)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null) {
            setupGreeting(); // update user name possibly
            fetchDashboardData();
        }
    }

    private void setupGreeting() {
        TextView tvGreeting = findViewById(R.id.tv_greeting);
        TextView tvUserName = findViewById(R.id.tv_user_name);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = getString(R.string.good_morning);
        if (hour >= 12 && hour < 17) {
            greeting = getString(R.string.good_afternoon);
        } else if (hour >= 17) {
            greeting = getString(R.string.good_evening);
        }

        tvGreeting.setText(greeting);
        
        currentUser = sessionManager.getUserDetails(); // get fresh
        if (currentUser != null) {
            tvUserName.setText(currentUser.getName());
        }
    }

    private void setupQuickActions() {
        LinearLayout qaBook = findViewById(R.id.qa_book);
        LinearLayout qaMeds = findViewById(R.id.qa_meds);
        LinearLayout qaHistory = findViewById(R.id.qa_history);
        LinearLayout qaProfile = findViewById(R.id.qa_profile);

        qaBook.setOnClickListener(v -> startActivity(new Intent(this, BookingActivity.class)));
        qaMeds.setOnClickListener(v -> startActivity(new Intent(this, MedicationActivity.class)));
        qaHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        qaProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void setupRecyclerViews() {
        rvAppointments = findViewById(R.id.rv_upcoming_appointments);
        rvMedications = findViewById(R.id.rv_todays_meds);
        tvNoAppointments = findViewById(R.id.tv_no_appointments);
        tvNoMeds = findViewById(R.id.tv_no_meds);

        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvMedications.setLayoutManager(new LinearLayoutManager(this));

        appointmentAdapter = new AppointmentAdapter(new ArrayList<>());
        medicationAdapter = new MedicationAdapter(new ArrayList<>(), sessionManager);

        rvAppointments.setAdapter(appointmentAdapter);
        rvMedications.setAdapter(medicationAdapter);
    }

    private void fetchDashboardData() {
        if (currentUser == null || currentUser.getId() == null) return;

        // Fallback: Load from SQLite first (Instant load)
        loadLocalData();
        
        // Fetch Appointments from Network
        apiService.getAppointments(currentUser.getId()).enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dbHelper.syncAppointments(response.body()); // Sync to SQLite
                    updateAppointmentUI(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                // Already loaded from local, just show a minor toast or silent fail
                Toast.makeText(MainActivity.this, "Connecting to server...", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch Medications from Network
        apiService.getMedications(currentUser.getId()).enqueue(new Callback<List<Medication>>() {
            @Override
            public void onResponse(Call<List<Medication>> call, Response<List<Medication>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dbHelper.syncMedications(response.body()); // Sync to SQLite
                    updateMedicationUI(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Medication>> call, Throwable t) {
                // Already loaded from local
            }
        });
    }

    private void loadLocalData() {
        if (currentUser == null) return;
        List<Appointment> localAppts = dbHelper.getAppointments(currentUser.getId());
        List<Medication> localMeds = dbHelper.getMedications(currentUser.getId());
        
        updateAppointmentUI(localAppts);
        updateMedicationUI(localMeds);
    }

    private void updateAppointmentUI(List<Appointment> allAppts) {
        List<Appointment> upcoming = new ArrayList<>();
        for (Appointment a : allAppts) {
            if ("upcoming".equals(a.getStatus())) {
                upcoming.add(a);
            }
            if (upcoming.size() >= 3) break;
        }

        if (upcoming.isEmpty()) {
            rvAppointments.setVisibility(View.GONE);
            tvNoAppointments.setVisibility(View.VISIBLE);
        } else {
            rvAppointments.setVisibility(View.VISIBLE);
            tvNoAppointments.setVisibility(View.GONE);
            appointmentAdapter.updateData(upcoming);
        }
    }

    private void updateMedicationUI(List<Medication> allMeds) {
        if (allMeds.isEmpty()) {
            rvMedications.setVisibility(View.GONE);
            tvNoMeds.setVisibility(View.VISIBLE);
        } else {
            List<Medication> limitedMeds = allMeds.subList(0, Math.min(allMeds.size(), 3));
            rvMedications.setVisibility(View.VISIBLE);
            tvNoMeds.setVisibility(View.GONE);
            medicationAdapter.updateData(limitedMeds);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_book) {
                startActivity(new Intent(this, BookingActivity.class));
                return true;
            } else if (itemId == R.id.nav_meds) {
                startActivity(new Intent(this, MedicationActivity.class));
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }
}
