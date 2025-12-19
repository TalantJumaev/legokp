package com.example.legokp.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.legokp.database.AppDatabase;
import com.example.legokp.database.dao.ReviewDao;
import com.example.legokp.database.entity.ReviewEntity;
import com.example.legokp.models.Review;
import com.example.legokp.models.ReviewResponse;
import com.example.legokp.network.RetrofitClient;
import com.example.legokp.utils.ModelMapper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Репозиторий для работы с отзывами
 * Управляет данными из локальной БД и API
 */
public class ReviewRepository {

    private static final String TAG = "ReviewRepository";
    private ReviewDao reviewDao;

    public ReviewRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        reviewDao = database.reviewDao();
    }

    // ========== LOCAL DATABASE OPERATIONS ==========

    /**
     * Получить отзывы для набора
     */
    public LiveData<List<ReviewEntity>> getReviewsForSet(String setNum) {
        return reviewDao.getReviewsForSet(setNum);
    }

    /**
     * Получить средний рейтинг
     */
    public LiveData<Float> getAverageRating(String setNum) {
        return reviewDao.getAverageRating(setNum);
    }

    /**
     * Получить количество отзывов
     */
    public LiveData<Integer> getReviewCount(String setNum) {
        return reviewDao.getReviewCount(setNum);
    }

    /**
     * Получить топ отзывы
     */
    public LiveData<List<ReviewEntity>> getTopReviews(String setNum) {
        return reviewDao.getTopReviews(setNum);
    }

    /**
     * Добавить отзыв локально
     */
    public void addReviewLocal(ReviewEntity review, AddReviewCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long reviewId = reviewDao.insert(review);
                review.setReviewId(reviewId);

                if (callback != null) {
                    callback.onSuccess(reviewId);
                }

                Log.d(TAG, "Review added locally with ID: " + reviewId);
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("Failed to add review: " + e.getMessage());
                }
                Log.e(TAG, "Error adding review: " + e.getMessage());
            }
        });
    }

    /**
     * Проверить, оставлял ли пользователь отзыв
     */
    public void hasUserReviewed(String setNum, String userId, CheckReviewCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = reviewDao.hasUserReviewed(setNum, userId);
            if (callback != null) {
                callback.onResult(count > 0);
            }
        });
    }

    /**
     * Получить отзыв пользователя для набора
     */
    public void getUserReviewForSet(String setNum, String userId, GetReviewCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ReviewEntity review = reviewDao.getUserReviewForSet(setNum, userId);
            if (callback != null) {
                callback.onResult(review);
            }
        });
    }

    /**
     * Обновить отзыв
     */
    public void updateReview(ReviewEntity review, UpdateReviewCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                review.setSynced(false); // Помечаем как несинхронизированный
                reviewDao.update(review);

                if (callback != null) {
                    callback.onSuccess();
                }

                Log.d(TAG, "Review updated: " + review.getReviewId());
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("Failed to update review: " + e.getMessage());
                }
                Log.e(TAG, "Error updating review: " + e.getMessage());
            }
        });
    }

    /**
     * Удалить отзыв
     */
    public void deleteReview(ReviewEntity review, DeleteReviewCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                reviewDao.delete(review);

                if (callback != null) {
                    callback.onSuccess();
                }

                Log.d(TAG, "Review deleted: " + review.getReviewId());
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("Failed to delete review: " + e.getMessage());
                }
                Log.e(TAG, "Error deleting review: " + e.getMessage());
            }
        });
    }

    // ========== API OPERATIONS ==========

    /**
     * Загрузить отзывы с сервера
     */
    public void fetchReviewsFromApi(String setNum, FetchReviewsCallback callback) {
        RetrofitClient.getApiService().getReviews(setNum)
                .enqueue(new Callback<ReviewResponse>() {
                    @Override
                    public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ReviewResponse reviewResponse = response.body();

                            if (reviewResponse.isSuccess()) {
                                List<Review> apiReviews = reviewResponse.getReviews();

                                // Сохранить в БД
                                AppDatabase.databaseWriteExecutor.execute(() -> {
                                    List<ReviewEntity> entities = ModelMapper.reviewListToEntityList(apiReviews);
                                    reviewDao.insertAll(entities);

                                    // Пометить как синхронизированные
                                    for (ReviewEntity entity : entities) {
                                        entity.setSynced(true);
                                        reviewDao.update(entity);
                                    }

                                    Log.d(TAG, "Cached " + entities.size() + " reviews");
                                });

                                if (callback != null) {
                                    callback.onSuccess(apiReviews, reviewResponse.getAverageRating());
                                }
                            } else {
                                if (callback != null) {
                                    callback.onError(reviewResponse.getMessage());
                                }
                            }
                        } else {
                            if (callback != null) {
                                callback.onError("Failed to fetch reviews: " + response.code());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ReviewResponse> call, Throwable t) {
                        if (callback != null) {
                            callback.onError("Network error: " + t.getMessage());
                        }
                        Log.e(TAG, "API call failed: " + t.getMessage());
                    }
                });
    }

    /**
     * Отправить отзыв на сервер
     */
    public void submitReviewToApi(Review review, SubmitReviewCallback callback) {
        RetrofitClient.getApiService().submitReview(review)
                .enqueue(new Callback<ReviewResponse>() {
                    @Override
                    public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ReviewResponse reviewResponse = response.body();

                            if (reviewResponse.isSuccess()) {
                                // Обновить локальную БД
                                AppDatabase.databaseWriteExecutor.execute(() -> {
                                    ReviewEntity entity = ModelMapper.reviewToEntity(review);
                                    entity.setSynced(true);
                                    reviewDao.update(entity);
                                });

                                if (callback != null) {
                                    callback.onSuccess();
                                }

                                Log.d(TAG, "Review submitted successfully");
                            } else {
                                if (callback != null) {
                                    callback.onError(reviewResponse.getMessage());
                                }
                            }
                        } else {
                            if (callback != null) {
                                callback.onError("Failed to submit review: " + response.code());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ReviewResponse> call, Throwable t) {
                        if (callback != null) {
                            callback.onError("Network error: " + t.getMessage());
                        }
                        Log.e(TAG, "API call failed: " + t.getMessage());
                    }
                });
    }

    /**
     * Синхронизировать несинхронизированные отзывы
     */
    public void syncReviews(SyncCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<ReviewEntity> unsyncedReviews = reviewDao.getUnsyncedReviews();

            if (unsyncedReviews.isEmpty()) {
                if (callback != null) {
                    callback.onComplete(0);
                }
                return;
            }

            // Отправить каждый отзыв на сервер
            for (ReviewEntity entity : unsyncedReviews) {
                Review review = ModelMapper.entityToReview(entity);
                submitReviewToApi(review, new SubmitReviewCallback() {
                    @Override
                    public void onSuccess() {
                        reviewDao.markAsSynced(entity.getReviewId());
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Failed to sync review: " + message);
                    }
                });
            }

            if (callback != null) {
                callback.onComplete(unsyncedReviews.size());
            }
        });
    }

    // ========== CALLBACKS ==========

    public interface AddReviewCallback {
        void onSuccess(long reviewId);
        void onError(String message);
    }

    public interface FetchReviewsCallback {
        void onSuccess(List<Review> reviews, float averageRating);
        void onError(String message);
    }

    public interface SubmitReviewCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface CheckReviewCallback {
        void onResult(boolean hasReviewed);
    }

    public interface GetReviewCallback {
        void onResult(ReviewEntity review);
    }

    public interface UpdateReviewCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface DeleteReviewCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface SyncCallback {
        void onComplete(int syncedCount);
    }
}