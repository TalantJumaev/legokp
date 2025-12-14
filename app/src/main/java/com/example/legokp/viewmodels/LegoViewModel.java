package com.example.legokp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.legokp.database.entity.LegoSetEntity;
import com.example.legokp.models.LegoSet;
import com.example.legokp.repository.LegoRepository;

import java.util.List;

public class LegoViewModel extends AndroidViewModel {

    private LegoRepository repository;
    private LiveData<List<LegoSetEntity>> allSets;
    private LiveData<List<LegoSetEntity>> favoriteSets;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LegoViewModel(@NonNull Application application) {
        super(application);
        repository = new LegoRepository(application);
        allSets = repository.getAllSets();
        favoriteSets = repository.getFavoriteSets();
    }

    // Get all sets
    public LiveData<List<LegoSetEntity>> getAllSets() {
        return allSets;
    }

    // Get favorite sets
    public LiveData<List<LegoSetEntity>> getFavoriteSets() {
        return favoriteSets;
    }

    // Get sets by theme
    public LiveData<List<LegoSetEntity>> getSetsByTheme(String theme) {
        return repository.getSetsByTheme(theme);
    }

    // Search sets
    public LiveData<List<LegoSetEntity>> searchSets(String query) {
        return repository.searchSets(query);
    }

    // Fetch from API
    public void fetchSetsFromApi(int page, int pageSize, String theme,
                                 Integer year, String search) {
        isLoading.setValue(true);
        repository.fetchAndCacheSets(page, pageSize, theme, year, search,
                new LegoRepository.FetchCallback() {
                    @Override
                    public void onSuccess(List<LegoSet> sets) {
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onError(String message) {
                        isLoading.postValue(false);
                        errorMessage.postValue(message);
                    }
                });
    }

    // Toggle favorite
    public void toggleFavorite(String setNum, FavoriteResultCallback callback) {
        repository.toggleFavorite(setNum, new LegoRepository.FavoriteCallback() {
            @Override
            public void onSuccess(boolean isFavorite) {
                if (callback != null) {
                    callback.onResult(isFavorite, null);
                }
            }

            @Override
            public void onError(String message) {
                if (callback != null) {
                    callback.onResult(false, message);
                }
            }
        });
    }

    // Get favorite count
    public void getFavoriteCount(CountCallback callback) {
        repository.getFavoriteCount(count -> {
            if (callback != null) {
                callback.onCount(count);
            }
        });
    }

    // Get total sets count
    public void getTotalSetsCount(CountCallback callback) {
        repository.getTotalSetsCount(count -> {
            if (callback != null) {
                callback.onCount(count);
            }
        });
    }

    // Observable states
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Callbacks
    public interface FavoriteResultCallback {
        void onResult(boolean isFavorite, String error);
    }

    public interface CountCallback {
        void onCount(int count);
    }
}