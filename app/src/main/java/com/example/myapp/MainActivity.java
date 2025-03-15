// app/src/main/java/com/example/myapp/MainActivity.java
package com.example.myapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FoodItemAdapter adapter;
    private List<FoodItem> foodItems;
    private DatabaseHelper dbHelper;
    private ImageCapture imageCapture;
    private Bitmap capturedImage;
    private String currentImagePath;
    private FoodItem currentEditItem;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        foodItems = dbHelper.getAllFoodItems();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new FoodItemAdapter(foodItems);
        recyclerView.setAdapter(adapter);

        EditText searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFoodItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        Spinner sortingMenu = findViewById(R.id.sorting_menu);
        ArrayAdapter<String> sortingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"A-Z", "Z-A", "Expiry date ↓", "Expiry date ↑"});
        sortingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortingMenu.setAdapter(sortingAdapter);
        sortingMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortFoodItems(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterFoodItems(String query) {
        List<FoodItem> filteredList = dbHelper.searchFoodItems(query);
        adapter.updateList(filteredList);
    }

    private void sortFoodItems(int option) {
        switch (option) {
            case 0: Collections.sort(foodItems, (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle())); break;
            case 1: Collections.sort(foodItems, (a, b) -> b.getTitle().compareToIgnoreCase(a.getTitle())); break;
            case 2: Collections.sort(foodItems, (a, b) -> a.getExpiryDate().compareTo(b.getExpiryDate())); break;
            case 3: Collections.sort(foodItems, (a, b) -> b.getExpiryDate().compareTo(a.getExpiryDate())); break;
        }
        adapter.notifyDataSetChanged();
    }
}
