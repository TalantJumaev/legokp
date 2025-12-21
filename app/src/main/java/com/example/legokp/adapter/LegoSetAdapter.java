package com.example.legokp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.legokp.R;
import com.example.legokp.models.LegoSet;
import com.example.legokp.ui.SetDetailActivity;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;
import java.util.Objects;

public class LegoSetAdapter extends ListAdapter<LegoSet, LegoSetAdapter.ViewHolder> {

    private final Context context;
    private final OnFavoriteClickListener favoriteClickListener;
    private final OnDeleteClickListener deleteClickListener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(LegoSet legoSet, int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(LegoSet legoSet, int position);
    }

    public LegoSetAdapter(Context context, OnFavoriteClickListener favoriteListener, OnDeleteClickListener deleteListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.favoriteClickListener = favoriteListener;
        this.deleteClickListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lego_set, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LegoSet set = getItem(position);
        if (set == null) return;

        holder.tvName.setText(set.getName());
        holder.tvPrice.setText(String.format(Locale.US, "$%.2f", set.getPrice()));
        holder.tvAge.setText(set.getAgeRange());
        holder.tvParts.setText(String.valueOf(set.getNumParts()));
        holder.tvRating.setText(String.format(Locale.US, "%.1f", set.getRating()));

        Glide.with(context)
                .load(set.getSetImgUrl())
                .placeholder(R.drawable.ic_lego_placeholder)
                .error(R.drawable.ic_lego_placeholder)
                .into(holder.ivSet);

        holder.tvExclusive.setVisibility(set.isExclusive() ? View.VISIBLE : View.GONE);

        updateFavoriteButton(holder.btnFavorite, set.isFavorite());

        holder.btnFavorite.setOnClickListener(v -> {
            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(set, holder.getAdapterPosition());
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(set, holder.getAdapterPosition());
            }
        });

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SetDetailActivity.class);
            intent.putExtra("set_num", set.getSetNum());
            intent.putExtra("name", set.getName());
            intent.putExtra("year", set.getYear());
            intent.putExtra("theme", set.getTheme());
            intent.putExtra("num_parts", set.getNumParts());
            intent.putExtra("set_img_url", set.getSetImgUrl());
            intent.putExtra("price", set.getPrice());
            intent.putExtra("rating", set.getRating());
            intent.putExtra("age_range", set.getAgeRange());
            intent.putExtra("is_exclusive", set.isExclusive());
            intent.putExtra("in_stock", set.isInStock());
            intent.putExtra("is_favorite", set.isFavorite());
            intent.putExtra("description", set.getDescription());
            context.startActivity(intent);
        });

        holder.btnAddToBag.setOnClickListener(v -> {
            android.widget.Toast.makeText(context, "Added " + set.getName() + " to bag 🛍️", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void updateFavoriteButton(ImageButton button, boolean isFavorite) {
        if (isFavorite) {
            button.setImageResource(R.drawable.ic_favorite);
        } else {
            button.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View cardView;
        ImageView ivSet;
        TextView tvName, tvPrice, tvAge, tvParts, tvRating, tvExclusive;
        ImageButton btnFavorite, btnDelete;
        MaterialButton btnAddToBag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivSet = itemView.findViewById(R.id.ivSet);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvAge = itemView.findViewById(R.id.tvAge);
            tvParts = itemView.findViewById(R.id.tvParts);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvExclusive = itemView.findViewById(R.id.tvExclusive);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnAddToBag = itemView.findViewById(R.id.btnAddToBag);
        }
    }

    private static final DiffUtil.ItemCallback<LegoSet> DIFF_CALLBACK = new DiffUtil.ItemCallback<LegoSet>() {
        @Override
        public boolean areItemsTheSame(@NonNull LegoSet oldItem, @NonNull LegoSet newItem) {
            return oldItem.getSetNum().equals(newItem.getSetNum());
        }

        @Override
        public boolean areContentsTheSame(@NonNull LegoSet oldItem, @NonNull LegoSet newItem) {
            // ✨ ИСПРАВЛЕНО: Используем полное имя класса Objects
            return oldItem.isFavorite() == newItem.isFavorite()
                && java.util.Objects.equals(oldItem.getName(), newItem.getName())
                && oldItem.getPrice() == newItem.getPrice();
        }
    };
}
