package com.example.legokp.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.os.HandlerCompat;
import androidx.lifecycle.LiveData;

import com.example.legokp.database.AppDatabase;
import com.example.legokp.database.dao.LegoSetDao;
import com.example.legokp.database.entity.LegoSetEntity;
import com.example.legokp.models.LegoSet;
import com.example.legokp.models.LegoSetResponse;
import com.example.legokp.network.RetrofitClient;
import com.example.legokp.utils.ModelMapper;

import java.util.List;
import java.util.concurrent.ExecutorService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LegoRepository {

    private static final String TAG = "LegoRepository";
    private final LegoSetDao legoSetDao;
    private final LiveData<List<LegoSetEntity>> allSets;
    private final LiveData<List<LegoSetEntity>> favoriteSets;
    private final ExecutorService databaseWriteExecutor;
    private final Handler mainThreadHandler;

    public LegoRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        legoSetDao = database.legoSetDao();
        allSets = legoSetDao.getAllSets();
        favoriteSets = legoSetDao.getFavoriteSets();
        databaseWriteExecutor = AppDatabase.databaseWriteExecutor;
        mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    }

    public LiveData<List<LegoSetEntity>> getAllSets() {
        return allSets;
    }

    public LiveData<List<LegoSetEntity>> getFavoriteSets() {
        return favoriteSets;
    }

    public LiveData<List<LegoSetEntity>> getSetsByTheme(String theme) {
        return legoSetDao.getSetsByTheme(theme);
    }

    public LiveData<List<LegoSetEntity>> searchSets(String query) {
        return legoSetDao.searchSets(query);
    }

    public void insertLegoSet(LegoSetEntity legoSet, InsertCallback callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                legoSetDao.insert(legoSet);
                if (callback != null) {
                    mainThreadHandler.post(callback::onSuccess);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting set", e);
                if (callback != null) {
                    mainThreadHandler.post(() -> callback.onError(e.getMessage()));
                }
            }
        });
    }

    public void deleteLegoSet(LegoSetEntity legoSet, DeleteCallback callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                legoSetDao.delete(legoSet);
                if (callback != null) {
                    mainThreadHandler.post(callback::onSuccess);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting set", e);
                if (callback != null) {
                    mainThreadHandler.post(() -> callback.onError(e.getMessage()));
                }
            }
        });
    }

    public void toggleFavorite(String setNum, FavoriteCallback callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                LegoSetEntity entity = legoSetDao.getSetByNum(setNum);
                if (entity != null) {
                    boolean newState = !entity.isFavorite();
                    legoSetDao.updateFavoriteStatus(setNum, newState);
                    if (callback != null) {
                        mainThreadHandler.post(() -> callback.onSuccess(newState));
                    }
                } else {
                    if (callback != null) {
                        mainThreadHandler.post(() -> callback.onError("Set not found"));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error toggling favorite", e);
                if (callback != null) {
                    mainThreadHandler.post(() -> callback.onError(e.getMessage()));
                }
            }
        });
    }

    public void getFavoriteCount(CountCallback callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                int count = legoSetDao.getFavoriteCount();
                if (callback != null) {
                    mainThreadHandler.post(() -> callback.onCount(count));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting favorite count", e);
            }
        });
    }

    public void getTotalSetsCount(CountCallback callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                int count = legoSetDao.getSetCount();
                if (callback != null) {
                    mainThreadHandler.post(() -> callback.onCount(count));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting total sets count", e);
            }
        });
    }

    public void fetchAndCacheSets(int page, int pageSize, String theme,
                                  Integer year, String search,
                                  FetchCallback callback) {
        RetrofitClient.getApiService().getLegoSets(page, pageSize, theme, year, search)
                .enqueue(new Callback<LegoSetResponse>() {
                    @Override
                    public void onResponse(Call<LegoSetResponse> call, Response<LegoSetResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            databaseWriteExecutor.execute(() -> {
                                List<LegoSet> apiSets = response.body().getResults();
                                List<LegoSetEntity> entities = ModelMapper.toEntityList(apiSets);
                                for (LegoSetEntity entity : entities) {
                                    LegoSetEntity existing = legoSetDao.getSetByNum(entity.getSetNum());
                                    if (existing != null) {
                                        entity.setFavorite(existing.isFavorite());
                                    }
                                }
                                legoSetDao.insertAll(entities);
                                if (callback != null) {
                                    mainThreadHandler.post(() -> callback.onSuccess(apiSets));
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
                        if (callback != null) {
                            callback.onError(t.getMessage());
                        }
                    }
                });
    }

    public interface FetchCallback {
        void onSuccess(List<LegoSet> sets);
        void onError(String message);
    }

    public interface FavoriteCallback {
        void onSuccess(boolean isFavorite);
        void onError(String message);
    }

    public interface InsertCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface CountCallback {
        void onCount(int count);
    }
}
