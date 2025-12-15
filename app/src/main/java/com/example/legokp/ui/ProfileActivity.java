package com.example.legokp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.legokp.R;
import com.example.legokp.utils.SessionManager;
import com.example.legokp.viewmodels.LegoViewModel;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail, tvUserId, tvMemberSince;
    private TextView tvTotalSets, tvFavoriteCount;
    private Button btnEditProfile, btnChangePassword, btnLogout;
    private SessionManager sessionManager;
    private LegoViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(LegoViewModel.class);

        setupToolbar();
        initViews();
        loadUserData();
        setupListeners();
        loadStatistics();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Profile");
        }
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvUserId = findViewById(R.id.tvUserId);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        tvTotalSets = findViewById(R.id.tvTotalSets);
        tvFavoriteCount = findViewById(R.id.tvFavoriteCount);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadUserData() {
        String username = sessionManager.getUsername();
        String email = sessionManager.getEmail();
        String userId = sessionManager.getUserId();

        tvUsername.setText(username);
        tvEmail.setText(email);
        tvUserId.setText("ID: " + userId);
        tvMemberSince.setText("Member since 2024");
    }

    private void loadStatistics() {
        // Get total sets count
        viewModel.getTotalSetsCount(count -> {
            runOnUiThread(() -> tvTotalSets.setText(String.valueOf(count)));
        });

        // Get favorite count
        viewModel.getFavoriteCount(count -> {
            runOnUiThread(() -> tvFavoriteCount.setText(String.valueOf(count)));
        });
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Profile - Coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Change Password - Coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    sessionManager.clearSession();
                    navigateToLogin();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}