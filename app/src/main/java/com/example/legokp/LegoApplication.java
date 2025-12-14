package com.example.legokp;

import android.app.Application;

import com.example.legokp.network.RetrofitClient;

public class LegoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize RetrofitClient with application context
        RetrofitClient.init(this);
    }
}