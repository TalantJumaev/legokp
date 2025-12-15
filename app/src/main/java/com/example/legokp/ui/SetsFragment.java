package com.example.legokp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.legokp.R;
import com.example.legokp.adapter.LegoSetAdapter;
import com.example.legokp.adapter.ThemeAdapter;
import com.example.legokp.database.entity.LegoSetEntity;
import com.example.legokp.models.LegoSet;
import com.example.legokp.models.Theme;
import com.example.legokp.models.ThemeResponse;
import com.example.legokp.network.RetrofitClient;
import com.example.legokp.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetsFragment extends Fragment {

    private RecyclerView rvSets, rvThemes;
    private LegoSetAdapter setAdapter;
    private ThemeAdapter themeAdapter;
    private ProgressBar progressBar;
    private SearchView searchView;
    private FloatingActionButton fabFilter;

    private String currentTheme = null;
    private String currentSearch = null;
    private FilterBottomSheetFragment.FilterOptions filterOptions;

    private List<LegoSet> allSets = new ArrayList<>();

    private static final String TAG = "SetsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sets, container, false);

        viewModel = new ViewModelProvider(this).get(LegoViewModel.class);

        initViews(view);
        setupRecyclerViews();
        setupObservers();
        loadThemes();
        loadSets();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSets();
    }

    private void initViews(View view) {
        rvSets = view.findViewById(R.id.rvSets);
        rvThemes = view.findViewById(R.id.rvThemes);
        progressBar = view.findViewById(R.id.progressBar);
        searchView = view.findViewById(R.id.searchView);
        fabFilter = view.findViewById(R.id.fabFilter);

        setupSearchView();
        setupFilterButton();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearch = query;
                applyFiltersAndSort();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    currentSearch = null;
                    applyFiltersAndSort();
                }
                return true;
            }
        });
    }

    private void setupFilterButton() {
        fabFilter.setOnClickListener(v -> {
            FilterBottomSheetFragment filterSheet = FilterBottomSheetFragment.newInstance(
                    options -> {
                        filterOptions = options;
                        applyFiltersAndSort();
                    }
            );

            // Передаем текущие фильтры
            if (filterOptions != null) {
                Bundle args = new Bundle();
                args.putString("sortBy", filterOptions.sortBy);
                args.putString("priceRange", filterOptions.priceRange);
                args.putString("ageRange", filterOptions.ageRange);
                args.putInt("minParts", filterOptions.minParts);
                args.putInt("maxParts", filterOptions.maxParts);
                filterSheet.setArguments(args);
            }

            filterSheet.show(getParentFragmentManager(), "FilterBottomSheet");
        });
    }

    private void setupRecyclerViews() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvSets.setLayoutManager(gridLayoutManager);

        setAdapter = new LegoSetAdapter(getContext(), (legoSet, position) -> {
            toggleFavorite(legoSet.getSetNum());
        });
        rvSets.setAdapter(setAdapter);

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvThemes.setLayoutManager(horizontalLayoutManager);

        themeAdapter = new ThemeAdapter(getContext(), (theme, position) -> {
            currentTheme = theme.getName();
            applyFiltersAndSort();
        });
        rvThemes.setAdapter(themeAdapter);
    }

    private void setupObservers() {
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            showLoading(isLoading);
        });

        // Observe errors
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe all sets from database
        viewModel.getAllSets().observe(getViewLifecycleOwner(), entities -> {
            if (entities != null) {
                List<LegoSet> sets = ModelMapper.toModelList(entities);
                setAdapter.updateSets(sets);
            }
        });
    }

    private void loadThemes() {
        RetrofitClient.getApiService().getThemes().enqueue(new Callback<ThemeResponse>() {
            @Override
            public void onResponse(Call<ThemeResponse> call, Response<ThemeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Theme> themes = response.body().getThemes();
                    if (themes != null) {
                        themeAdapter.updateThemes(themes);
                    }
                }
            }

            @Override
            public void onFailure(Call<ThemeResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load themes: " + t.getMessage());
            }
        });
    }

    private void loadSets() {
        showLoading(true);

        RetrofitClient.getApiService().getLegoSets(
                1,
                Constants.PAGE_SIZE,
                null, // Не фильтруем на сервере, будем фильтровать локально
                null,
                null
        ).enqueue(new Callback<LegoSetResponse>() {
            @Override
            public void onResponse(Call<LegoSetResponse> call, Response<LegoSetResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    allSets = response.body().getResults();
                    if (allSets != null) {
                        applyFiltersAndSort();
                        Log.d(TAG, "Loaded " + allSets.size() + " sets");
                    }
                } else {
                    Toast.makeText(getContext(),
                            "Failed to load sets: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LegoSetResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(),
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failure: " + t.getMessage());
            }
        });
    }

    private void applyFiltersAndSort() {
        List<LegoSet> filteredSets = new ArrayList<>(allSets);

        // Фильтр по теме
        if (currentTheme != null) {
            List<LegoSet> themeFiltered = new ArrayList<>();
            for (LegoSet set : filteredSets) {
                if (set.getTheme() != null && set.getTheme().equalsIgnoreCase(currentTheme)) {
                    themeFiltered.add(set);
                }
            }
            filteredSets = themeFiltered;
        }

        // Фильтр по поиску
        if (currentSearch != null && !currentSearch.isEmpty()) {
            List<LegoSet> searchFiltered = new ArrayList<>();
            for (LegoSet set : filteredSets) {
                if (set.getName() != null &&
                        set.getName().toLowerCase().contains(currentSearch.toLowerCase())) {
                    searchFiltered.add(set);
                }
            }
            filteredSets = searchFiltered;
        }

        // Применяем фильтры из FilterOptions
        if (filterOptions != null) {
            filteredSets = applyAdvancedFilters(filteredSets);
        }

        // Сортировка
        if (filterOptions != null && filterOptions.sortBy != null) {
            sortSets(filteredSets, filterOptions.sortBy);
        }

        setAdapter.updateSets(filteredSets);
    }

    private List<LegoSet> applyAdvancedFilters(List<LegoSet> sets) {
        List<LegoSet> result = new ArrayList<>();

        for (LegoSet set : sets) {
            boolean matches = true;

            // Фильтр по цене
            if (filterOptions.priceRange != null) {
                matches = matchesPriceRange(set.getPrice(), filterOptions.priceRange);
            }

            // Фильтр по возрасту
            if (matches && filterOptions.ageRange != null) {
                matches = matchesAgeRange(set.getAgeRange(), filterOptions.ageRange);
            }

            // Фильтр по количеству деталей
            if (matches) {
                matches = set.getNumParts() >= filterOptions.minParts &&
                        set.getNumParts() <= filterOptions.maxParts;
            }

            if (matches) {
                result.add(set);
            }
        }

        return result;
    }

    private boolean matchesPriceRange(double price, String range) {
        switch (range) {
            case "Under $50":
                return price < 50;
            case "$50 - $100":
                return price >= 50 && price <= 100;
            case "$100 - $200":
                return price >= 100 && price <= 200;
            case "Over $200":
                return price > 200;
            default:
                return true;
        }
    }

    private boolean matchesAgeRange(String ageRange, String filter) {
        if (ageRange == null) return true;

        // Простое сравнение, можно улучшить
        return ageRange.contains(filter);
    }

    private void sortSets(List<LegoSet> sets, String sortBy) {
        switch (sortBy) {
            case "name_asc":
                Collections.sort(sets, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                break;
            case "name_desc":
                Collections.sort(sets, (a, b) -> b.getName().compareToIgnoreCase(a.getName()));
                break;
            case "price_asc":
                Collections.sort(sets, Comparator.comparingDouble(LegoSet::getPrice));
                break;
            case "price_desc":
                Collections.sort(sets, (a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            case "rating_desc":
                Collections.sort(sets, (a, b) -> Double.compare(b.getRating(), a.getRating()));
                break;
            case "year_desc":
                Collections.sort(sets, (a, b) -> Integer.compare(b.getYear(), a.getYear()));
                break;
        }
    }

    private void toggleFavorite(LegoSet legoSet, int position) {
        boolean newFavoriteState = !legoSet.isFavorite();
        setAdapter.updateFavoriteStatus(position, newFavoriteState);

        FavoriteRequest request = new FavoriteRequest(legoSet.getSetNum());

        RetrofitClient.getApiService().toggleFavorite(request).enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FavoriteResponse favoriteResponse = response.body();

                    if (favoriteResponse.isSuccess()) {
                        boolean isFavorite = favoriteResponse.isFavorite();
                        setAdapter.updateFavoriteStatus(position, isFavorite);

                        String message = isFavorite ? "Added to favorites ❤️" : "Removed from favorites";
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

                        Log.d(TAG, "Favorite toggled: " + legoSet.getName() + " -> " + isFavorite);
                    } else {
                        setAdapter.updateFavoriteStatus(position, !newFavoriteState);
                        Toast.makeText(getContext(),
                                "Failed to update: " + favoriteResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    setAdapter.updateFavoriteStatus(position, !newFavoriteState);
                    Toast.makeText(getContext(),
                            "Failed to update favorite",
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error: " + response.code());
                }
            }
        });
    }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                setAdapter.updateFavoriteStatus(position, !newFavoriteState);
                Toast.makeText(getContext(),
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failure: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}