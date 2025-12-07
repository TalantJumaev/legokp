package com.example.legokp.models;

public class AuthResponse {
    private boolean success;
    private String message;
    private User data;  // ИЗМЕНЕНО: было "user", теперь "data"

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public User getData() { return data; }
    public void setData(User data) { this.data = data; }

    // Для обратной совместимости с кодом
    public User getUser() { return data; }
    public void setUser(User user) { this.data = user; }

    public String getToken() {
        return data != null ? data.getToken() : null;
    }
}
