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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.legokp.R;
import com.example.legokp.models.LegoSet;
import com.example.legokp.ui.SetDetailActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LegoSetAdapter extends RecyclerView.Adapter<LegoSetAdapter.ViewHolder> {

    private List<LegoSet> legoSets;
    private Context context;
    private OnFavoriteClickListener favoriteClickListener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(LegoSet legoSet, int position);
    }

    public LegoSetAdapter(Context context, OnFavoriteClickListener listener) {
        this.context = context;
        this.legoSets = new ArrayList<>();
        this.favoriteClickListener = listener;
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
        LegoSet set = legoSets.get(position);

        // Название
        holder.tvName.setText(set.getName());

        // Цена
        holder.tvPrice.setText(String.format(Locale.US, "$%.2f", set.getPrice()));

        // Возраст
        holder.tvAge.setText(set.getAgeRange());

        // Количество деталей
        holder.tvParts.setText(String.valueOf(set.getNumParts()));

        // Рейтинг
        holder.tvRating.setText(String.format(Locale.US, "%.1f", set.getRating()));

        // Изображение
        Glide.with(context)
                .load(set.getSetImgUrl())
                .placeholder(R.drawable.ic_lego_placeholder)
                .error(R.drawable.ic_lego_placeholder)
                .into(holder.ivSet);

        // Badge "Exclusive"
        if (set.isExclusive()) {
            holder.tvExclusive.setVisibility(View.VISIBLE);
        } else {
            holder.tvExclusive.setVisibility(View.GONE);
        }

        // Favorite button - обновляем иконку в зависимости от состояния
        updateFavoriteButton(holder.btnFavorite, set.isFavorite());

        // Click на Favorite
        holder.btnFavorite.setOnClickListener(v -> {
            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(set, holder.getAdapterPosition());
            }
        });

        // Click на карточку - открыть детали
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SetDetailActivity.class);
            intent.putExtra("set_num", set.getSetNum());
            intent.putExtra("name", set.getName());
            intent.putExtra("price", set.getPrice());
            intent.putExtra("image_url", set.getSetImgUrl());
            intent.putExtra("rating", set.getRating());
            intent.putExtra("age_range", set.getAgeRange());
            intent.putExtra("num_parts", set.getNumParts());
            intent.putExtra("theme", set.getTheme());
            intent.putExtra("year", set.getYear());
            intent.putExtra("description", set.getDescription());
            intent.putExtra("is_favorite", set.isFavorite());
            context.startActivity(intent);
        });

        // Click на "Add to Bag"
        holder.btnAddToBag.setOnClickListener(v -> {
            // TODO: Добавить в корзину
            android.widget.Toast.makeText(context,
                    "Added " + set.getName() + " to bag 🛍️",
                    android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return legoSets != null ? legoSets.size() : 0;
    }

    public void updateSets(List<LegoSet> newSets) {
        if (newSets != null) {
            this.legoSets = new ArrayList<>(newSets);
            notifyDataSetChanged();
        }
    }

    public void updateFavoriteStatus(int position, boolean isFavorite) {
        if (position >= 0 && position < legoSets.size()) {
            legoSets.get(position).setFavorite(isFavorite);
            notifyItemChanged(position);
        }
    }

    private void updateFavoriteButton(ImageButton button, boolean isFavorite) {
        if (isFavorite) {
            button.setImageResource(R.drawable.ic_favorite);
            button.setContentDescription("Remove from favorites");
        } else {
            button.setImageResource(R.drawable.ic_favorite_border);
            button.setContentDescription("Add to favorites");
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivSet;
        TextView tvName, tvPrice, tvAge, tvParts, tvRating, tvExclusive;
        ImageButton btnFavorite;
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
            btnAddToBag = itemView.findViewById(R.id.btnAddToBag);
        }
    }
}