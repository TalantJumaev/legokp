package com.example.legokp.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("userId")
    private String userId;

    private String username;
    private String email;
    private String token;
    private String createdAt;

    public User() {}

    public User(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    // Для обратной совместимости
    public String getId() { return userId; }
    public void setId(String id) { this.userId = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}