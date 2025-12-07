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

    // Сохранить данные пользователя после логина
    public void saveUserSession(String token, String userId, String username, String email) {
        editor.putString(Constants.KEY_TOKEN, token);
        editor.putString(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_USERNAME, username);
        editor.putString(Constants.KEY_EMAIL, email);
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    // Проверить, залогинен ли пользователь
    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    // Получить токен
    public String getToken() {
        return prefs.getString(Constants.KEY_TOKEN, null);
    }

    // Получить имя пользователя
    public String getUsername() {
        return prefs.getString(Constants.KEY_USERNAME, "User");
    }

    // Получить email
    public String getEmail() {
        return prefs.getString(Constants.KEY_EMAIL, "");
    }

    // Очистить сессию (логаут)
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}