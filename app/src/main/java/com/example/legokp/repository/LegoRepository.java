package com.example.legokp.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.legokp.database.AppDatabase;
import com.example.legokp.database.dao.LegoSetDao;
import com.example.legokp.database.entity.LegoSetEntity;
import com.example.legokp.models.FavoriteRequest;
import com.example.legokp.models.FavoriteResponse;
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

    // Get all sets from local database
    public LiveData<List<LegoSetEntity>> getAllSets() {
        return allSets;
    }

    // Get favorite sets from local database
    public LiveData<List<LegoSetEntity>> getFavoriteSets() {
        return favoriteSets;
    }

    // Get sets by theme
    public LiveData<List<LegoSetEntity>> getSetsByTheme(String theme) {
        return legoSetDao.getSetsByTheme(theme);
    }

    // Search sets
    public LiveData<List<LegoSetEntity>> searchSets(String query) {
        return legoSetDao.searchSets(query);
    }

    // Fetch sets from API and cache to database
    public void fetchAndCacheSets(int page, int pageSize, String theme,
                                  Integer year, String search,
                                  FetchCallback callback) {
        RetrofitClient.getApiService().getLegoSets(page, pageSize, theme, year, search)
                .enqueue(new Callback<LegoSetResponse>() {
                    @Override
                    public void onResponse(Call<LegoSetResponse> call, Response<LegoSetResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<LegoSet> apiSets = response.body().getResults();

                            // Convert and cache to database
                            AppDatabase.databaseWriteExecutor.execute(() -> {
                                List<LegoSetEntity> entities = ModelMapper.toEntityList(apiSets);
                                legoSetDao.insertAll(entities);
                                Log.d(TAG, "Cached " + entities.size() + " sets to database");
                            });

                            if (callback != null) {
                                callback.onSuccess(apiSets);
                            }
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

    // Toggle favorite status
    public void toggleFavorite(String setNum, FavoriteCallback callback) {
        FavoriteRequest request = new FavoriteRequest(setNum);

        RetrofitClient.getApiService().toggleFavorite(request)
                .enqueue(new Callback<FavoriteResponse>() {
                    @Override
                    public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            boolean isFavorite = response.body().isFavorite();

                            // Update local database
                            AppDatabase.databaseWriteExecutor.execute(() -> {
                                legoSetDao.updateFavoriteStatus(setNum, isFavorite);
                                Log.d(TAG, "Updated favorite status for " + setNum);
                            });

                            if (callback != null) {
                                callback.onSuccess(isFavorite);
                            }
                        } else {
                            if (callback != null) {
                                callback.onError("Failed to update favorite");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                        if (callback != null) {
                            callback.onError(t.getMessage());
                        }
                    }
                });
    }

    // Get favorite count
    public void getFavoriteCount(CountCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = legoSetDao.getFavoriteCount();
            if (callback != null) {
                callback.onCount(count);
            }
        });
    }

    // Get total sets count
    public void getTotalSetsCount(CountCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = legoSetDao.getSetCount();
            if (callback != null) {
                callback.onCount(count);
            }
        });
    }

    // Callbacks
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