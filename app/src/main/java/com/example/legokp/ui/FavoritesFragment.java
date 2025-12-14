package com.example.legokp.ui;

import android.os.Bundle;
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
import com.example.legokp.models.LegoSet;
import com.example.legokp.utils.ModelMapper;
import com.example.legokp.viewmodels.LegoViewModel;

import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private LegoSetAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private LegoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        viewModel = new ViewModelProvider(this).get(LegoViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupObservers();

        return view;
    }

    private void initViews(View view) {
        rvFavorites = view.findViewById(R.id.rvFavorites);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvFavorites.setLayoutManager(gridLayoutManager);

        adapter = new LegoSetAdapter(getContext(), (legoSet, position) -> {
            removeFavorite(legoSet.getSetNum());
        });
        rvFavorites.setAdapter(adapter);
    }

    private void setupObservers() {
        // Observe favorite sets from database
        viewModel.getFavoriteSets().observe(getViewLifecycleOwner(), entities -> {
            if (entities != null) {
                List<LegoSet> favoriteSets = ModelMapper.toModelList(entities);

                if (favoriteSets.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                    adapter.updateSets(favoriteSets);
                }
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            showLoading(isLoading);
        });
    }

    private void removeFavorite(String setNum) {
        viewModel.toggleFavorite(setNum, (isFavorite, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), "Failed to remove", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
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