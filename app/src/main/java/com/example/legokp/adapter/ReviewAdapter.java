package com.example.legokp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.legokp.R;
import com.example.legokp.database.entity.ReviewEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Адаптер для отображения списка отзывов
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<ReviewEntity> reviews;
    private Context context;
    private OnReviewActionListener listener;
    private String currentUserId;

    public interface OnReviewActionListener {
        void onEditClick(ReviewEntity review);
        void onDeleteClick(ReviewEntity review);
    }

    public ReviewAdapter(Context context, String currentUserId, OnReviewActionListener listener) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.listener = listener;
        this.reviews = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewEntity review = reviews.get(position);

        // Username
        holder.tvUsername.setText(review.getUsername());

        // Rating
        holder.ratingBar.setRating(review.getRating());
        holder.tvRating.setText(String.format(Locale.US, "%.1f", review.getRating()));

        // Comment
        if (review.getComment() != null && !review.getComment().isEmpty()) {
            holder.tvComment.setText(review.getComment());
            holder.tvComment.setVisibility(View.VISIBLE);
        } else {
            holder.tvComment.setVisibility(View.GONE);
        }

        // Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(review.getCreatedAt()));
        holder.tvDate.setText(formattedDate);

        // Синхронизация
        if (!review.isSynced()) {
            holder.tvSyncStatus.setVisibility(View.VISIBLE);
            holder.tvSyncStatus.setText("Not synced");
        } else {
            holder.tvSyncStatus.setVisibility(View.GONE);
        }

        // Показать кнопки редактирования только для своих отзывов
        boolean isOwnReview = review.getUserId().equals(currentUserId);
        holder.btnEdit.setVisibility(isOwnReview ? View.VISIBLE : View.GONE);
        holder.btnDelete.setVisibility(isOwnReview ? View.VISIBLE : View.GONE);

        // Кнопка редактирования
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(review);
            }
        });

        // Кнопка удаления
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(review);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    public void updateReviews(List<ReviewEntity> newReviews) {
        if (newReviews != null) {
            this.reviews = new ArrayList<>(newReviews);
            notifyDataSetChanged();
        }
    }

    public void addReview(ReviewEntity review) {
        if (review != null) {
            this.reviews.add(0, review); // Добавить в начало
            notifyItemInserted(0);
        }
    }

    public void removeReview(int position) {
        if (position >= 0 && position < reviews.size()) {
            reviews.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, reviews.size());
        }
    }

    public void updateReview(int position, ReviewEntity review) {
        if (position >= 0 && position < reviews.size()) {
            reviews.set(position, review);
            notifyItemChanged(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvRating, tvComment, tvDate, tvSyncStatus;
        RatingBar ratingBar;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvSyncStatus = itemView.findViewById(R.id.tvSyncStatus);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}