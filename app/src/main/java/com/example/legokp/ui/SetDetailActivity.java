package com.example.legokp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.legokp.R;
import com.example.legokp.models.FavoriteRequest;
import com.example.legokp.models.FavoriteResponse;
import com.example.legokp.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetDetailActivity extends AppCompatActivity {

    private ImageView ivSetImage;
    private TextView tvName, tvDescription, tvPrice, tvRating, tvAge, tvParts, tvTheme, tvYear;
    private ImageButton btnFavorite;
    private MaterialButton btnAddToBag;

    private String setNum;
    private boolean isFavorite;

    private static final String TAG = "SetDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Set Details");
        }

        initViews();
        loadData();
        setupListeners();
    }

    private void initViews() {
        ivSetImage = findViewById(R.id.ivSetImage);
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvPrice = findViewById(R.id.tvPrice);
        tvRating = findViewById(R.id.tvRating);
        tvAge = findViewById(R.id.tvAge);
        tvParts = findViewById(R.id.tvParts);
        tvTheme = findViewById(R.id.tvTheme);
        tvYear = findViewById(R.id.tvYear);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnAddToBag = findViewById(R.id.btnAddToBag);
    }

    private void loadData() {
        // Получить данные из Intent
        setNum = getIntent().getStringExtra("set_num");
        String name = getIntent().getStringExtra("name");
        double price = getIntent().getDoubleExtra("price", 0.0);
        String imageUrl = getIntent().getStringExtra("image_url");
        double rating = getIntent().getDoubleExtra("rating", 0.0);
        String ageRange = getIntent().getStringExtra("age_range");
        int numParts = getIntent().getIntExtra("num_parts", 0);
        String theme = getIntent().getStringExtra("theme");
        int year = getIntent().getIntExtra("year", 0);
        String description = getIntent().getStringExtra("description");
        isFavorite = getIntent().getBooleanExtra("is_favorite", false);

        // Установить данные
        tvName.setText(name);
        tvDescription.setText(description != null ? description : "No description available");
        tvPrice.setText(String.format(Locale.US, "$%.2f", price));
        tvRating.setText(String.format(Locale.US, "⭐ %.1f", rating));
        tvAge.setText("Age: " + ageRange);
        tvParts.setText("Parts: " + numParts);
        tvTheme.setText("Theme: " + theme);
        tvYear.setText("Year: " + year);

        // Загрузить изображение
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_lego_placeholder)
                .error(R.drawable.ic_lego_placeholder)
                .into(ivSetImage);

        // Установить иконку избранного
        updateFavoriteIcon();
    }

    private void setupListeners() {
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        btnAddToBag.setOnClickListener(v -> {
            Toast.makeText(this, "Added to bag!", Toast.LENGTH_SHORT).show();
        });
    }

    private void toggleFavorite() {
        FavoriteRequest request = new FavoriteRequest(setNum);

        RetrofitClient.getApiService().toggleFavorite(request).enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isFavorite = response.body().isFavorite();
                    updateFavoriteIcon();

                    String message = isFavorite ? "Added to favorites" : "Removed from favorites";
                    Toast.makeText(SetDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                Toast.makeText(SetDetailActivity.this,
                        "Failed to update favorite",
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}