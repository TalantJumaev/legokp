package com.example.legokp.models;

import com.google.gson.annotations.SerializedName;

/**
 * Модель отзыва для API
 */
public class Review {

    @SerializedName("review_id")
    private long reviewId;

    @SerializedName("set_num")
    private String setNum;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("username")
    private String username;

    @SerializedName("rating")
    private float rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("created_at")
    private long createdAt;

    @SerializedName("is_synced")
    private boolean isSynced;

    // Конструкторы
    public Review() {
    }

    public Review(String setNum, String userId, String username, float rating, String comment) {
        this.setNum = setNum;
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getReviewId() {
        return reviewId;
    }

    public void setReviewId(long reviewId) {
        this.reviewId = reviewId;
    }

    public String getSetNum() {
        return setNum;
    }

    public void setSetNum(String setNum) {
        this.setNum = setNum;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}