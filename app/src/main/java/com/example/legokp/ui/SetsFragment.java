package com.example.legokp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.example.legokp.models.ThemeResponse;
import com.example.legokp.network.RetrofitClient;
import com.example.legokp.utils.ModelMapper;
import com.example.legokp.viewmodels.LegoViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    // ✨ ИСПРАВЛЕНО: Удалена кнопка fabSort
    private FloatingActionButton fabFilter, fabAddSet;
    private ImageButton btnClearFilters;

    private LegoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sets, container, false);

        viewModel = new ViewModelProvider(this).get(LegoViewModel.class);

        initViews(view);
        setupRecyclerViews();
        setupObservers();
        loadThemes();

        return view;
    }

    private void initViews(View view) {
        rvSets = view.findViewById(R.id.rvSets);
        rvThemes = view.findViewById(R.id.rvThemes);
        progressBar = view.findViewById(R.id.progressBar);
        searchView = view.findViewById(R.id.searchView);
        fabFilter = view.findViewById(R.id.fabFilter);
        fabAddSet = view.findViewById(R.id.fabAddSet);
        btnClearFilters = view.findViewById(R.id.btnClearFilters);

        setupSearchView();
        setupAddSetButton();
        setupFilterButton(); 
        setupClearFiltersButton();
    }

    private void setupRecyclerViews() {
        rvSets.setLayoutManager(new GridLayoutManager(getContext(), 2));
        setAdapter = new LegoSetAdapter(getContext(), this::toggleFavorite, this::deleteLegoSet);
        rvSets.setAdapter(setAdapter);

        rvThemes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        themeAdapter = new ThemeAdapter(getContext(), (theme, position) -> {
            viewModel.setTheme(theme.getName());
            showClearFiltersButton();
        });
        rvThemes.setAdapter(themeAdapter);
    }

    private void setupObservers() {
        viewModel.getFilteredSets().observe(getViewLifecycleOwner(), legoSetEntities -> {
            if (legoSetEntities != null) {
                List<LegoSet> sets = ModelMapper.toModelList(legoSetEntities);
                setAdapter.submitList(sets);
                if (getActivity() != null) {
                    getActivity().setTitle(sets.size() + " sets found");
                }
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::showLoading);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::showError);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(query);
                if (!query.isEmpty()) showClearFiltersButton();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                if (!newText.isEmpty()) showClearFiltersButton(); else hideClearFiltersButtonIfNeeded();
                return true;
            }
        });
    }

    private void setupFilterButton() {
        fabFilter.setOnClickListener(v -> {
            FilterBottomSheetFragment sheet = FilterBottomSheetFragment.newInstance(options -> {
                viewModel.setAdvancedFilters(options.theme, options.minYear, options.maxYear, options.minParts, options.maxParts);
                viewModel.setSortOrder(options.sortBy);
                showClearFiltersButton();
            });
            sheet.show(getParentFragmentManager(), "FilterBottomSheet");
        });
    }

    private void setupAddSetButton() {
        fabAddSet.setOnClickListener(v -> {
            AddLegoSetBottomSheetFragment sheet = AddLegoSetBottomSheetFragment.newInstance(this::addNewLegoSet);
            sheet.show(getParentFragmentManager(), "AddLegoSetBottomSheet");
        });
    }

    private void setupClearFiltersButton() {
        btnClearFilters.setOnClickListener(v -> {
            viewModel.clearFilters();
            searchView.setQuery("", false);
            themeAdapter.clearSelection();
            hideClearFiltersButton();
        });
    }

    private void showClearFiltersButton() {
        if (btnClearFilters != null) btnClearFilters.setVisibility(View.VISIBLE);
    }

    private void hideClearFiltersButtonIfNeeded() {
        if (searchView.getQuery().length() == 0 && themeAdapter.getSelectedPosition() == -1) {
             if (btnClearFilters != null) btnClearFilters.setVisibility(View.GONE);
        }
    }

     private void hideClearFiltersButton() {
        if (btnClearFilters != null) {
            btnClearFilters.setVisibility(View.GONE);
        }
    }

    private void addNewLegoSet(LegoSet newSet) {
        viewModel.addLegoSet(newSet, error -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (error == null) {
                        Toast.makeText(getContext(), "Set added successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void deleteLegoSet(LegoSet legoSet, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Set")
                .setMessage("Are you sure you want to delete this set?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteLegoSet(legoSet, error -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (error == null) {
                                    Toast.makeText(getContext(), "Set deleted successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toggleFavorite(LegoSet legoSet, int position) {
        viewModel.toggleFavorite(legoSet.getSetNum(), (isFavorite, error) -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (error != null) {
                        showError("Error updating favorite status");
                    }
                });
            }
        });
    }

    private void loadThemes() {
        RetrofitClient.getApiService().getThemes().enqueue(new Callback<ThemeResponse>() {
            @Override
            public void onResponse(Call<ThemeResponse> call, Response<ThemeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    themeAdapter.updateThemes(response.body().getThemes());
                }
            }

            @Override
            public void onFailure(Call<ThemeResponse> call, Throwable t) {
                showError(t.getMessage());
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
