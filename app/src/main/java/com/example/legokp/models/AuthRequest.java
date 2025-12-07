package com.example.legokp.models;

public class AuthRequest {
    private String email;
    private String password;
    private String username; // только для регистрации

    // Конструктор для логина
    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Конструктор для регистрации
    public AuthRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}