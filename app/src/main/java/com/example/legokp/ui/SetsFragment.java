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
import com.example.legokp.utils.ModelMapper;
import com.example.legokp.viewmodels.LegoViewModel;

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
    private LegoViewModel viewModel;

    private String currentTheme = null;
    private String currentSearch = null;

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

    private void initViews(View view) {
        rvSets = view.findViewById(R.id.rvSets);
        rvThemes = view.findViewById(R.id.rvThemes);
        progressBar = view.findViewById(R.id.progressBar);
        searchView = view.findViewById(R.id.searchView);

        setupSearchView();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearch = query;
                loadSetsWithSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    currentSearch = null;
                    loadSetsFromDatabase();
                }
                return true;
            }
        });
    }

    private void setupRecyclerViews() {
        // Sets RecyclerView - Grid with 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvSets.setLayoutManager(gridLayoutManager);

        setAdapter = new LegoSetAdapter(getContext(), (legoSet, position) -> {
            toggleFavorite(legoSet.getSetNum());
        });
        rvSets.setAdapter(setAdapter);

        // Themes RecyclerView - Horizontal
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvThemes.setLayoutManager(horizontalLayoutManager);

        themeAdapter = new ThemeAdapter(getContext(), (theme, position) -> {
            currentTheme = theme.getName();
            loadSets();
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
                    themeAdapter.updateThemes(themes);
                }
            }

            @Override
            public void onFailure(Call<ThemeResponse> call, Throwable t) {
                Log.e("SetsFragment", "Failed to load themes: " + t.getMessage());
            }
        });
    }

    private void loadSets() {
        // Fetch from API and cache to database
        viewModel.fetchSetsFromApi(1, Constants.PAGE_SIZE, currentTheme, null, currentSearch);
    }

    private void loadSetsFromDatabase() {
        // Just observe the database - it's already set up in setupObservers()
        if (currentTheme != null) {
            viewModel.getSetsByTheme(currentTheme).observe(getViewLifecycleOwner(), entities -> {
                if (entities != null) {
                    List<LegoSet> sets = ModelMapper.toModelList(entities);
                    setAdapter.updateSets(sets);
                }
            });
        }
    }

    private void loadSetsWithSearch(String query) {
        viewModel.searchSets(query).observe(getViewLifecycleOwner(), entities -> {
            if (entities != null) {
                List<LegoSet> sets = ModelMapper.toModelList(entities);
                setAdapter.updateSets(sets);

                if (sets.isEmpty()) {
                    Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void toggleFavorite(String setNum) {
        viewModel.toggleFavorite(setNum, (isFavorite, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            } else {
                String message = isFavorite ? "Added to favorites" : "Removed from favorites";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}