package com.example.legokp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.legokp.R;
import com.example.legokp.adapter.LegoSetAdapter;
import com.example.legokp.models.FavoriteRequest;
import com.example.legokp.models.FavoriteResponse;
import com.example.legokp.models.LegoSet;
import com.example.legokp.models.LegoSetResponse;
import com.example.legokp.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private LegoSetAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;  // ИСПРАВЛЕНО: было TextView

    private static final String TAG = "FavoritesFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        initViews(view);
        setupRecyclerView();
        loadFavorites();

        return view;
    }

    private void initViews(View view) {
        rvFavorites = view.findViewById(R.id.rvFavorites);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);  // ИСПРАВЛЕНО
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvFavorites.setLayoutManager(gridLayoutManager);

        adapter = new LegoSetAdapter(getContext(), (legoSet, position) -> {
            removeFavorite(legoSet, position);
        });
        rvFavorites.setAdapter(adapter);
    }

    private void loadFavorites() {
        showLoading(true);

        // В реальном API был бы отдельный endpoint для избранного
        // Здесь мы просто фильтруем все наборы
        RetrofitClient.getApiService().getLegoSets(1, 100, null, null, null)
                .enqueue(new Callback<LegoSetResponse>() {
                    @Override
                    public void onResponse(Call<LegoSetResponse> call, Response<LegoSetResponse> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            List<LegoSet> allSets = response.body().getResults();
                            List<LegoSet> favoriteSets = new ArrayList<>();

                            // Фильтруем только избранные
                            if (allSets != null) {
                                for (LegoSet set : allSets) {
                                    if (set.isFavorite()) {
                                        favoriteSets.add(set);
                                    }
                                }
                            }

                            if (favoriteSets.isEmpty()) {
                                showEmptyState(true);
                            } else {
                                showEmptyState(false);
                                adapter.updateSets(favoriteSets);
                            }
                        } else {
                            Toast.makeText(getContext(),
                                    "Failed to load favorites: " + response.code(),
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

    private void removeFavorite(LegoSet legoSet, int position) {
        FavoriteRequest request = new FavoriteRequest(legoSet.getSetNum());

        RetrofitClient.getApiService().toggleFavorite(request).enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                    loadFavorites(); // Перезагрузить список
                }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to remove", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Remove favorite error: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmptyState(boolean show) {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (rvFavorites != null) {
            rvFavorites.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}