// app/src/main/java/com/example/myapp/SearchManager.java
package com.example.myapp;

import androidx.appcompat.widget.SearchView;

public class SearchManager {
    private MainActivity activity;
    
    public SearchManager(MainActivity activity) {
        this.activity = activity;
    }
    
    public void setupSearchView(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterItems(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterItems(newText);
                return true;
            }
        });
    }
    
    public void filterItems(String query) {
        activity.getFilteredItems().clear();
        
        if (query.isEmpty()) {
            activity.getFilteredItems().addAll(activity.getFoodItems());
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (FoodItem item : activity.getFoodItems()) {
                if (item.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    activity.getFilteredItems().add(item);
                }
            }
        }
        
        activity.getAdapter().notifyDataSetChanged();
    }
}
