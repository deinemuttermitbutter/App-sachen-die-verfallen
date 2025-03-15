// app/src/main/java/com/example/myapp/SortManager.java
package com.example.myapp;

import android.view.View;
import android.widget.PopupMenu;

import java.util.Collections;

public class SortManager {
    private MainActivity activity;
    
    public SortManager(MainActivity activity) {
        this.activity = activity;
    }
    
    public void showSortingOptions() {
        PopupMenu popup = new PopupMenu(activity, activity.findViewById(R.id.btn_sort));
        popup.getMenuInflater().inflate(R.menu.menu_sorting, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.sort_az) {
                sortItemsAZ();
                return true;
            } else if (itemId == R.id.sort_za) {
                sortItemsZA();
                return true;
            } else if (itemId == R.id.sort_expiry_asc) {
                sortItemsByExpiryAsc();
                return true;
            } else if (itemId == R.id.sort_expiry_desc) {
                sortItemsByExpiryDesc();
                return true;
            }
            return false;
        });
        
        popup.show();
    }
    
    private void sortItemsAZ() {
        Collections.sort(activity.getFoodItems(), (o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
        applyFiltering();
    }
    
    private void sortItemsZA() {
        Collections.sort(activity.getFoodItems(), (o1, o2) -> o2.getTitle().compareToIgnoreCase(o1.getTitle()));
        applyFiltering();
    }
    
    private void sortItemsByExpiryAsc() {
        Collections.sort(activity.getFoodItems(), (o1, o2) -> o1.getExpiryDate().compareTo(o2.getExpiryDate()));
        applyFiltering();
    }
    
    private void sortItemsByExpiryDesc() {
        Collections.sort(activity.getFoodItems(), (o1, o2) -> o2.getExpiryDate().compareTo(o1.getExpiryDate()));
        applyFiltering();
    }
    
    private void applyFiltering() {
        // Reapply current filter after sorting
        String currentQuery = activity.getSearchView().getQuery().toString();
        new SearchManager(activity).filterItems(currentQuery);
    }
}
