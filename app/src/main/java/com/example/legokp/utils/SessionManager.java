package com.example.legokp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ✅ UPDATED: Added imageUrl parameter
    public void saveUserSession(String token, String userId, String username, String email, String imageUrl) {
        editor.putString(Constants.KEY_TOKEN, token);
        editor.putString(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_USERNAME, username);
        editor.putString(Constants.KEY_EMAIL, email);
        editor.putString(Constants.KEY_USER_IMAGE, imageUrl);
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    // ✅ NEW: Overloaded method for backward compatibility
    public void saveUserSession(String token, String userId, String username, String email) {
        saveUserSession(token, userId, username, email, null);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    public String getToken() {
        return prefs.getString(Constants.KEY_TOKEN, null);
    }

    public String getUsername() {
        return prefs.getString(Constants.KEY_USERNAME, "User");
    }

    public String getEmail() {
        return prefs.getString(Constants.KEY_EMAIL, "");
    }

    public String getUserId() {
        return prefs.getString(Constants.KEY_USER_ID, "");
    }

    // ✅ NEW: Get image URL
    public String getUserImage() {
        return prefs.getString(Constants.KEY_USER_IMAGE, null);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}