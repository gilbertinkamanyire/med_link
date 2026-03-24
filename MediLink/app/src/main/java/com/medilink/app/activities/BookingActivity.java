package com.medilink.app.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.medilink.app.R;
import com.medilink.app.adapters.DoctorAdapter;
import com.medilink.app.database.DatabaseHelper;
import com.medilink.app.api.ApiClient;
import com.medilink.app.api.ApiService;
import com.medilink.app.models.Appointment;
import com.medilink.app.models.Doctor;
import com.medilink.app.utils.MockData;
import com.medilink.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingActivity extends AppCompatActivity {

    private RecyclerView rvDoctors;
    private DoctorAdapter adapter;
    private List<Doctor> allDoctors;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);
        apiService = ApiClient.getClient().create(ApiService.class);
        
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        rvDoctors = findViewById(R.id.rv_doctors);
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));

        allDoctors = new ArrayList<>();
        adapter = new DoctorAdapter(allDoctors, this::showBookingDialog);
        rvDoctors.setAdapter(adapter);

        setupSearchAndFilter();
        fetchDoctors();
    }

    private void fetchDoctors() {
        apiService.getDoctors().enqueue(new Callback<List<Doctor>>() {
            @Override
            public void onResponse(Call<List<Doctor>> call, Response<List<Doctor>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    allDoctors = response.body();
                    adapter.updateList(allDoctors);
                } else {
                    Toast.makeText(BookingActivity.this, "Failed to load doctors", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Doctor>> call, Throwable t) {
                Toast.makeText(BookingActivity.this, "Network Error. Loading offline cache...", Toast.LENGTH_SHORT).show();
                allDoctors = MockData.getDoctors();
                adapter.updateList(allDoctors);
            }
        });
    }

    private void setupSearchAndFilter() {
        TextInputEditText etSearch = findViewById(R.id.et_search);
        ChipGroup chipGroup = findViewById(R.id.chip_group_specialty);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctors(s.toString(), getSelectedSpecialty(chipGroup));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filterDoctors(etSearch.getText().toString(), getSelectedSpecialty(group));
        });
    }

    private String getSelectedSpecialty(ChipGroup group) {
        int id = group.getCheckedChipId();
        if (id == R.id.chip_all) return "All";
        findViewById(id);
        Chip chip = findViewById(id);
        if(chip != null) return chip.getText().toString();
        return "All";
    }

    private void filterDoctors(String query, String specialty) {
        List<Doctor> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Doctor doctor : allDoctors) {
            boolean matchesSearch = doctor.getName().toLowerCase().contains(lowerQuery) ||
                                    doctor.getClinic().toLowerCase().contains(lowerQuery);
            boolean matchesSpecialty = "All".equals(specialty) || doctor.getSpecialty().equals(specialty);

            if (matchesSearch && matchesSpecialty) {
                filtered.add(doctor);
            }
        }
        adapter.updateList(filtered);
    }

    private void showBookingDialog(Doctor doctor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_book_appointment, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ((TextView) view.findViewById(R.id.tv_dialog_doctor_initials)).setText(MockData.getInitials(doctor.getName()));
        ((TextView) view.findViewById(R.id.tv_dialog_doctor_name)).setText(doctor.getName());
        ((TextView) view.findViewById(R.id.tv_dialog_specialty)).setText(doctor.getSpecialty());

        // Color
        try {
            ((TextView) view.findViewById(R.id.tv_dialog_doctor_initials)).setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(doctor.getColor())));
        } catch (Exception e) {}

        GridLayout gridSlots = view.findViewById(R.id.grid_time_slots);
        final String[] selectedTime = {null};

        for (String time : doctor.getTimeSlots()) {
            TextView timeView = new TextView(this);
            timeView.setText(time);
            timeView.setBackgroundResource(R.drawable.time_slot_bg);
            timeView.setPadding(32, 24, 32, 24);
            timeView.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
            timeView.setTextSize(13);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(8, 8, 8, 8);
            timeView.setLayoutParams(params);
            timeView.setClickable(true);
            timeView.setFocusable(true);

            timeView.setOnClickListener(v -> {
                for (int i = 0; i < gridSlots.getChildCount(); i++) {
                    gridSlots.getChildAt(i).setSelected(false);
                    ((TextView)gridSlots.getChildAt(i)).setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
                }
                v.setSelected(true);
                ((TextView)v).setTextColor(getResources().getColor(R.color.white, getTheme()));
                selectedTime[0] = time;
            });

            gridSlots.addView(timeView);
        }

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            if (selectedTime[0] == null) {
                Toast.makeText(this, getString(R.string.select_time), Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = sessionManager.getUserDetails().getId();
            String dateStr = doctor.getNextAvailable().split(", ")[0];
            
            Appointment appointment = new Appointment(
                UUID.randomUUID().toString(),
                userId,
                doctor.getName(), 
                doctor.getSpecialty(), 
                doctor.getClinic(), 
                dateStr, 
                selectedTime[0], 
                "upcoming", 
                doctor.getColor()
            );

            apiService.createAppointment(appointment).enqueue(new Callback<Appointment>() {
                @Override
                public void onResponse(Call<Appointment> call, Response<Appointment> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        dbHelper.syncSingleAppointment(response.body()); // Sync local
                        Toast.makeText(BookingActivity.this, getString(R.string.booking_success), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        finish();
                    } else {
                        Toast.makeText(BookingActivity.this, "Failed to book appointment", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Appointment> call, Throwable t) {
                    // Force save locally if network fails?
                    dbHelper.syncSingleAppointment(appointment);
                    Toast.makeText(BookingActivity.this, "Network Error: Saved locally, will sync later.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    finish();
                }
            });
        });

        dialog.show();
    }
}
