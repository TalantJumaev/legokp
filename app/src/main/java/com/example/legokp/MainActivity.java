package com.example.legokp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.legokp.adapter.MinifigAdapter;
import com.example.legokp.models.Minifig;
import com.example.legokp.models.MinifigResponse;
import com.example.legokp.network.LegoApiService;
import com.example.legokp.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MinifigAdapter adapter;
    private static final String API_KEY = "key 407a3fae79051f8cda0c06bf216cce8c";  // Замените!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Фикс: Пустой адаптер сразу, чтобы избежать warning
        adapter = new MinifigAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        fetchMinifigs();
    }

    private void fetchMinifigs() {
        LegoApiService apiService = RetrofitClient.getClient().create(LegoApiService.class);
        Call<MinifigResponse> call = apiService.getMinifigs();  // No API_KEY parameter

        call.enqueue(new Callback<MinifigResponse>() {
            @Override
            public void onResponse(Call<MinifigResponse> call, Response<MinifigResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Minifig> minifigs = response.body().getResults();
                    if (minifigs != null && !minifigs.isEmpty()) {
                        adapter.updateMinifigs(minifigs);
                        Log.d("API", "Loaded " + minifigs.size() + " minifigs");
                    } else {
                        Toast.makeText(MainActivity.this, "No minifigs found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load data: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("API", "Error code: " + response.code() + " Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<MinifigResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API", "Failure: " + t.getMessage());
            }
        });
    }
}