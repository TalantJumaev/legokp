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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.legokp.R;
import com.example.legokp.adapter.MinifigAdapter;
import com.example.legokp.models.Minifig;
import com.example.legokp.models.MinifigResponse;
import com.example.legokp.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MinifigsFragment extends Fragment {

    private RecyclerView recyclerView;
    private MinifigAdapter adapter;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_minifigs, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MinifigAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        fetchMinifigs();

        return view;
    }

    private void fetchMinifigs() {
        showLoading(true);

        RetrofitClient.getApiService().getMinifigs().enqueue(new Callback<MinifigResponse>() {
            @Override
            public void onResponse(Call<MinifigResponse> call, Response<MinifigResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Minifig> minifigs = response.body().getResults();
                    if (minifigs != null && !minifigs.isEmpty()) {
                        adapter.updateMinifigs(minifigs);
                        Log.d("API", "Loaded " + minifigs.size() + " minifigs");
                    } else {
                        Toast.makeText(getContext(), "No minifigs found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load data: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MinifigResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}