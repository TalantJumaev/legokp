package com.example.legokp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.legokp.R;
import com.example.legokp.models.AuthRequest;
import com.example.legokp.models.AuthResponse;
import com.example.legokp.models.User;
import com.example.legokp.network.RetrofitClient;
import com.example.legokp.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize RetrofitClient with context
        RetrofitClient.init(this);

        sessionManager = new SessionManager(this);

        // Check if already logged in, navigate to main screen
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            etEmail.setError("Email required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Send request
        performLogin(email, password);
    }

    private void performLogin(String email, String password) {
        showLoading(true);

        AuthRequest request = new AuthRequest(email, password);

        RetrofitClient.getApiService().login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                showLoading(false);

                Log.d(TAG, "=== LOGIN RESPONSE DEBUG ===");
                Log.d(TAG, "Response Code: " + response.code());
                Log.d(TAG, "Response Success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    Log.d(TAG, "Response Message: " + authResponse.getMessage());
                    Log.d(TAG, "Response Success Flag: " + authResponse.isSuccess());

                    if (authResponse.isSuccess() && authResponse.getData() != null) {
                        User user = authResponse.getData();

                        // ✅ DEBUG: Print all user data
                        Log.d(TAG, "--- User Data ---");
                        Log.d(TAG, "User ID: " + user.getUserId());
                        Log.d(TAG, "Username: " + user.getUsername());
                        Log.d(TAG, "Email: " + user.getEmail());
                        Log.d(TAG, "Token: " + (user.getToken() != null ? "Present" : "NULL"));
                        Log.d(TAG, "Image URL: " + user.getImage()); // ✅ KEY DEBUG LINE
                        Log.d(TAG, "Image is null? " + (user.getImage() == null));
                        Log.d(TAG, "Image is empty? " + (user.getImage() != null && user.getImage().isEmpty()));

                        // Check if all data is present
                        String userId = user.getUserId() != null ? user.getUserId() : String.valueOf(user.getUserId());
                        String username = user.getUsername() != null ? user.getUsername() : "User";
                        String userEmail = user.getEmail() != null ? user.getEmail() : email;
                        String token = user.getToken() != null ? user.getToken() : "";
                        String imageUrl = user.getImage(); // Get image URL from user object

                        // ✅ DEBUG: Log what we're about to save
                        Log.d(TAG, "--- Saving to Session ---");
                        Log.d(TAG, "Saving Token: " + (token != null ? "Yes" : "No"));
                        Log.d(TAG, "Saving User ID: " + userId);
                        Log.d(TAG, "Saving Username: " + username);
                        Log.d(TAG, "Saving Email: " + userEmail);
                        Log.d(TAG, "Saving Image URL: " + imageUrl);

                        // Save session with image URL
                        sessionManager.saveUserSession(token, userId, username, userEmail, imageUrl);

                        // ✅ DEBUG: Verify what was saved
                        Log.d(TAG, "--- Verifying Saved Data ---");
                        Log.d(TAG, "Retrieved Image URL: " + sessionManager.getUserImage());

                        Toast.makeText(LoginActivity.this,
                                "Welcome, " + username + "!",
                                Toast.LENGTH_SHORT).show();

                        navigateToMain();
                    } else {
                        String errorMsg = authResponse.getMessage() != null ?
                                authResponse.getMessage() : "Login failed";
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Login failed: " + errorMsg);
                    }
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Login failed: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error Response Code: " + response.code());

                    // ✅ DEBUG: Print error body if available
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error Body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(LoginActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Login Failure: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}