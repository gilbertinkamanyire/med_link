package com.medilink.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.medilink.app.R;
import com.medilink.app.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager session = new SessionManager(this);
            if (session.isLoggedIn() && session.getUserDetails() != null) {
                String role = session.getUserDetails().getRole();
                if ("admin".equals(role)) {
                    startActivity(new Intent(this, AdminDashboardActivity.class));
                } else if ("doctor".equals(role)) {
                    startActivity(new Intent(this, DoctorDashboardActivity.class));
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                }
                overridePendingTransition(R.anim.fade_in, android.R.anim.fade_out);
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, 2000);
    }
}
