package com.example.legokp.utils;

import android.content.Context;
import android.util.Log;

import com.example.legokp.database.AppDatabase;
import com.example.legokp.database.entity.LegoSetEntity;
import com.example.legokp.models.LegoSet;

public class FavoriteHelper {

    private static final String TAG = "FavoriteHelper";

    public static void addToFavorites(Context context, LegoSet set) {
        AppDatabase db = AppDatabase.getDatabase(context);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                LegoSetEntity existing = db.legoSetDao().getSetByNum(set.getSetNum());

                if (existing != null) {
                    db.legoSetDao().updateFavoriteStatus(set.getSetNum(), true);
                } else {
                    LegoSetEntity entity = ModelMapper.toEntity(set);
                    entity.setFavorite(true);
                    db.legoSetDao().insert(entity);
                }

                Log.d(TAG, "Added to favorites: " + set.getName());
            } catch (Exception e) {
                Log.e(TAG, "Error adding to favorites: " + e.getMessage());
            }
        });
    }

    public static void removeFromFavorites(Context context, String setNum) {
        AppDatabase db = AppDatabase.getDatabase(context);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                db.legoSetDao().updateFavoriteStatus(setNum, false);
                Log.d(TAG, "Removed from favorites: " + setNum);
            } catch (Exception e) {
                Log.e(TAG, "Error removing from favorites: " + e.getMessage());
            }
        });
    }

    public static boolean isFavorite(Context context, String setNum) {
        AppDatabase db = AppDatabase.getDatabase(context);

        try {
            LegoSetEntity entity = db.legoSetDao().getSetByNum(setNum);
            return entity != null && entity.isFavorite();
        } catch (Exception e) {
            Log.e(TAG, "Error checking favorite status: " + e.getMessage());
            return false;
        }
    }

    public static void toggleFavorite(Context context, LegoSet set, FavoriteCallback callback) {
        AppDatabase db = AppDatabase.getDatabase(context);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                LegoSetEntity existing = db.legoSetDao().getSetByNum(set.getSetNum());

                boolean newState;

                if (existing != null) {
                    newState = !existing.isFavorite();
                    db.legoSetDao().updateFavoriteStatus(set.getSetNum(), newState);

                    Log.d(TAG, "Updated existing set: " + set.getName());
                    Log.d(TAG, "  Previous state: " + existing.isFavorite());
                    Log.d(TAG, "  New state: " + newState);
                } else {
                    LegoSetEntity entity = ModelMapper.toEntity(set);
                    entity.setFavorite(true);
                    db.legoSetDao().insert(entity);
                    newState = true;

                    Log.d(TAG, "Inserted new favorite set: " + set.getName());
                }

                // ✅ Проверяем что действительно сохранилось
                LegoSetEntity verify = db.legoSetDao().getSetByNum(set.getSetNum());
                if (verify != null) {
                    Log.d(TAG, "Verification: is_favorite = " + verify.isFavorite());
                } else {
                    Log.e(TAG, "Verification FAILED: set not found after save!");
                }

                if (callback != null) {
                    callback.onSuccess(newState);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error toggling favorite: " + e.getMessage());
                e.printStackTrace();

                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public interface FavoriteCallback {
        void onSuccess(boolean isFavorite);
        void onError(String error);
    }
}