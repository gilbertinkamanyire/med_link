package com.medilink.app.activities;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.medilink.app.R;
import com.medilink.app.adapters.MedicationAdapter;
import com.medilink.app.database.DatabaseHelper;
import com.medilink.app.api.ApiClient;
import com.medilink.app.api.ApiService;
import com.medilink.app.models.Medication;
import com.medilink.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicationActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private ApiService apiService;
    private RecyclerView rvMedications;
    private MedicationAdapter adapter;
    private LinearLayout llEmptyState;
    private List<Medication> allMedications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication);

        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);
        apiService = ApiClient.getClient().create(ApiService.class);
        
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_med).setOnClickListener(v -> showAddMedicationDialog());

        rvMedications = findViewById(R.id.rv_medications);
        llEmptyState = findViewById(R.id.ll_empty_state);

        rvMedications.setLayoutManager(new LinearLayoutManager(this));
        
        allMedications = new ArrayList<>();
        adapter = new MedicationAdapter(allMedications, sessionManager);
        rvMedications.setAdapter(adapter);

        loadMedications();
    }

    private void loadMedications() {
        String userId = sessionManager.getUserDetails().getId();
        
        // Initial load from local
        allMedications = dbHelper.getMedications(userId);
        updateUI();

        apiService.getMedications(userId).enqueue(new Callback<List<Medication>>() {
            @Override
            public void onResponse(Call<List<Medication>> call, Response<List<Medication>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allMedications = response.body();
                    dbHelper.syncMedications(allMedications); // Sync
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<List<Medication>> call, Throwable t) {
                Toast.makeText(MedicationActivity.this, "Offline Mode: Showing cached medications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        adapter.updateData(allMedications);
        if (allMedications.isEmpty()) {
            rvMedications.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvMedications.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    private void showAddMedicationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_medication, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextInputEditText etName = view.findViewById(R.id.et_dialog_med_name);
        TextInputEditText etDosage = view.findViewById(R.id.et_dialog_med_dosage);
        TextInputEditText etTime = view.findViewById(R.id.et_dialog_med_time);
        Spinner spFreq = view.findViewById(R.id.spinner_frequency);
        TextInputEditText etNotes = view.findViewById(R.id.et_dialog_med_notes);

        String[] frequencies = {getString(R.string.daily), getString(R.string.twice_daily), 
                                getString(R.string.weekly), getString(R.string.as_needed)};
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, frequencies);
        spFreq.setAdapter(freqAdapter);

        etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view1, hourOfDay, minute) -> {
                        String ampm = hourOfDay >= 12 ? "PM" : "AM";
                        int hr = hourOfDay > 12 ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
                        etTime.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hr, minute, ampm));
                    },
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
            timePickerDialog.show();
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.btn_add).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String dosage = etDosage.getText().toString().trim();
            String time = etTime.getText().toString().trim();
            String freq = spFreq.getSelectedItem().toString();
            String notes = etNotes.getText().toString().trim();

            if (name.isEmpty() || dosage.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = sessionManager.getUserDetails().getId();
            Medication medication = new Medication(UUID.randomUUID().toString(), userId, name, dosage, time, freq, notes);

            apiService.addMedication(medication).enqueue(new Callback<Medication>() {
                @Override
                public void onResponse(Call<Medication> call, Response<Medication> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        dbHelper.syncSingleMedication(response.body()); // Sync local
                        Toast.makeText(MedicationActivity.this, getString(R.string.med_added), Toast.LENGTH_SHORT).show();
                        loadMedications();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(MedicationActivity.this, "Failed to add medication", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Medication> call, Throwable t) {
                    // Even if network fails, we could potentially save locally to sync later?
                    // For now, just sync locally and tell user it will sync with server later.
                    dbHelper.syncSingleMedication(medication);
                    Toast.makeText(MedicationActivity.this, "Saved locally. Will sync with server later.", Toast.LENGTH_SHORT).show();
                    loadMedications();
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }
}
