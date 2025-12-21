package com.example.legokp.viewmodels;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.legokp.database.entity.LegoSetEntity;
import com.example.legokp.models.LegoSet;
import com.example.legokp.repository.LegoRepository;
import com.example.legokp.ui.FilterBottomSheetFragment;
import com.example.legokp.utils.ModelMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class LegoViewModel extends AndroidViewModel {

    private final LegoRepository repository;
    private final LiveData<List<LegoSetEntity>> favoriteSets;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // --- ✨ РЕАКТИВНАЯ СИСТЕМА ФИЛЬТРАЦИИ (ПОЛНАЯ ВЕРСИЯ) --- //

    public static class FilterOptions {
        public String query = "";
        public String theme = null;
        public String sortBy = "name_asc";
        // ✨ ИСПРАВЛЕНО: Добавлены новые поля
        public int minYear = 1970;
        public int maxYear = 2024;
        public int minParts = 0;
        public int maxParts = 10000;

        FilterOptions copy() {
            FilterOptions newOptions = new FilterOptions();
            newOptions.query = this.query;
            newOptions.theme = this.theme;
            newOptions.sortBy = this.sortBy;
            newOptions.minYear = this.minYear;
            newOptions.maxYear = this.maxYear;
            newOptions.minParts = this.minParts;
            newOptions.maxParts = this.maxParts;
            return newOptions;
        }
    }

    private final MutableLiveData<FilterOptions> filters = new MutableLiveData<>(new FilterOptions());
    private final MediatorLiveData<List<LegoSetEntity>> filteredSets = new MediatorLiveData<>();

    public LegoViewModel(@NonNull Application application) {
        super(application);
        repository = new LegoRepository(application);
        favoriteSets = repository.getFavoriteSets();

        LiveData<List<LegoSetEntity>> allSetsFromDb = repository.getAllSets();

        filteredSets.addSource(allSetsFromDb, entities -> {
            if (entities != null && entities.isEmpty()) {
                fetchSetsFromApi(1, 200, null, null, null);
            }
            filteredSets.setValue(applyFilters(entities, filters.getValue()));
        });

        filteredSets.addSource(filters, filterOptions -> {
            filteredSets.setValue(applyFilters(allSetsFromDb.getValue(), filterOptions));
        });
    }

    private List<LegoSetEntity> applyFilters(List<LegoSetEntity> entities, FilterOptions options) {
        if (entities == null || options == null) return new ArrayList<>();
        
        List<LegoSetEntity> filteredList = new ArrayList<>(entities);

        // 1. Поиск по названию
        if (!TextUtils.isEmpty(options.query)) {
            filteredList.removeIf(set -> set.getName() == null || !set.getName().toLowerCase().contains(options.query.toLowerCase()));
        }

        // 2. Фильтр по теме
        if (!TextUtils.isEmpty(options.theme)) {
            filteredList.removeIf(set -> set.getTheme() == null || !set.getTheme().toLowerCase().contains(options.theme.toLowerCase()));
        }

        // 3. Фильтр по году
        filteredList.removeIf(set -> set.getYear() < options.minYear || set.getYear() > options.maxYear);
        
        // 4. Фильтр по количеству деталей
        filteredList.removeIf(set -> set.getNumParts() < options.minParts || set.getNumParts() > options.maxParts);

        // 5. Сортировка
        sortSets(filteredList, options.sortBy);

        return filteredList;
    }

    private void sortSets(List<LegoSetEntity> sets, String sortBy) {
        switch (sortBy) {
            case "name_desc": sets.sort((a, b) -> b.getName().compareToIgnoreCase(a.getName())); break;
            case "price_asc": sets.sort(Comparator.comparingDouble(LegoSetEntity::getPrice)); break;
            case "price_desc": sets.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice())); break;
            case "rating_desc": sets.sort((a, b) -> Double.compare(b.getRating(), a.getRating())); break;
            case "year_desc": sets.sort((a, b) -> Integer.compare(b.getYear(), a.getYear())); break;
            case "year_asc": sets.sort(Comparator.comparingInt(LegoSetEntity::getYear)); break;
            case "parts_desc": sets.sort((a, b) -> Integer.compare(b.getNumParts(), a.getNumParts())); break;
            case "parts_asc": sets.sort(Comparator.comparingInt(LegoSetEntity::getNumParts)); break;
            default: sets.sort(Comparator.comparing(LegoSetEntity::getName, String.CASE_INSENSITIVE_ORDER)); break;
        }
    }

    // --- ПУБЛИЧНЫЕ МЕТОДЫ ДЛЯ UI --- //

    public LiveData<List<LegoSetEntity>> getFilteredSets() { return filteredSets; }

    public void setSearchQuery(String query) {
        FilterOptions oldOptions = filters.getValue();
        if (oldOptions == null || oldOptions.query.equals(query)) return;
        FilterOptions newOptions = oldOptions.copy();
        newOptions.query = query;
        filters.setValue(newOptions);
    }

    // ✨ ИСПРАВЛЕНО: Этот метод теперь тоже фильтрует по теме
    public void setTheme(String theme) {
        FilterOptions oldOptions = filters.getValue();
        if (oldOptions == null || Objects.equals(oldOptions.theme, theme)) return;
        FilterOptions newOptions = oldOptions.copy();
        newOptions.theme = theme;
        filters.setValue(newOptions);
    }

    // ✨ ИСПРАВЛЕНО: Обновленный метод для приема новых фильтров
    public void setAdvancedFilters(String theme, int minYear, int maxYear, int minParts, int maxParts) {
        FilterOptions oldOptions = filters.getValue();
        if (oldOptions == null) return;
        FilterOptions newOptions = oldOptions.copy();
        newOptions.theme = theme;
        newOptions.minYear = minYear;
        newOptions.maxYear = maxYear;
        newOptions.minParts = minParts;
        newOptions.maxParts = maxParts;
        filters.setValue(newOptions);
    }

    public void setSortOrder(String sortBy) {
        FilterOptions oldOptions = filters.getValue();
        if (oldOptions == null || oldOptions.sortBy.equals(sortBy)) return;
        FilterOptions newOptions = oldOptions.copy();
        newOptions.sortBy = sortBy;
        filters.setValue(newOptions);
    }
    
    public void clearFilters() { filters.setValue(new FilterOptions()); }

    // --- Остальные методы --- //

    public interface FavoriteResultCallback { void onResult(boolean isFavorite, String error); }
    public interface AddResultCallback { void onResult(String error); }
    public interface DeleteResultCallback { void onResult(String error); }
    public interface CountCallback { void onCount(int count); }

    public void fetchSetsFromApi(int page, int pageSize, String theme, Integer year, String search) {
        isLoading.setValue(true);
        repository.fetchAndCacheSets(page, pageSize, theme, year, search, new LegoRepository.FetchCallback() {
            @Override public void onSuccess(List<LegoSet> sets) { isLoading.postValue(false); }
            @Override public void onError(String message) { 
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void toggleFavorite(String setNum, FavoriteResultCallback callback) {
        repository.toggleFavorite(setNum, new LegoRepository.FavoriteCallback() {
            @Override public void onSuccess(boolean isFavorite) { if (callback != null) callback.onResult(isFavorite, null); }
            @Override public void onError(String message) { if (callback != null) callback.onResult(false, message); }
        });
    }

    public void addLegoSet(LegoSet legoSet, AddResultCallback callback) {
        LegoSetEntity entity = ModelMapper.toEntity(legoSet);
        repository.insertLegoSet(entity, new LegoRepository.InsertCallback() {
            @Override public void onSuccess() { if (callback != null) callback.onResult(null); }
            @Override public void onError(String message) { if (callback != null) callback.onResult(message); }
        });
    }

    public void deleteLegoSet(LegoSet legoSet, DeleteResultCallback callback) {
        LegoSetEntity entity = ModelMapper.toEntity(legoSet);
        repository.deleteLegoSet(entity, new LegoRepository.DeleteCallback() {
            @Override public void onSuccess() { if (callback != null) callback.onResult(null); }
            @Override public void onError(String message) { if (callback != null) callback.onResult(message); }
        });
    }
    
    public void getFavoriteCount(CountCallback callback) { repository.getFavoriteCount(callback::onCount); }
    public void getTotalSetsCount(CountCallback callback) { repository.getTotalSetsCount(callback::onCount); }

    public LiveData<List<LegoSetEntity>> getFavoriteSets() { return favoriteSets; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
