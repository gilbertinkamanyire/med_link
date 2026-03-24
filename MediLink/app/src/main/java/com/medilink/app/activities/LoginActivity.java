package com.medilink.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.medilink.app.R;
import com.medilink.app.api.ApiClient;
import com.medilink.app.api.ApiService;
import com.medilink.app.database.DatabaseHelper;
import com.medilink.app.models.User;
import com.medilink.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private ApiService apiService;

    private final ActivityResultLauncher<Intent> scannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    processConfigScan(result.getData().getStringExtra("SCAN_RESULT"));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        String storedUrl = sessionManager.getBaseUrl();
        if (storedUrl != null) {
            ApiClient.setBaseUrl(storedUrl);
        }
        apiService = ApiClient.getClient().create(ApiService.class);

        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvGoSignup = findViewById(R.id.tv_go_signup);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);

        btnLogin.setOnClickListener(v -> loginUser());
        
        tvGoSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });

        tvForgotPassword.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Forgot Password")
                .setMessage("Please contact support at support@medilink.com to reset your password.")
                .setPositiveButton("OK", null)
                .show();
        });

        findViewById(R.id.btn_config).setOnClickListener(v -> 
            scannerLauncher.launch(new Intent(this, ScannerActivity.class)));
    }

    private void processConfigScan(String data) {
        if (data != null && data.startsWith("API_URL:")) {
            String newUrl = data.substring(8).trim();
            if (!newUrl.endsWith("/")) newUrl += "/";
            
            sessionManager.setBaseUrl(newUrl);
            ApiClient.setBaseUrl(newUrl);
            apiService = ApiClient.getClient().create(ApiService.class); // Refresh API service
            
            new AlertDialog.Builder(this)
                    .setTitle("Server Configured")
                    .setMessage("Connected to:\n" + newUrl)
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            Toast.makeText(this, "Invalid configuration code", Toast.LENGTH_SHORT).show();
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
            return;
        }

        User loginRequest = new User();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        apiService.login(loginRequest).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    dbHelper.syncUser(user); // Sync to SQLite
                    sessionManager.createLoginSession(user);
                    Toast.makeText(LoginActivity.this, getString(R.string.welcome_toast), Toast.LENGTH_SHORT).show();
                    openDashboard(user.getRole());
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Try local login fallback (if user was already synced)
                User localUser = dbHelper.getUserByEmail(email);
                if (localUser != null && localUser.getPassword().equals(password)) {
                    sessionManager.createLoginSession(localUser);
                    openDashboard(localUser.getRole());
                    Toast.makeText(LoginActivity.this, "Offline Login Successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Network Error: Cannot connect to server.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openDashboard(String role) {
        if ("admin".equals(role)) {
            startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
        } else if ("doctor".equals(role)) {
            startActivity(new Intent(LoginActivity.this, DoctorDashboardActivity.class));
        } else {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
        finish();
    }
}
