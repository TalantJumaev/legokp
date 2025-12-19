package com.example.legokp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.legokp.R;
import com.example.legokp.database.entity.ReviewEntity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Bottom Sheet для добавления или редактирования отзыва
 */
public class AddReviewBottomSheet extends BottomSheetDialogFragment {

    private TextView tvTitle;
    private RatingBar ratingBar;
    private TextView tvRatingValue;
    private TextInputEditText etComment;
    private Button btnSubmit, btnCancel;

    private OnReviewSubmitListener listener;
    private ReviewEntity existingReview;
    private boolean isEditMode = false;

    public interface OnReviewSubmitListener {
        void onReviewSubmit(float rating, String comment);
        void onReviewUpdate(ReviewEntity review, float rating, String comment);
    }

    public static AddReviewBottomSheet newInstance(OnReviewSubmitListener listener) {
        AddReviewBottomSheet fragment = new AddReviewBottomSheet();
        fragment.listener = listener;
        return fragment;
    }

    public static AddReviewBottomSheet newInstanceForEdit(ReviewEntity review, OnReviewSubmitListener listener) {
        AddReviewBottomSheet fragment = new AddReviewBottomSheet();
        fragment.listener = listener;
        fragment.existingReview = review;
        fragment.isEditMode = true;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();

        if (isEditMode && existingReview != null) {
            loadExistingReview();
        }
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tvTitle);
        ratingBar = view.findViewById(R.id.ratingBar);
        tvRatingValue = view.findViewById(R.id.tvRatingValue);
        etComment = view.findViewById(R.id.etComment);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnCancel = view.findViewById(R.id.btnCancel);

        // Установить заголовок
        if (isEditMode) {
            tvTitle.setText("Edit Your Review");
            btnSubmit.setText("Update Review");
        } else {
            tvTitle.setText("Write a Review");
            btnSubmit.setText("Submit Review");
        }
    }

    private void setupListeners() {
        // Rating bar изменение
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            updateRatingText(rating);
        });

        // Кнопка Submit
        btnSubmit.setOnClickListener(v -> handleSubmit());

        // Кнопка Cancel
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void loadExistingReview() {
        if (existingReview != null) {
            ratingBar.setRating(existingReview.getRating());
            updateRatingText(existingReview.getRating());

            if (existingReview.getComment() != null) {
                etComment.setText(existingReview.getComment());
            }
        }
    }

    private void updateRatingText(float rating) {
        String ratingText;
        if (rating == 0) {
            ratingText = "No rating";
        } else {
            ratingText = String.format("%.1f stars", rating);
        }
        tvRatingValue.setText(ratingText);
    }

    private void handleSubmit() {
        float rating = ratingBar.getRating();
        String comment = etComment.getText() != null ?
                etComment.getText().toString().trim() : "";

        // Валидация
        if (rating == 0) {
            Toast.makeText(getContext(), "Please provide a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.isEmpty()) {
            Toast.makeText(getContext(), "Please write a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.length() < 10) {
            Toast.makeText(getContext(), "Comment must be at least 10 characters",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Отправить данные
        if (listener != null) {
            if (isEditMode) {
                listener.onReviewUpdate(existingReview, rating, comment);
            } else {
                listener.onReviewSubmit(rating, comment);
            }
        }

        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Установить начальный текст рейтинга
        updateRatingText(ratingBar.getRating());
    }
}