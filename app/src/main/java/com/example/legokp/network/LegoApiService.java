package com.example.legokp.network;

import com.example.legokp.models.AuthRequest;
import com.example.legokp.models.AuthResponse;
import com.example.legokp.models.FavoriteRequest;
import com.example.legokp.models.FavoriteResponse;
import com.example.legokp.models.LegoSetResponse;
import com.example.legokp.models.MinifigResponse;
import com.example.legokp.models.ThemeResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LegoApiService {

    // ========== AUTH ENDPOINTS ==========
    @POST("api/register")
    Call<AuthResponse> register(@Body AuthRequest request);

    @POST("api/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    // ========== LEGO SETS ENDPOINTS ==========
    @GET("api/legosets")
    Call<LegoSetResponse> getLegoSets(
            @Query("page") int page,
            @Query("pageSize") int pageSize,
            @Query("theme") String theme,
            @Query("year") Integer year,
            @Query("search") String search
    );

    @GET("lego/themes")
    Call<ThemeResponse> getThemes();

    @POST("lego/favorites/toggle")
    Call<FavoriteResponse> toggleFavorite(@Body FavoriteRequest request);

    // ========== MINIFIGS ENDPOINTS ==========
    @GET("lego/minifigs/")
    Call<MinifigResponse> getMinifigs();
}