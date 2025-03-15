// app/src/main/java/com/example/myapp/MainActivity.java
package com.example.myapp;

import android.Manifest;
import android.app.Dialog;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
        ArrayAdapter<String> sortingAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, 
            new String[]{"A-Z", "Z-A", "Expiry date ↓", "Expiry date ↑"});
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
        
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddOptionsDialog();
            }
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
    
    private void showAddOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_options, null);
        
        view.findViewById(R.id.option_camera).setOnClickListener(v -> {
            AlertDialog dialog = (AlertDialog) v.getTag();
            if (dialog != null) {
                dialog.dismiss();
            }
            requestCameraPermission();
        });
        
        view.findViewById(R.id.option_search).setOnClickListener(v -> {
            AlertDialog dialog = (AlertDialog) v.getTag();
            if (dialog != null) {
                dialog.dismiss();
            }
            showSearchDialog();
        });
        
        view.findViewById(R.id.option_custom).setOnClickListener(v -> {
            AlertDialog dialog = (AlertDialog) v.getTag();
            if (dialog != null) {
                dialog.dismiss();
            }
            showAddItemDialog(null);
        });
        
        builder.setView(view);
        AlertDialog dialog = builder.create();
        
        // Set the dialog as a tag for each option to dismiss it when clicked
        view.findViewById(R.id.option_camera).setTag(dialog);
        view.findViewById(R.id.option_search).setTag(dialog);
        view.findViewById(R.id.option_custom).setTag(dialog);
        
        dialog.show();
    }
    
    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_search, null);
        
        EditText searchEditText = view.findViewById(R.id.search_edit_text);
        
        builder.setView(view)
               .setPositiveButton("Search", (dialog, which) -> {
                   String query = searchEditText.getText().toString().trim();
                   if (!query.isEmpty()) {
                       filterFoodItems(query);
                   }
               })
               .setNegativeButton("Cancel", null);
        
        builder.create().show();
    }
    
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            showCameraDialog();
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                showCameraDialog();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        });

    private void showCameraDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_camera, null);
        
        Button captureButton = view.findViewById(R.id.capture_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here we would implement camera capture logic
                // For now, just simulate capturing and proceed to add item dialog
                showAddItemDialog(null);
                // Close the camera dialog
                AlertDialog dialog = (AlertDialog) v.getTag();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        
        builder.setView(view);
        AlertDialog dialog = builder.create();
        captureButton.setTag(dialog);
        dialog.show();
    }

    private void showAddItemDialog(FoodItem itemToEdit) {
        isEditMode = itemToEdit != null;
        currentEditItem = itemToEdit;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        
        EditText titleEdit = view.findViewById(R.id.edit_title);
        DatePicker datePicker = view.findViewById(R.id.date_picker);
        ImageView imageView = view.findViewById(R.id.image_view);
        Button retakeButton = view.findViewById(R.id.retake_button);
        
        // Set current date as default
        Calendar calendar = Calendar.getInstance();
        
        if (isEditMode) {
            // Populate fields with existing item data
            titleEdit.setText(currentEditItem.getTitle());
            
            // Parse existing expiry date
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                calendar.setTime(dateFormat.parse(currentEditItem.getExpiryDate()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Load image if available
            Bitmap image = currentEditItem.getImage();
            if (image != null) {
                imageView.setImageBitmap(image);
            }
            
            currentImagePath = currentEditItem.getImagePath();
        } else if (capturedImage != null) {
            // Use recently captured image
            imageView.setImageBitmap(capturedImage);
        }
        
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 
                calendar.get(Calendar.DAY_OF_MONTH), null);
        
        retakeButton.setOnClickListener(v -> {
            // Go back to camera dialog
            requestCameraPermission();
            AlertDialog dialog = (AlertDialog) v.getTag();
            if (dialog != null) {
                dialog.dismiss();
            }
        });
        
        builder.setView(view)
               .setPositiveButton("Save", (dialog, which) -> {
                   String title = titleEdit.getText().toString().trim();
                   if (title.isEmpty()) {
                       Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
                       return;
                   }
                   
                   // Format date
                   Calendar selectedDate = Calendar.getInstance();
                   selectedDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                   SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                   String expiryDate = dateFormat.format(selectedDate.getTime());
                   
                   // Save image if needed
                   if (capturedImage != null && currentImagePath == null) {
                       currentImagePath = ImageUtils.saveBitmapToFile(this, capturedImage);
                   }
                   
                   if (isEditMode) {
                       // Update existing item
                       currentEditItem.setTitle(title);
                       currentEditItem.setExpiryDate(expiryDate);
                       if (currentImagePath != null) {
                           currentEditItem.setImagePath(currentImagePath);
                       }
                       dbHelper.updateFoodItem(currentEditItem);
                   } else {
                       // Create new item
                       FoodItem newItem = new FoodItem(title, expiryDate, currentImagePath);
                       long id = dbHelper.insertFoodItem(title, expiryDate, currentImagePath);
                       newItem.setId((int) id);
                       foodItems.add(newItem);
                   }
                   
                   // Reset captured image
                   capturedImage = null;
                   currentImagePath = null;
                   
                   // Refresh list
                   adapter.notifyDataSetChanged();
               })
               .setNegativeButton("Cancel", (dialog, which) -> {
                   // Reset captured image if canceled
                   capturedImage = null;
               });
        
        if (isEditMode) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                new AlertDialog.Builder(this)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        // Delete image file
                        if (currentEditItem.getImagePath() != null) {
                            ImageUtils.deleteImage(currentEditItem.getImagePath());
                        }
                        
                        // Delete from database
                        dbHelper.deleteFoodItem(currentEditItem.getId());
                        
                        // Remove from list
                        foodItems.remove(currentEditItem);
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("No", null)
                    .show();
            });
        }
        
        AlertDialog dialog = builder.create();
        retakeButton.setTag(dialog);
        dialog.show();
    }
}
