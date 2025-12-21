package com.example.legokp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.legokp.R;
import com.example.legokp.models.Theme;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ViewHolder> {

    private List<Theme> themes;
    private Context context;
    private OnThemeClickListener clickListener;
    private int selectedPosition = -1;

    public interface OnThemeClickListener {
        void onThemeClick(Theme theme, int position);
    }

    public ThemeAdapter(Context context, OnThemeClickListener listener) {
        this.context = context;
        this.themes = new ArrayList<>();
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_theme_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Theme theme = themes.get(position);

        holder.chip.setText(theme.getName());
        holder.chip.setChecked(position == selectedPosition);

        holder.chip.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = position;

            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);

            if (clickListener != null) {
                clickListener.onThemeClick(theme, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return themes != null ? themes.size() : 0;
    }

    public void updateThemes(List<Theme> newThemes) {
        this.themes = newThemes;
        notifyDataSetChanged();
    }

    public void clearSelection() {
        int previous = selectedPosition;
        selectedPosition = -1;
        if (previous != -1) {
            notifyItemChanged(previous);
        }
    }

    // ✨ НОВОЕ: Метод для получения текущей выбранной позиции
    public int getSelectedPosition() {
        return selectedPosition;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Chip chip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.chipTheme);
        }
    }
}