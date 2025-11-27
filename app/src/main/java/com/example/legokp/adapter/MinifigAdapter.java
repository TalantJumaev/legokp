package com.example.legokp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.legokp.R;
import com.example.legokp.models.Minifig;

import java.util.List;

public class MinifigAdapter extends RecyclerView.Adapter<MinifigAdapter.ViewHolder> {

    private List<Minifig> minifigs;

    public MinifigAdapter(List<Minifig> minifigs) {
        this.minifigs = minifigs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_minifig, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Minifig minifig = minifigs.get(position);
        holder.textViewName.setText(minifig.getName());

        // Use setImgUrl instead of imageUrl
        Glide.with(holder.itemView.getContext())
                .load(minifig.getSetImgUrl())
                .placeholder(R.drawable.ic_lego_placeholder)
                .error(R.drawable.ic_lego_placeholder)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return minifigs != null ? minifigs.size() : 0;
    }

    public void updateMinifigs(List<Minifig> newMinifigs) {
        this.minifigs = newMinifigs;
        notifyDataSetChanged();  // THIS WAS MISSING!
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewName = itemView.findViewById(R.id.textViewName);
        }
    }
}