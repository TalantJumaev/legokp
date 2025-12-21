package com.example.legokp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.legokp.R;
import com.example.legokp.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail;
    private Button btnEditProfile, btnChangePassword, btnLogout;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupListeners();
        loadUserProfile();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Profile Clicked", Toast.LENGTH_SHORT).show();
        });

        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Change Password Clicked", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    private void loadUserProfile() {
        if (sessionManager.isLoggedIn()) {
            tvUsername.setText(sessionManager.getUsername());
            // ✨ ИСПРАВЛЕНО: Используем правильный метод getEmail()
            tvEmail.setText(sessionManager.getEmail());
        } else {
            // Redirect to login if not logged in
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    sessionManager.clearSession(); // ИСПРАВЛЕНО: Используем clearSession вместо logoutUser
                    // Redirect to Login Screen
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}