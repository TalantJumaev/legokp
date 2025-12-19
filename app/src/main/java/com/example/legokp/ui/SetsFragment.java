package com.example.legokp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.example.legokp.models.LegoSet;
import com.example.legokp.models.Theme;
import com.example.legokp.models.ThemeResponse;
import com.example.legokp.models.LegoSetResponse;
import com.example.legokp.network.RetrofitClient;
import com.example.legokp.utils.Constants;
import com.example.legokp.viewmodels.LegoViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetsFragment extends Fragment {

    private RecyclerView rvSets, rvThemes;
    private LegoSetAdapter setAdapter;
    private ThemeAdapter themeAdapter;
    private ProgressBar progressBar;
    private SearchView searchView;
    private FloatingActionButton fabFilter, fabSort;  // ✨ НОВАЯ кнопка сортировки
    private ImageButton btnClearFilters;  // ✨ НОВАЯ кнопка очистки

    private LegoViewModel viewModel;

    private String currentTheme = null;
    private String currentSearch = null;
    private FilterBottomSheetFragment.FilterOptions filterOptions;
    private String currentSortOption = "name_asc";  // ✨ НОВОЕ - текущая сортировка

    private List<LegoSet> allSets = new ArrayList<>();

    private static final String TAG = "SetsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
        fabSort = view.findViewById(R.id.fabSort);  // ✨ НОВОЕ
        btnClearFilters = view.findViewById(R.id.btnClearFilters);  // ✨ НОВОЕ

        setupSearchView();
        setupFilterButton();
        setupSortButton();  // ✨ НОВОЕ
        setupClearFiltersButton();  // ✨ НОВОЕ
    }

    private void setupRecyclerViews() {
        // Sets Grid
        rvSets.setLayoutManager(new GridLayoutManager(getContext(), 2));

        setAdapter = new LegoSetAdapter(getContext(), (legoSet, position) -> {
            toggleFavorite(legoSet, position);
        });
        rvSets.setAdapter(setAdapter);

        // Themes Horizontal
        rvThemes.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        themeAdapter = new ThemeAdapter(getContext(), (theme, position) -> {
            currentTheme = theme.getName();
            showClearFiltersButton();  // ✨ НОВОЕ
            applyFiltersAndSort();
        });
        rvThemes.setAdapter(themeAdapter);
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::showLoading);

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearch = query;
                showClearFiltersButton();  // ✨ НОВОЕ
                applyFiltersAndSort();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearch = newText.isEmpty() ? null : newText;
                if (currentSearch == null) {
                    hideClearFiltersButtonIfNeeded();  // ✨ НОВОЕ
                } else {
                    showClearFiltersButton();  // ✨ НОВОЕ
                }
                applyFiltersAndSort();
                return true;
            }
        });
    }

    private void setupFilterButton() {
        fabFilter.setOnClickListener(v -> {
            FilterBottomSheetFragment sheet = FilterBottomSheetFragment.newInstance(options -> {
                filterOptions = options;
                currentSortOption = options.sortBy;  // ✨ Обновляем текущую сортировку
                showClearFiltersButton();  // ✨ НОВОЕ
                applyFiltersAndSort();
            });
            sheet.show(getParentFragmentManager(), "FilterBottomSheet");
        });
    }

    // ✨ НОВЫЙ метод - быстрая сортировка
    private void setupSortButton() {
        if (fabSort != null) {
            fabSort.setOnClickListener(v -> showSortDialog());
        }
    }

    // ✨ НОВЫЙ метод - показать диалог сортировки
    private void showSortDialog() {
        String[] sortOptions = {
                "Name (A-Z)",
                "Name (Z-A)",
                "Price: Low to High",
                "Price: High to Low",
                "Rating: High to Low",
                "Year: Newest First",
                "Year: Oldest First",
                "Parts: Most to Least",
                "Parts: Least to Most"
        };

        String[] sortValues = {
                "name_asc",
                "name_desc",
                "price_asc",
                "price_desc",
                "rating_desc",
                "year_desc",
                "year_asc",
                "parts_desc",
                "parts_asc"
        };

        // Найти текущий выбранный индекс
        int selectedIndex = 0;
        for (int i = 0; i < sortValues.length; i++) {
            if (sortValues[i].equals(currentSortOption)) {
                selectedIndex = i;
                break;
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Sort By")
                .setSingleChoiceItems(sortOptions, selectedIndex, (dialog, which) -> {
                    currentSortOption = sortValues[which];
                    applyFiltersAndSort();
                    dialog.dismiss();

                    Toast.makeText(getContext(),
                            "Sorted by: " + sortOptions[which],
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ✨ НОВЫЙ метод - кнопка очистки фильтров
    private void setupClearFiltersButton() {
        if (btnClearFilters != null) {
            btnClearFilters.setOnClickListener(v -> clearAllFilters());
        }
    }

    // ✨ НОВЫЙ метод - очистить все фильтры
    private void clearAllFilters() {
        currentTheme = null;
        currentSearch = null;
        filterOptions = null;
        currentSortOption = "name_asc";

        // Очистить UI
        searchView.setQuery("", false);
        searchView.clearFocus();
        themeAdapter.clearSelection();

        hideClearFiltersButton();
        applyFiltersAndSort();

        Toast.makeText(getContext(), "Filters cleared", Toast.LENGTH_SHORT).show();
    }

    // ✨ НОВЫЙ метод - показать кнопку очистки
    private void showClearFiltersButton() {
        if (btnClearFilters != null) {
            btnClearFilters.setVisibility(View.VISIBLE);
        }
    }

    // ✨ НОВЫЙ метод - скрыть кнопку очистки если нет фильтров
    private void hideClearFiltersButtonIfNeeded() {
        if (currentTheme == null && currentSearch == null && filterOptions == null) {
            if (btnClearFilters != null) {
                btnClearFilters.setVisibility(View.GONE);
            }
        }
    }

    // ✨ НОВЫЙ метод - скрыть кнопку очистки
    private void hideClearFiltersButton() {
        if (btnClearFilters != null) {
            btnClearFilters.setVisibility(View.GONE);
        }
    }

    private void loadThemes() {
        RetrofitClient.getApiService().getThemes().enqueue(new Callback<ThemeResponse>() {
            @Override
            public void onResponse(Call<ThemeResponse> call, Response<ThemeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Theme> themes = response.body().getThemes();
                    if (themes != null) themeAdapter.updateThemes(themes);
                }
            }

            @Override
            public void onFailure(Call<ThemeResponse> call, Throwable t) {
                Log.e(TAG, "Themes error: " + t.getMessage());
            }
        });
    }

    private void loadSets() {
        showLoading(true);

        RetrofitClient.getApiService()
                .getLegoSets(1, Constants.PAGE_SIZE, null, null, null)
                .enqueue(new Callback<LegoSetResponse>() {
                    @Override
                    public void onResponse(Call<LegoSetResponse> call,
                                           Response<LegoSetResponse> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            allSets = removeDuplicates(response.body().getResults());
                            applyFiltersAndSort();
                            Log.d(TAG, "Loaded unique sets: " + allSets.size());
                        } else {
                            Toast.makeText(getContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LegoSetResponse> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<LegoSet> removeDuplicates(List<LegoSet> sets) {
        Map<String, LegoSet> map = new LinkedHashMap<>();
        for (LegoSet set : sets) {
            if (set.getSetNum() != null) {
                map.put(set.getSetNum(), set);
            }
        }
        return new ArrayList<>(map.values());
    }

    private void applyFiltersAndSort() {
        List<LegoSet> filtered = new ArrayList<>(allSets);

        // Применить фильтр по теме
        if (currentTheme != null) {
            filtered.removeIf(set -> set.getTheme() == null ||
                    !set.getTheme().equalsIgnoreCase(currentTheme));
        }

        // Применить поиск
        if (currentSearch != null) {
            filtered.removeIf(set -> set.getName() == null ||
                    !set.getName().toLowerCase().contains(currentSearch.toLowerCase()));
        }

        // Применить дополнительные фильтры из bottom sheet
        if (filterOptions != null) {
            // Фильтр по цене
            if (filterOptions.priceRange != null) {
                filtered = applyPriceFilter(filtered, filterOptions.priceRange);
            }

            // Фильтр по возрасту
            if (filterOptions.ageRange != null) {
                filtered = applyAgeFilter(filtered, filterOptions.ageRange);
            }

            // Фильтр по количеству деталей
            filtered = applyPartsFilter(filtered, filterOptions.minParts, filterOptions.maxParts);
        }

        // Применить сортировку
        sortSets(filtered, currentSortOption);

        // Обновить UI
        setAdapter.updateSets(filtered);

        // Показать количество результатов
        String resultText = filtered.size() + " sets found";
        if (getActivity() != null) {
            getActivity().setTitle(resultText);
        }
    }

    // ✨ УЛУЧШЕННАЯ сортировка с большим количеством опций
    private void sortSets(List<LegoSet> sets, String sortBy) {
        switch (sortBy) {
            case "name_asc":
                sets.sort(Comparator.comparing(LegoSet::getName, String.CASE_INSENSITIVE_ORDER));
                break;
            case "name_desc":
                sets.sort((a, b) -> b.getName().compareToIgnoreCase(a.getName()));
                break;
            case "price_asc":
                sets.sort(Comparator.comparingDouble(LegoSet::getPrice));
                break;
            case "price_desc":
                sets.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            case "rating_desc":
                sets.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
                break;
            case "year_desc":
                sets.sort((a, b) -> Integer.compare(b.getYear(), a.getYear()));
                break;
            case "year_asc":
                sets.sort(Comparator.comparingInt(LegoSet::getYear));
                break;
            case "parts_desc":
                sets.sort((a, b) -> Integer.compare(b.getNumParts(), a.getNumParts()));
                break;
            case "parts_asc":
                sets.sort(Comparator.comparingInt(LegoSet::getNumParts));
                break;
            default:
                sets.sort(Comparator.comparing(LegoSet::getName, String.CASE_INSENSITIVE_ORDER));
        }
    }

    // ✨ НОВЫЕ методы фильтрации
    private List<LegoSet> applyPriceFilter(List<LegoSet> sets, String priceRange) {
        List<LegoSet> filtered = new ArrayList<>();
        for (LegoSet set : sets) {
            double price = set.getPrice();
            boolean matches = false;

            switch (priceRange) {
                case "Under $50":
                    matches = price < 50;
                    break;
                case "$50 - $100":
                    matches = price >= 50 && price <= 100;
                    break;
                case "$100 - $200":
                    matches = price > 100 && price <= 200;
                    break;
                case "Over $200":
                    matches = price > 200;
                    break;
            }

            if (matches) {
                filtered.add(set);
            }
        }
        return filtered;
    }

    private List<LegoSet> applyAgeFilter(List<LegoSet> sets, String ageRange) {
        List<LegoSet> filtered = new ArrayList<>();
        for (LegoSet set : sets) {
            String setAge = set.getAgeRange();
            if (setAge != null && setAge.contains(ageRange)) {
                filtered.add(set);
            }
        }
        return filtered;
    }

    private List<LegoSet> applyPartsFilter(List<LegoSet> sets, int minParts, int maxParts) {
        List<LegoSet> filtered = new ArrayList<>();
        for (LegoSet set : sets) {
            int parts = set.getNumParts();
            if (parts >= minParts && parts <= maxParts) {
                filtered.add(set);
            }
        }
        return filtered;
    }

    private void toggleFavorite(LegoSet legoSet, int position) {
        boolean newState = !legoSet.isFavorite();
        setAdapter.updateFavoriteStatus(position, newState);
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}