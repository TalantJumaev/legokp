package com.example.legokp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.legokp.R;
import com.example.legokp.adapter.ReviewAdapter;
import com.example.legokp.database.entity.ReviewEntity;
import com.example.legokp.models.FavoriteRequest;
import com.example.legokp.models.FavoriteResponse;
import com.example.legokp.network.RetrofitClient;
import com.example.legokp.utils.SessionManager;
import com.example.legokp.viewmodels.ReviewViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetDetailActivity extends AppCompatActivity {

    private ImageView ivSetImage;
    private TextView tvName, tvDescription, tvPrice, tvRating, tvAge, tvParts, tvTheme, tvYear;
    private ImageButton btnFavorite;
    private MaterialButton btnAddToBag, btnWriteReview;

    // ✨ НОВОЕ - Отзывы
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private TextView tvReviewCount, tvAverageRating, tvNoReviews;
    private LinearLayout layoutReviewsSection;
    private ProgressBar progressBarReviews;

    private ReviewViewModel reviewViewModel;
    private SessionManager sessionManager;

    private String setNum;
    private boolean isFavorite;
    private boolean isUpdatingFavorite = false;

    private static final String TAG = "SetDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_detail);

        sessionManager = new SessionManager(this);
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        initViews();
        loadData();
        setupListeners();
        setupReviewsObservers();
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

        // ✨ НОВОЕ - Инициализация view отзывов
        btnWriteReview = findViewById(R.id.btnWriteReview);
        rvReviews = findViewById(R.id.rvReviews);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvNoReviews = findViewById(R.id.tvNoReviews);
        layoutReviewsSection = findViewById(R.id.layoutReviewsSection);
        progressBarReviews = findViewById(R.id.progressBarReviews);

        setupReviewsRecyclerView();
    }

    private void setupReviewsRecyclerView() {
        rvReviews.setLayoutManager(new LinearLayoutManager(this));

        String currentUserId = sessionManager.getUserId();

        reviewAdapter = new ReviewAdapter(this, currentUserId, new ReviewAdapter.OnReviewActionListener() {
            @Override
            public void onEditClick(ReviewEntity review) {
                openEditReviewDialog(review);
            }

            @Override
            public void onDeleteClick(ReviewEntity review) {
                showDeleteReviewDialog(review);
            }
        });

        rvReviews.setAdapter(reviewAdapter);
    }

    private void loadData() {
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

        tvName.setText(name);
        tvDescription.setText(description != null ? description : "No description available");
        tvPrice.setText(String.format(Locale.US, "$%.2f", price));
        tvRating.setText(String.format(Locale.US, "⭐ %.1f", rating));
        tvAge.setText("Age: " + ageRange);
        tvParts.setText("Parts: " + numParts);
        tvTheme.setText("Theme: " + theme);
        tvYear.setText("Year: " + year);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_lego_placeholder)
                .error(R.drawable.ic_lego_placeholder)
                .into(ivSetImage);

        updateFavoriteIcon();

        // ✨ НОВОЕ - Загрузить отзывы
        loadReviews();
    }

    private void loadReviews() {
        if (setNum == null) return;

        showReviewsLoading(true);

        // Загрузить отзывы из локальной БД
        reviewViewModel.getReviewsForSet(setNum).observe(this, reviews -> {
            showReviewsLoading(false);

            if (reviews != null && !reviews.isEmpty()) {
                reviewAdapter.updateReviews(reviews);
                tvNoReviews.setVisibility(View.GONE);
                rvReviews.setVisibility(View.VISIBLE);
            } else {
                tvNoReviews.setVisibility(View.VISIBLE);
                rvReviews.setVisibility(View.GONE);
            }
        });

        // Загрузить средний рейтинг
        reviewViewModel.getAverageRating(setNum).observe(this, avgRating -> {
            if (avgRating != null && avgRating > 0) {
                tvAverageRating.setText(String.format(Locale.US, "⭐ %.1f", avgRating));
            } else {
                tvAverageRating.setText("⭐ No ratings yet");
            }
        });

        // Загрузить количество отзывов
        reviewViewModel.getReviewCount(setNum).observe(this, count -> {
            if (count != null && count > 0) {
                String reviewText = count == 1 ? "review" : "reviews";
                tvReviewCount.setText(count + " " + reviewText);
            } else {
                tvReviewCount.setText("0 reviews");
            }
        });

        // Попытаться загрузить с сервера
        reviewViewModel.fetchReviewsFromApi(setNum);
    }

    private void setupListeners() {
        btnFavorite.setOnClickListener(v -> {
            if (!isUpdatingFavorite) {
                toggleFavorite();
            }
        });

        btnAddToBag.setOnClickListener(v -> {
            Toast.makeText(this, "Added to bag! 🛍️", Toast.LENGTH_SHORT).show();
        });

        // ✨ НОВОЕ - Кнопка написать отзыв
        btnWriteReview.setOnClickListener(v -> {
            checkAndOpenReviewDialog();
        });
    }

    private void setupReviewsObservers() {
        // Наблюдать за сообщениями об ошибках
        reviewViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Наблюдать за успешными операциями
        reviewViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndOpenReviewDialog() {
        String userId = sessionManager.getUserId();

        // Проверить, оставлял ли пользователь отзыв
        reviewViewModel.checkUserReview(setNum, userId, hasReviewed -> {
            runOnUiThread(() -> {
                if (hasReviewed) {
                    // Если уже оставлял - предложить редактировать
                    reviewViewModel.getUserReview(setNum, userId, review -> {
                        runOnUiThread(() -> {
                            if (review != null) {
                                openEditReviewDialog(review);
                            }
                        });
                    });
                } else {
                    // Открыть диалог для нового отзыва
                    openAddReviewDialog();
                }
            });
        });
    }

    private void openAddReviewDialog() {
        AddReviewBottomSheet bottomSheet = AddReviewBottomSheet.newInstance(
                new AddReviewBottomSheet.OnReviewSubmitListener() {
                    @Override
                    public void onReviewSubmit(float rating, String comment) {
                        addReview(rating, comment);
                    }

                    @Override
                    public void onReviewUpdate(ReviewEntity review, float rating, String comment) {
                        // Not used here
                    }
                }
        );

        bottomSheet.show(getSupportFragmentManager(), "AddReviewBottomSheet");
    }

    private void openEditReviewDialog(ReviewEntity review) {
        AddReviewBottomSheet bottomSheet = AddReviewBottomSheet.newInstanceForEdit(
                review,
                new AddReviewBottomSheet.OnReviewSubmitListener() {
                    @Override
                    public void onReviewSubmit(float rating, String comment) {
                        // Not used here
                    }

                    @Override
                    public void onReviewUpdate(ReviewEntity existingReview, float rating, String comment) {
                        updateReview(existingReview, rating, comment);
                    }
                }
        );

        bottomSheet.show(getSupportFragmentManager(), "EditReviewBottomSheet");
    }

    private void addReview(float rating, String comment) {
        String userId = sessionManager.getUserId();
        String username = sessionManager.getUsername();

        reviewViewModel.addReview(setNum, userId, username, rating, comment);
    }

    private void updateReview(ReviewEntity review, float rating, String comment) {
        review.setRating(rating);
        review.setComment(comment);

        reviewViewModel.updateReview(review);
    }

    private void showDeleteReviewDialog(ReviewEntity review) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Review")
                .setMessage("Are you sure you want to delete this review?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    reviewViewModel.deleteReview(review);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showReviewsLoading(boolean show) {
        if (progressBarReviews != null) {
            progressBarReviews.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void toggleFavorite() {
        if (isUpdatingFavorite) {
            return;
        }

        isUpdatingFavorite = true;
        isFavorite = !isFavorite;
        updateFavoriteIcon();

        FavoriteRequest request = new FavoriteRequest(setNum);

        RetrofitClient.getApiService().toggleFavorite(request).enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                isUpdatingFavorite = false;

                if (response.isSuccessful() && response.body() != null) {
                    FavoriteResponse favoriteResponse = response.body();

                    if (favoriteResponse.isSuccess()) {
                        isFavorite = favoriteResponse.isFavorite();
                        updateFavoriteIcon();

                        String message = isFavorite ? "Added to favorites ❤️" : "Removed from favorites";
                        Toast.makeText(SetDetailActivity.this, message, Toast.LENGTH_SHORT).show();

                        Log.d(TAG, "Favorite toggled successfully: " + isFavorite);
                    } else {
                        isFavorite = !isFavorite;
                        updateFavoriteIcon();
                        Toast.makeText(SetDetailActivity.this,
                                "Failed: " + favoriteResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    isFavorite = !isFavorite;
                    updateFavoriteIcon();
                    Toast.makeText(SetDetailActivity.this,
                            "Failed to update favorite",
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                isUpdatingFavorite = false;
                isFavorite = !isFavorite;
                updateFavoriteIcon();

                Toast.makeText(SetDetailActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failure: " + t.getMessage());
            }
        });
    }

    private void updateFavoriteIcon() {
        if (btnFavorite != null) {
            if (isFavorite) {
                btnFavorite.setImageResource(R.drawable.ic_favorite);
                btnFavorite.setContentDescription("Remove from favorites");
            } else {
                btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                btnFavorite.setContentDescription("Add to favorites");
            }
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

    @Override
    public void finish() {
        setResult(RESULT_OK);
        super.finish();
    }
}