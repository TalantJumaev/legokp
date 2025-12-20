package com.example.legokp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.legokp.R;
import com.example.legokp.utils.DatabaseCleaner;
import com.example.legokp.utils.SessionManager;
import com.example.legokp.viewmodels.LegoViewModel;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity"; // ✅ Added for debugging

    private ImageView ivProfileImage;
    private TextView tvUsername, tvEmail, tvUserId, tvMemberSince;
    private TextView tvTotalSets, tvFavoriteCount;
    private Button btnEditProfile, btnChangePassword, btnLogout, btnClearCache;
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
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvUserId = findViewById(R.id.tvUserId);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        tvTotalSets = findViewById(R.id.tvTotalSets);
        tvFavoriteCount = findViewById(R.id.tvFavoriteCount);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
        btnClearCache = findViewById(R.id.btnClearCache);
    }

    private void loadUserData() {
        String username = sessionManager.getUsername();
        String email = sessionManager.getEmail();
        String userId = sessionManager.getUserId();
        String imageUrl = sessionManager.getUserImage();

        // ✅ DEBUG: Print image URL to console
        Log.d(TAG, "=== PROFILE IMAGE DEBUG ===");
        Log.d(TAG, "Image URL from session: " + imageUrl);
        Log.d(TAG, "Username: " + username);
        Log.d(TAG, "Email: " + email);

        tvUsername.setText(username);
        tvEmail.setText(email);
        tvUserId.setText("ID: " + userId);
        tvMemberSince.setText("Member since 2024");

        // Load profile image
        loadProfileImage(imageUrl);
    }

    private void loadProfileImage(String imageUrl) {
        Log.d(TAG, "loadProfileImage called with URL: " + imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d(TAG, "Loading image from URL...");

            // ✅ IMPROVED: Better error handling and logging
            Glide.with(this)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e,
                                                    Object model,
                                                    com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                    boolean isFirstResource) {
                            Log.e(TAG, "❌ Glide failed to load image: " + imageUrl);
                            if (e != null) {
                                Log.e(TAG, "Error details: " + e.getMessage());
                                e.logRootCauses(TAG);
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                       Object model,
                                                       com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                       com.bumptech.glide.load.DataSource dataSource,
                                                       boolean isFirstResource) {
                            Log.d(TAG, "✅ Glide successfully loaded image from: " + dataSource);
                            return false;
                        }
                    })
                    .into(ivProfileImage);
        } else {
            Log.w(TAG, "⚠️ Image URL is null or empty, using default icon");
            ivProfileImage.setImageResource(R.drawable.ic_profile);
        }
    }

    private void loadStatistics() {
        viewModel.getTotalSetsCount(count -> {
            runOnUiThread(() -> tvTotalSets.setText(String.valueOf(count)));
        });

        viewModel.getFavoriteCount(count -> {
            runOnUiThread(() -> tvFavoriteCount.setText(String.valueOf(count)));
        });

        DatabaseCleaner.printStats(this);
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Profile - Coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Change Password - Coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        btnClearCache.setOnClickListener(v -> showClearCacheDialog());
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

    private void showClearCacheDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Cache")
                .setMessage("This will remove all non-favorite sets from local database. Your favorites will be kept. Continue?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    DatabaseCleaner.cleanNonFavorites(this);
                    Toast.makeText(this, "Cache cleared! Favorites preserved.", Toast.LENGTH_SHORT).show();

                    loadStatistics();
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