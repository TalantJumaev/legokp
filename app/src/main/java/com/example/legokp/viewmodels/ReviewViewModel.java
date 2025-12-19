package com.example.legokp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.legokp.database.entity.ReviewEntity;
import com.example.legokp.models.Review;
import com.example.legokp.repository.ReviewRepository;

import java.util.List;

/**
 * ViewModel для управления отзывами
 */
public class ReviewViewModel extends AndroidViewModel {

    private ReviewRepository repository;

    // LiveData для UI
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<String> successMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> operationComplete = new MutableLiveData<>();

    public ReviewViewModel(@NonNull Application application) {
        super(application);
        repository = new ReviewRepository(application);
    }

    // ========== GET DATA ==========

    /**
     * Получить отзывы для набора
     */
    public LiveData<List<ReviewEntity>> getReviewsForSet(String setNum) {
        return repository.getReviewsForSet(setNum);
    }

    /**
     * Получить средний рейтинг
     */
    public LiveData<Float> getAverageRating(String setNum) {
        return repository.getAverageRating(setNum);
    }

    /**
     * Получить количество отзывов
     */
    public LiveData<Integer> getReviewCount(String setNum) {
        return repository.getReviewCount(setNum);
    }

    /**
     * Получить топ отзывы
     */
    public LiveData<List<ReviewEntity>> getTopReviews(String setNum) {
        return repository.getTopReviews(setNum);
    }

    // ========== OPERATIONS ==========

    /**
     * Добавить отзыв
     */
    public void addReview(String setNum, String userId, String username,
                          float rating, String comment) {
        isLoading.setValue(true);

        ReviewEntity review = new ReviewEntity(setNum, userId, username, rating, comment);

        repository.addReviewLocal(review, new ReviewRepository.AddReviewCallback() {
            @Override
            public void onSuccess(long reviewId) {
                isLoading.postValue(false);
                successMessage.postValue("Review added successfully!");
                operationComplete.postValue(true);

                // Отправить на сервер
                Review apiReview = new Review(setNum, userId, username, rating, comment);
                repository.submitReviewToApi(apiReview, new ReviewRepository.SubmitReviewCallback() {
                    @Override
                    public void onSuccess() {
                        // Синхронизировано
                    }

                    @Override
                    public void onError(String message) {
                        // Будет синхронизировано позже
                    }
                });
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    /**
     * Обновить отзыв
     */
    public void updateReview(ReviewEntity review) {
        isLoading.setValue(true);

        repository.updateReview(review, new ReviewRepository.UpdateReviewCallback() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                successMessage.postValue("Review updated successfully!");
                operationComplete.postValue(true);
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    /**
     * Удалить отзыв
     */
    public void deleteReview(ReviewEntity review) {
        isLoading.setValue(true);

        repository.deleteReview(review, new ReviewRepository.DeleteReviewCallback() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                successMessage.postValue("Review deleted successfully!");
                operationComplete.postValue(true);
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    /**
     * Проверить, оставлял ли пользователь отзыв
     */
    public void checkUserReview(String setNum, String userId, UserReviewCallback callback) {
        repository.hasUserReviewed(setNum, userId, hasReviewed -> {
            if (callback != null) {
                callback.onResult(hasReviewed);
            }
        });
    }

    /**
     * Получить отзыв пользователя
     */
    public void getUserReview(String setNum, String userId, GetUserReviewCallback callback) {
        repository.getUserReviewForSet(setNum, userId, review -> {
            if (callback != null) {
                callback.onResult(review);
            }
        });
    }

    /**
     * Загрузить отзывы с сервера
     */
    public void fetchReviewsFromApi(String setNum) {
        isLoading.setValue(true);

        repository.fetchReviewsFromApi(setNum, new ReviewRepository.FetchReviewsCallback() {
            @Override
            public void onSuccess(List<Review> reviews, float averageRating) {
                isLoading.postValue(false);
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    /**
     * Синхронизировать отзывы
     */
    public void syncReviews() {
        repository.syncReviews(syncedCount -> {
            if (syncedCount > 0) {
                successMessage.postValue(syncedCount + " reviews synced");
            }
        });
    }

    // ========== OBSERVABLE STATES ==========

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<Boolean> getOperationComplete() {
        return operationComplete;
    }

    public void clearMessages() {
        errorMessage.setValue(null);
        successMessage.setValue(null);
        operationComplete.setValue(false);
    }

    // ========== CALLBACKS ==========

    public interface UserReviewCallback {
        void onResult(boolean hasReviewed);
    }

    public interface GetUserReviewCallback {
        void onResult(ReviewEntity review);
    }
}