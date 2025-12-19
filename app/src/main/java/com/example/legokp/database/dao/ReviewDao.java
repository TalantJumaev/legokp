package com.example.legokp.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.legokp.database.entity.ReviewEntity;

import java.util.List;

/**
 * DAO для работы с отзывами
 */
@Dao
public interface ReviewDao {

    /**
     * Добавить отзыв
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ReviewEntity review);

    /**
     * Добавить несколько отзывов
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ReviewEntity> reviews);

    /**
     * Обновить отзыв
     */
    @Update
    void update(ReviewEntity review);

    /**
     * Удалить отзыв
     */
    @Delete
    void delete(ReviewEntity review);

    /**
     * Удалить все отзывы
     */
    @Query("DELETE FROM reviews")
    void deleteAll();

    /**
     * Получить все отзывы для конкретного набора (с LiveData)
     */
    @Query("SELECT * FROM reviews WHERE set_num = :setNum ORDER BY created_at DESC")
    LiveData<List<ReviewEntity>> getReviewsForSet(String setNum);

    /**
     * Получить все отзывы для конкретного набора (без LiveData, для синхронизации)
     */
    @Query("SELECT * FROM reviews WHERE set_num = :setNum ORDER BY created_at DESC")
    List<ReviewEntity> getReviewsForSetSync(String setNum);

    /**
     * Получить все отзывы пользователя
     */
    @Query("SELECT * FROM reviews WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<ReviewEntity>> getReviewsByUser(String userId);

    /**
     * Получить отзыв по ID
     */
    @Query("SELECT * FROM reviews WHERE review_id = :reviewId LIMIT 1")
    ReviewEntity getReviewById(long reviewId);

    /**
     * Получить средний рейтинг для набора
     */
    @Query("SELECT AVG(rating) FROM reviews WHERE set_num = :setNum")
    LiveData<Float> getAverageRating(String setNum);

    /**
     * Получить количество отзывов для набора
     */
    @Query("SELECT COUNT(*) FROM reviews WHERE set_num = :setNum")
    LiveData<Integer> getReviewCount(String setNum);

    /**
     * Проверить, оставил ли пользователь отзыв на набор
     */
    @Query("SELECT COUNT(*) FROM reviews WHERE set_num = :setNum AND user_id = :userId")
    int hasUserReviewed(String setNum, String userId);

    /**
     * Получить отзыв пользователя для конкретного набора
     */
    @Query("SELECT * FROM reviews WHERE set_num = :setNum AND user_id = :userId LIMIT 1")
    ReviewEntity getUserReviewForSet(String setNum, String userId);

    /**
     * Получить несинхронизированные отзывы
     */
    @Query("SELECT * FROM reviews WHERE is_synced = 0")
    List<ReviewEntity> getUnsyncedReviews();

    /**
     * Пометить отзыв как синхронизированный
     */
    @Query("UPDATE reviews SET is_synced = 1 WHERE review_id = :reviewId")
    void markAsSynced(long reviewId);

    /**
     * Удалить отзывы для конкретного набора
     */
    @Query("DELETE FROM reviews WHERE set_num = :setNum")
    void deleteReviewsForSet(String setNum);

    /**
     * Получить топ 5 лучших отзывов (по рейтингу)
     */
    @Query("SELECT * FROM reviews WHERE set_num = :setNum ORDER BY rating DESC LIMIT 5")
    LiveData<List<ReviewEntity>> getTopReviews(String setNum);
}