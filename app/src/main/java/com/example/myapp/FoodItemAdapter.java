// app/src/main/java/com/example/myapp/FoodItemAdapter.java
package com.example.myapp;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.ViewHolder> {
    private List<FoodItem> items;

    public FoodItemAdapter(List<FoodItem> items) {
        this.items = items;
    }

    public void updateList(List<FoodItem> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = items.get(position);
        holder.titleTextView.setText(item.getTitle());
        holder.dateTextView.setText("Expires: " + item.getExpiryDate());

        Bitmap image = item.getImage();
        if (image != null) {
            holder.imageView.setImageBitmap(image);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_food_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView, dateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            titleTextView = itemView.findViewById(R.id.item_title);
            dateTextView = itemView.findViewById(R.id.item_date);
        }
    }
}
