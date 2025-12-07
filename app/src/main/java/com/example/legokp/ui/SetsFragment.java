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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.legokp.R;
import com.example.legokp.adapter.LegoSetAdapter;
import com.example.legokp.adapter.ThemeAdapter;
import com.example.legokp.models.FavoriteRequest;
import com.example.legokp.models.FavoriteResponse;
import com.example.legokp.models.LegoSet;
import com.example.legokp.models.LegoSetResponse;
import com.example.legokp.models.Theme;
import com.example.legokp.models.ThemeResponse;
import com.example.legokp.network.RetrofitClient;
import com.example.legokp.utils.Constants;

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

    private String currentTheme = null;
    private String currentSearch = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sets, container, false);

        initViews(view);
        setupRecyclerViews();
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
                loadSets();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    currentSearch = null;
                    loadSets();
                }
                return true;
            }
        });
    }

    private void setupRecyclerViews() {
        // Sets RecyclerView - Grid с 2 колонками
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvSets.setLayoutManager(gridLayoutManager);

        setAdapter = new LegoSetAdapter(getContext(), (legoSet, position) -> {
            toggleFavorite(legoSet, position);
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
        showLoading(true);

        RetrofitClient.getApiService().getLegoSets(
                1,
                Constants.PAGE_SIZE,
                currentTheme,
                null,
                currentSearch
        ).enqueue(new Callback<LegoSetResponse>() {
            @Override
            public void onResponse(Call<LegoSetResponse> call, Response<LegoSetResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<LegoSet> sets = response.body().getResults();
                    setAdapter.updateSets(sets);
                } else {
                    Toast.makeText(getContext(),
                            "Failed to load sets",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LegoSetResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(),
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFavorite(LegoSet legoSet, int position) {
        FavoriteRequest request = new FavoriteRequest(legoSet.getSetNum());

        RetrofitClient.getApiService().toggleFavorite(request).enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isFavorite = response.body().isFavorite();
                    setAdapter.updateFavoriteStatus(position, isFavorite);

                    String message = isFavorite ? "Added to favorites" : "Removed from favorites";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Failed to update favorite",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
