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
    private FloatingActionButton fabFilter;

    private LegoViewModel viewModel;

    private String currentTheme = null;
    private String currentSearch = null;
    private FilterBottomSheetFragment.FilterOptions filterOptions;

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

        setupSearchView();
        setupFilterButton();
    }

    private void setupRecyclerViews() {
        rvSets.setLayoutManager(new GridLayoutManager(getContext(), 2));

        setAdapter = new LegoSetAdapter(getContext(), (legoSet, position) -> {
            toggleFavorite(legoSet, position);
        });
        rvSets.setAdapter(setAdapter);

        rvThemes.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        themeAdapter = new ThemeAdapter(getContext(), (theme, position) -> {
            currentTheme = theme.getName();
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
                applyFiltersAndSort();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearch = newText.isEmpty() ? null : newText;
                applyFiltersAndSort();
                return true;
            }
        });
    }

    private void setupFilterButton() {
        fabFilter.setOnClickListener(v -> {
            FilterBottomSheetFragment sheet = FilterBottomSheetFragment.newInstance(options -> {
                filterOptions = options;
                applyFiltersAndSort();
            });
            sheet.show(getParentFragmentManager(), "FilterBottomSheet");
        });
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

        if (currentTheme != null) {
            filtered.removeIf(set -> set.getTheme() == null ||
                    !set.getTheme().equalsIgnoreCase(currentTheme));
        }

        if (currentSearch != null) {
            filtered.removeIf(set -> set.getName() == null ||
                    !set.getName().toLowerCase().contains(currentSearch.toLowerCase()));
        }

        if (filterOptions != null && filterOptions.sortBy != null) {
            sortSets(filtered, filterOptions.sortBy);
        }

        setAdapter.updateSets(filtered);
    }

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
            case "year_desc":
                sets.sort((a, b) -> Integer.compare(b.getYear(), a.getYear()));
                break;
        }
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
