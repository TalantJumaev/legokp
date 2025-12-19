package com.example.legokp.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Сущность отзыва для Room Database
 * Связь с LegoSetEntity через set_num
 */
@Entity(
        tableName = "reviews",
        foreignKeys = @ForeignKey(
                entity = LegoSetEntity.class,
                parentColumns = "set_num",
                childColumns = "set_num",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("set_num")}
)
public class ReviewEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "review_id")
    private long reviewId;

    @NonNull
    @ColumnInfo(name = "set_num")
    private String setNum;

    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @NonNull
    @ColumnInfo(name = "username")
    private String username;

    @NonNull
    @ColumnInfo(name = "rating")
    private float rating; // От 1.0 до 5.0

    @ColumnInfo(name = "comment")
    private String comment;

    @NonNull
    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "is_synced")
    private boolean isSynced; // Синхронизирован с сервером

    // Конструктор
    public ReviewEntity(@NonNull String setNum, @NonNull String userId,
                        @NonNull String username, float rating, String comment) {
        this.setNum = setNum;
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = System.currentTimeMillis();
        this.isSynced = false;
    }

    // Getters and Setters
    public long getReviewId() {
        return reviewId;
    }

    public void setReviewId(long reviewId) {
        this.reviewId = reviewId;
    }

    @NonNull
    public String getSetNum() {
        return setNum;
    }

    public void setSetNum(@NonNull String setNum) {
        this.setNum = setNum;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
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