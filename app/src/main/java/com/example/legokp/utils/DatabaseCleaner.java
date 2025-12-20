package com.example.legokp.utils;

import android.content.Context;
import android.util.Log;

import com.example.legokp.database.AppDatabase;
import com.example.legokp.database.entity.LegoSetEntity;

import java.util.List;

public class DatabaseCleaner {

    private static final String TAG = "DatabaseCleaner";

    public static void cleanNonFavorites(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                db.getOpenHelper().getWritableDatabase()
                        .execSQL("DELETE FROM lego_sets WHERE is_favorite = 0");

                Log.d(TAG, "Cleaned non-favorite sets from DB");
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning DB: " + e.getMessage());
            }
        });
    }

    public static void clearAllData(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                db.legoSetDao().deleteAll();
                db.reviewDao().deleteAll();

                Log.d(TAG, "Cleared all data from DB");
            } catch (Exception e) {
                Log.e(TAG, "Error clearing DB: " + e.getMessage());
            }
        });
    }

    public static void printStats(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int totalSets = db.legoSetDao().getSetCount();
                int favoriteSets = db.legoSetDao().getFavoriteCount();

                Log.d(TAG, "========== DB Stats ==========");
                Log.d(TAG, "Total sets: " + totalSets);
                Log.d(TAG, "Favorite sets: " + favoriteSets);
                Log.d(TAG, "Non-favorite sets: " + (totalSets - favoriteSets));

                // ✅ НОВОЕ: Показываем все избранные
                List<LegoSetEntity> favorites = db.legoSetDao().getFavoriteSetsSync();
                Log.d(TAG, "--- Favorite Sets List ---");
                for (LegoSetEntity entity : favorites) {
                    Log.d(TAG, "  • " + entity.getName() + " (is_favorite=" + entity.isFavorite() + ")");
                }

                Log.d(TAG, "==============================");
            } catch (Exception e) {
                Log.e(TAG, "Error getting stats: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}