// app/src/main/java/com/example/myapp/FoodItemAdapter.java
package com.example.myapp;

import android.content.Context;
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
    private Context context;
    private DatabaseHelper dbHelper;
    
    public FoodItemAdapter(Context context, List<FoodItem> items) {
        this.context = context;
        this.items = items;
        this.dbHelper = new DatabaseHelper(context);
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
        
        if (item.getImage() != null) {
            holder.imageView.setImageBitmap(item.getImage());
        } else {
            holder.imageView.setImageResource(R.drawable.ic_food_placeholder);
        }
        
        holder.itemView.setOnLongClickListener(v -> {
            DialogManager.showItemOptionsDialog(context, item, holder.getAdapterPosition(), items, this, dbHelper);
            return true;
        });
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView dateTextView;
        
        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            titleTextView = itemView.findViewById(R.id.item_title);
            dateTextView = itemView.findViewById(R.id.item_date);
        }
    }
}
