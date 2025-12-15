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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.legokp.R;
import com.example.legokp.adapter.LegoSetAdapter;
import com.example.legokp.models.FavoriteRequest;
import com.example.legokp.models.FavoriteResponse;
import com.example.legokp.models.LegoSetResponse;
import com.example.legokp.network.RetrofitClient;
import com.example.legokp.models.LegoSet;
import com.example.legokp.viewmodels.LegoViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private LegoSetAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private LegoViewModel viewModel; // Declared viewModel

    private List<LegoSet> favoritesList = new ArrayList<>();

    private static final String TAG = "FavoritesFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LegoViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupObservers();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the list of favorites when the fragment is resumed
        loadFavorites();
    }

    private void initViews(View view) {
        rvFavorites = view.findViewById(R.id.rvFavorites);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        rvFavorites.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Pass a lambda to handle item clicks (for removing favorites)
        adapter = new LegoSetAdapter(getContext(), (legoSet, position) -> {
            removeFavorite(legoSet, position);
        });
        rvFavorites.setAdapter(adapter);
    }

    private void setupObservers() {
        // Observe loading state from the ViewModel
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            showLoading(isLoading);
        });
    }

    private void loadFavorites() {
        showLoading(true);

        // In a real API, you would have a dedicated endpoint for favorites.
        // Here, we filter all sets.
        RetrofitClient.getApiService().getLegoSets(1, 100, null, null, null)
                .enqueue(new Callback<LegoSetResponse>() {
                    @Override
                    public void onResponse(Call<LegoSetResponse> call, Response<LegoSetResponse> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            List<LegoSet> allSets = response.body().getResults();
                            favoritesList.clear(); // Clear the list before adding new items

                            if (allSets != null) {
                                // Filter for sets marked as favorite
                                for (LegoSet set : allSets) {
                                    if (set.isFavorite()) {
                                        favoritesList.add(set);
                                    }
                                }
                            }

                            if (favoritesList.isEmpty()) {
                                showEmptyState(true);
                            } else {
                                showEmptyState(false);
                                adapter.updateSets(favoritesList);
                                Log.d(TAG, "Loaded " + favoritesList.size() + " favorites");
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to load favorites: " + response.code(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error loading favorites: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<LegoSetResponse> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failure loading favorites: " + t.getMessage());
                    }
                });
    }

    private void removeFavorite(LegoSet legoSet, int position) {
        // Immediately remove from the list for a responsive UI
        if (position >= 0 && position < adapter.getItemCount()) {
            favoritesList.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, favoritesList.size());


            // Show the empty state if the list becomes empty
            if (favoritesList.isEmpty()) {
                showEmptyState(true);
            }
        }

        FavoriteRequest request = new FavoriteRequest(legoSet.getSetNum());

        RetrofitClient.getApiService().toggleFavorite(request).enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FavoriteResponse favoriteResponse = response.body();

                    if (favoriteResponse.isSuccess()) {
                        Toast.makeText(getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Removed: " + legoSet.getName());
                    } else {
                        Toast.makeText(getContext(), "Failed: " + favoriteResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        // If removal failed on the server, reload the list to get the correct state
                        loadFavorites();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to remove", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error removing favorite: " + response.code());
                    // If there was an error, reload the list
                    loadFavorites();
                }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                // If there's a network error, reload the list
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failure removing favorite: " + t.getMessage());
                loadFavorites();
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
