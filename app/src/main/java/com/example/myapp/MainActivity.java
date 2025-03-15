// app/src/main/java/com/example/myapp/MainActivity.java
package com.example.myapp;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FoodItemAdapter adapter;
    private List<FoodItem> foodItems;
    private List<FoodItem> filteredItems;
    private DatabaseHelper dbHelper;
    private SearchView searchView;
    
    // Components
    private SearchManager searchManager;
    private SortManager sortManager;
    private CameraManager cameraManager;
    private DialogManager dialogManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        
        // Initialize managers
        searchManager = new SearchManager(this);
        sortManager = new SortManager(this);
        cameraManager = new CameraManager(this);
        dialogManager = new DialogManager(this, cameraManager);
        
        // Setup UI components
        setupRecyclerView();
        setupSearchView();
        setupSortButton();
        setupFab();
        
        // Load initial data
        loadFoodItems();
    }
    
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        
        filteredItems = new ArrayList<>();
        adapter = new FoodItemAdapter(filteredItems);
        recyclerView.setAdapter(adapter);
    }
    
    private void setupSearchView() {
        searchView = findViewById(R.id.search_view);
        searchManager.setupSearchView(searchView);
        
        // Add clear button listener
        ImageView clearButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (clearButton != null) {
            clearButton.setOnClickListener(v -> {
                searchView.setQuery("", false);
                searchView.clearFocus();
                searchManager.filterItems("");
            });
        }
    }
    
    private void setupSortButton() {
        findViewById(R.id.btn_sort).setOnClickListener(v -> sortManager.showSortingOptions());
    }
    
    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> dialogManager.showAddOptionsDialog());
    }
    
    private void loadFoodItems() {
        foodItems = dbHelper.getAllFoodItems();
        filteredItems.clear();
        filteredItems.addAll(foodItems);
        adapter.notifyDataSetChanged();
    }
    
    // Getters for the managers
    public List<FoodItem> getFoodItems() {
        return foodItems;
    }
    
    public List<FoodItem> getFilteredItems() {
        return filteredItems;
    }
    
    public FoodItemAdapter getAdapter() {
        return adapter;
    }
    
    public DatabaseHelper getDbHelper() {
        return dbHelper;
    }
    
    public SearchView getSearchView() {
        return searchView;
    }
}
