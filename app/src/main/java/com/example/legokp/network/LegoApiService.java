package com.example.legokp.network;

import com.example.legokp.models.AuthRequest;
import com.example.legokp.models.AuthResponse;
import com.example.legokp.models.FavoriteRequest;
import com.example.legokp.models.FavoriteResponse;
import com.example.legokp.models.LegoSetResponse;
import com.example.legokp.models.MinifigResponse;
import com.example.legokp.models.Review;
import com.example.legokp.models.ReviewResponse;
import com.example.legokp.models.ThemeResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * API сервис для всех запросов к серверу
 * Включает endpoints для наборов, авторизации, избранного, минифигурок и отзывов
 */
public interface LegoApiService {

    // ========================================
    // AUTH ENDPOINTS - Авторизация
    // ========================================

    /**
     * Регистрация нового пользователя
     * @param request Данные для регистрации (username, email, password)
     * @return AuthResponse с токеном и данными пользователя
     */
    @POST("api/register")
    Call<AuthResponse> register(@Body AuthRequest request);

    /**
     * Вход пользователя
     * @param request Данные для входа (email, password)
     * @return AuthResponse с токеном и данными пользователя
     */
    @POST("api/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    // ========================================
    // LEGO SETS ENDPOINTS - Наборы LEGO
    // ========================================

    /**
     * Получить список наборов LEGO с фильтрацией и пагинацией
     * @param page Номер страницы
     * @param pageSize Количество элементов на странице
     * @param theme Фильтр по теме (необязательно)
     * @param year Фильтр по году (необязательно)
     * @param search Поисковый запрос (необязательно)
     * @return LegoSetResponse со списком наборов
     */
    @GET("api/legosets")
    Call<LegoSetResponse> getLegoSets(
            @Query("page") int page,
            @Query("pageSize") int pageSize,
            @Query("theme") String theme,
            @Query("year") Integer year,
            @Query("search") String search
    );

    /**
     * Получить список всех тем/категорий LEGO
     * @return ThemeResponse со списком тем
     */
    @GET("lego/themes")
    Call<ThemeResponse> getThemes();

    // ========================================
    // FAVORITES ENDPOINTS - Избранное
    // ========================================

    /**
     * Добавить/удалить набор из избранного
     * @param request FavoriteRequest с номером набора
     * @return FavoriteResponse с новым статусом избранного
     */
    @POST("lego/favorites/toggle")
    Call<FavoriteResponse> toggleFavorite(@Body FavoriteRequest request);

    // ========================================
    // MINIFIGS ENDPOINTS - Минифигурки
    // ========================================

    /**
     * Получить список минифигурок
     * @return MinifigResponse со списком минифигурок
     */
    @GET("lego/minifigs/")
    Call<MinifigResponse> getMinifigs();

    // ========================================
    // REVIEWS ENDPOINTS - Отзывы ✨ НОВОЕ
    // ========================================

    /**
     * Получить все отзывы для конкретного набора
     * @param setNum Номер набора (например: "21333-1")
     * @return ReviewResponse со списком отзывов и средним рейтингом
     */
    @GET("api/reviews/{setNum}")
    Call<ReviewResponse> getReviews(@Path("setNum") String setNum);

    /**
     * Добавить новый отзыв
     * @param review Объект отзыва (set_num, user_id, username, rating, comment)
     * @return ReviewResponse с подтверждением
     */
    @POST("api/reviews")
    Call<ReviewResponse> submitReview(@Body Review review);

    /**
     * Обновить существующий отзыв
     * @param reviewId ID отзыва
     * @param review Обновленные данные отзыва
     * @return ReviewResponse с подтверждением
     */
    @PUT("api/reviews/{reviewId}")
    Call<ReviewResponse> updateReview(
            @Path("reviewId") long reviewId,
            @Body Review review
    );

    /**
     * Удалить отзыв
     * @param reviewId ID отзыва для удаления
     * @return ReviewResponse с подтверждением удаления
     */
    @DELETE("api/reviews/{reviewId}")
    Call<ReviewResponse> deleteReview(@Path("reviewId") long reviewId);

    /**
     * Получить средний рейтинг для набора
     * @param setNum Номер набора
     * @return ReviewResponse с средним рейтингом и количеством отзывов
     */
    @GET("api/reviews/{setNum}/rating")
    Call<ReviewResponse> getAverageRating(@Path("setNum") String setNum);

    /**
     * Получить отзывы пользователя
     * @param userId ID пользователя
     * @return ReviewResponse со списком отзывов пользователя
     */
    @GET("api/reviews/user/{userId}")
    Call<ReviewResponse> getUserReviews(@Path("userId") String userId);
}