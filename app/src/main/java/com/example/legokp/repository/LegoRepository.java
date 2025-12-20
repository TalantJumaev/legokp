package com.example.legokp.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.legokp.database.AppDatabase;
import com.example.legokp.database.dao.LegoSetDao;
import com.example.legokp.database.entity.LegoSetEntity;
import com.example.legokp.models.LegoSet;
import com.example.legokp.models.LegoSetResponse;
import com.example.legokp.network.RetrofitClient;
import com.example.legokp.utils.ModelMapper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LegoRepository {

    private static final String TAG = "LegoRepository";
    private LegoSetDao legoSetDao;
    private LiveData<List<LegoSetEntity>> allSets;
    private LiveData<List<LegoSetEntity>> favoriteSets;

    public LegoRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        legoSetDao = database.legoSetDao();
        allSets = legoSetDao.getAllSets();
        favoriteSets = legoSetDao.getFavoriteSets();
    }

    // ========== LOCAL DATABASE OPERATIONS ==========

    /**
     * Получить все наборы из локальной БД
     */
    public LiveData<List<LegoSetEntity>> getAllSets() {
        return allSets;
    }

    /**
     * Получить избранные наборы из локальной БД
     */
    public LiveData<List<LegoSetEntity>> getFavoriteSets() {
        return favoriteSets;
    }

    /**
     * Получить наборы по теме
     */
    public LiveData<List<LegoSetEntity>> getSetsByTheme(String theme) {
        return legoSetDao.getSetsByTheme(theme);
    }

    /**
     * Поиск наборов
     */
    public LiveData<List<LegoSetEntity>> searchSets(String query) {
        return legoSetDao.searchSets(query);
    }

    // ========== API OPERATIONS ==========

    /**
     * Загрузить наборы из API и сохранить в БД
     */
    public void fetchAndCacheSets(int page, int pageSize, String theme,
                                  Integer year, String search,
                                  FetchCallback callback) {
        RetrofitClient.getApiService().getLegoSets(page, pageSize, theme, year, search)
                .enqueue(new Callback<LegoSetResponse>() {
                    @Override
                    public void onResponse(Call<LegoSetResponse> call, Response<LegoSetResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<LegoSet> apiSets = response.body().getResults();

                            // Конвертируем и сохраняем в БД
                            AppDatabase.databaseWriteExecutor.execute(() -> {
                                try {
                                    List<LegoSetEntity> entities = ModelMapper.toEntityList(apiSets);

                                    // ✅ Сохраняем состояние избранного для существующих наборов
                                    for (LegoSetEntity entity : entities) {
                                        LegoSetEntity existing = legoSetDao.getSetByNum(entity.getSetNum());
                                        if (existing != null) {
                                            // Сохраняем текущее состояние избранного
                                            entity.setFavorite(existing.isFavorite());
                                        }
                                    }

                                    legoSetDao.insertAll(entities);
                                    Log.d(TAG, "Cached " + entities.size() + " sets to database");

                                    if (callback != null) {
                                        callback.onSuccess(apiSets);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error caching sets: " + e.getMessage());
                                    if (callback != null) {
                                        callback.onError("Database error: " + e.getMessage());
                                    }
                                }
                            });
                        } else {
                            if (callback != null) {
                                callback.onError("Failed to fetch: " + response.code());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LegoSetResponse> call, Throwable t) {
                        Log.e(TAG, "API call failed: " + t.getMessage());
                        if (callback != null) {
                            callback.onError(t.getMessage());
                        }
                    }
                });
    }

    // ========== FAVORITE OPERATIONS (LOCAL ONLY) ==========

    /**
     * ✅ Переключить состояние избранного (работает только с локальной БД)
     */
    public void toggleFavorite(String setNum, FavoriteCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Получаем текущий набор из БД
                LegoSetEntity entity = legoSetDao.getSetByNum(setNum);

                if (entity == null) {
                    Log.e(TAG, "Set not found in database: " + setNum);
                    if (callback != null) {
                        callback.onError("Set not found in database");
                    }
                    return;
                }

                // Переключаем состояние
                boolean newFavoriteState = !entity.isFavorite();

                // ✅ Обновляем в БД
                legoSetDao.updateFavoriteStatus(setNum, newFavoriteState);

                Log.d(TAG, "Updated favorite status for " + setNum + " to " + newFavoriteState);

                if (callback != null) {
                    callback.onSuccess(newFavoriteState);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error toggling favorite: " + e.getMessage());
                if (callback != null) {
                    callback.onError("Database error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Получить количество избранных
     */
    public void getFavoriteCount(CountCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int count = legoSetDao.getFavoriteCount();
                if (callback != null) {
                    callback.onCount(count);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting favorite count: " + e.getMessage());
            }
        });
    }

    /**
     * Получить общее количество наборов
     */
    public void getTotalSetsCount(CountCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int count = legoSetDao.getSetCount();
                if (callback != null) {
                    callback.onCount(count);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting total count: " + e.getMessage());
            }
        });
    }

    // ========== CALLBACKS ==========

    public interface FetchCallback {
        void onSuccess(List<LegoSet> sets);
        void onError(String message);
    }

    public interface FavoriteCallback {
        void onSuccess(boolean isFavorite);
        void onError(String message);
    }

    public interface CountCallback {
        void onCount(int count);
    }
}