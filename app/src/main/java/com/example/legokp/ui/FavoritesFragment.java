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
import com.example.legokp.database.AppDatabase;
import com.example.legokp.database.entity.LegoSetEntity;
import com.example.legokp.models.LegoSet;
import com.example.legokp.utils.ModelMapper;
import com.example.legokp.viewmodels.LegoViewModel;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private LegoSetAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private LegoViewModel viewModel;
    private AppDatabase database;

    private List<LegoSet> favoritesList = new ArrayList<>();

    private static final String TAG = "FavoritesFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        viewModel = new ViewModelProvider(this).get(LegoViewModel.class);
        database = AppDatabase.getDatabase(requireContext());

        initViews(view);
        setupRecyclerView();
        setupObservers();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - loading favorites");
        loadFavoritesFromDB();
    }

    private void initViews(View view) {
        rvFavorites = view.findViewById(R.id.rvFavorites);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        rvFavorites.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new LegoSetAdapter(getContext(), this::removeFavoriteLocal);
        rvFavorites.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::showLoading);

        // ✅ LiveData observer для автоматического обновления
        viewModel.getFavoriteSets().observe(getViewLifecycleOwner(), entities -> {
            if (entities != null && !entities.isEmpty()) {
                List<LegoSet> sets = ModelMapper.toModelList(entities);

                favoritesList.clear();
                favoritesList.addAll(sets);

                showEmptyState(false);
                adapter.updateSets(favoritesList);

                Log.d(TAG, "LiveData updated: " + favoritesList.size() + " favorites");
            }
        });
    }

    private void loadFavoritesFromDB() {
        showLoading(true);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // ✅ Используем синхронный метод
                List<LegoSetEntity> entities = database.legoSetDao().getFavoriteSetsSync();

                Log.d(TAG, "Loaded from DB: " + entities.size() + " entities");

                List<LegoSet> sets = ModelMapper.toModelList(entities);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);

                        favoritesList.clear();
                        favoritesList.addAll(sets);

                        if (favoritesList.isEmpty()) {
                            showEmptyState(true);
                            Log.d(TAG, "No favorites - showing empty state");
                        } else {
                            showEmptyState(false);
                            adapter.updateSets(favoritesList);
                            Log.d(TAG, "Displaying " + favoritesList.size() + " favorites");
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading favorites: " + e.getMessage());
                e.printStackTrace();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        showEmptyState(true);
                        Toast.makeText(getContext(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void removeFavoriteLocal(LegoSet legoSet, int position) {
        if (legoSet == null) {
            Log.e(TAG, "LegoSet is null");
            return;
        }

        Log.d(TAG, "Removing from favorites: " + legoSet.getName());

        LegoSet removedSet = legoSet;
        int removedPosition = position;

        favoritesList.remove(position);
        adapter.removeItem(position);

        if (favoritesList.isEmpty()) {
            showEmptyState(true);
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                LegoSetEntity existing = database.legoSetDao().getSetByNum(legoSet.getSetNum());

                if (existing != null) {
                    database.legoSetDao().updateFavoriteStatus(legoSet.getSetNum(), false);

                    Log.d(TAG, "Successfully removed from DB: " + removedSet.getName());

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(),
                                    "Removed from favorites",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    Log.w(TAG, "Set not found in DB: " + legoSet.getSetNum());

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(),
                                    "Removed from favorites",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error removing favorite: " + e.getMessage());
                e.printStackTrace();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        restoreItem(removedSet, removedPosition);

                        Toast.makeText(getContext(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void restoreItem(LegoSet set, int position) {
        int insertPosition = Math.min(position, favoritesList.size());
        favoritesList.add(insertPosition, set);

        adapter.updateSets(favoritesList);

        if (!favoritesList.isEmpty()) {
            showEmptyState(false);
        }

        Log.d(TAG, "Restored item: " + set.getName());
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