package com.medilink.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.medilink.app.R;
import com.medilink.app.api.ApiClient;
import com.medilink.app.api.ApiService;
import com.medilink.app.models.User;
import com.medilink.app.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private RadioGroup rgRole;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        etName = findViewById(R.id.et_signup_name);
        etEmail = findViewById(R.id.et_signup_email);
        etPhone = findViewById(R.id.et_signup_phone);
        etPassword = findViewById(R.id.et_signup_password);
        etConfirmPassword = findViewById(R.id.et_signup_confirm_password);
        rgRole = findViewById(R.id.rg_role);

        Button btnSignup = findViewById(R.id.btn_signup);
        TextView tvGoLogin = findViewById(R.id.tv_go_login);

        btnSignup.setOnClickListener(v -> registerUser());

        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, getString(R.string.passwords_mismatch), Toast.LENGTH_SHORT).show();
            return;
        }

        String roleTemp = "patient";
        if (rgRole != null) {
            int selectedId = rgRole.getCheckedRadioButtonId();
            if (selectedId == R.id.rb_doctor) {
                roleTemp = "doctor";
            }
        }
        final String role = roleTemp;

        User newUser = new User(name, email, phone, password);
        newUser.setRole(role);

        apiService.register(newUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sessionManager.createLoginSession(response.body());
                    Toast.makeText(SignupActivity.this, getString(R.string.account_created), Toast.LENGTH_SHORT).show();
                    
                    if ("doctor".equals(role)) {
                        startActivity(new Intent(SignupActivity.this, DoctorDashboardActivity.class));
                    } else if ("admin".equals(role)) {
                        startActivity(new Intent(SignupActivity.this, AdminDashboardActivity.class));
                    } else {
                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                    }
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, "Registration failed or email exists.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
