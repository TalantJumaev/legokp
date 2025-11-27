package com.example.legokp.network;

import com.example.legokp.models.MinifigResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface LegoApiService {
    @GET("lego/minifigs/")
    Call<MinifigResponse> getMinifigs();  // No @Header parameter
}