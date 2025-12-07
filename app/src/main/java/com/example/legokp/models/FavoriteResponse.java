package com.example.legokp.models;

import com.google.gson.annotations.SerializedName;

public class FavoriteResponse {
    private boolean success;

    @SerializedName("is_favorite")
    private boolean isFavorite;

    private String message;

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}