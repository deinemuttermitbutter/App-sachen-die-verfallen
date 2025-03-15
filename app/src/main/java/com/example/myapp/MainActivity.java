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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FoodItemAdapter adapter;
    private List<FoodItem> foodItems;
    private List<FoodItem> filteredItems;
    private DatabaseHelper dbHelper;
    private ImageCapture imageCapture;
    private Bitmap capturedImage;
    private String currentCameraMode;
    private String currentImagePath;
    private FoodItem currentEditItem;
    private boolean isEditMode = false;
    private AlertDialog currentDialog;
    private AlertDialog cameraDialog;
    private SearchView searchView;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted, proceed with camera
                    openCamera(currentCameraMode);
                } else {
                    Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        
        // Initialize search view
        searchView = findViewById(R.id.search_view);
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
        
        // Add clear button listener
        ImageView clearButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (clearButton != null) {
            clearButton.setOnClickListener(v -> {
                searchView.setQuery("", false);
                searchView.clearFocus();
                filterItems("");
            });
        }
        
        // Setup sort button
        findViewById(R.id.btn_sort).setOnClickListener(v -> showSortingOptions());
        
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        
        // Load food items from database
        foodItems = dbHelper.getAllFoodItems();
        filteredItems = new ArrayList<>(foodItems);
        
        // Initialize adapter
        adapter = new FoodItemAdapter(filteredItems);
        recyclerView.setAdapter(adapter);

        // Initialize FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showAddOptionsDialog());
    }
    
    private void filterItems(String query) {
        filteredItems.clear();
        
        if (query.isEmpty()) {
            filteredItems.addAll(foodItems);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (FoodItem item : foodItems) {
                if (item.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    filteredItems.add(item);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
    }
    
    private void showSortingOptions() {
        PopupMenu popup = new PopupMenu(this, findViewById(R.id.btn_sort));
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
        Collections.sort(foodItems, (o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
        filterItems(searchView.getQuery().toString());
    }
    
    private void sortItemsZA() {
        Collections.sort(foodItems, (o1, o2) -> o2.getTitle().compareToIgnoreCase(o1.getTitle()));
        filterItems(searchView.getQuery().toString());
    }
    
    private void sortItemsByExpiryAsc() {
        Collections.sort(foodItems, (o1, o2) -> o1.getExpiryDate().compareTo(o2.getExpiryDate()));
        filterItems(searchView.getQuery().toString());
    }
    
    private void sortItemsByExpiryDesc() {
        Collections.sort(foodItems, (o1, o2) -> o2.getExpiryDate().compareTo(o1.getExpiryDate()));
        filterItems(searchView.getQuery().toString());
    }

    private void showAddOptionsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_options, null);
        
        view.findViewById(R.id.option_camera).setOnClickListener(v -> {
            dialog.dismiss();
            // Set current camera mode before checking permission
            currentCameraMode = "barcode";
            isEditMode = false;
            if (checkCameraPermission()) {
                openCamera(currentCameraMode);
            }
        });
        
        view.findViewById(R.id.option_search).setOnClickListener(v -> {
            dialog.dismiss();
            showSearchDialog();
        });
        
        view.findViewById(R.id.option_custom).setOnClickListener(v -> {
            dialog.dismiss();
            // Set current camera mode before checking permission
            currentCameraMode = "custom";
            isEditMode = false;
            if (checkCameraPermission()) {
                openCamera(currentCameraMode);
            }
        });
        
        dialog.setContentView(view);
        dialog.show();
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            // Always request permission when not granted
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            return false;
        }
        return true;
    }

    private void openCamera(String mode) {
        // Close existing camera dialog if any
        if (cameraDialog != null && cameraDialog.isShowing()) {
            cameraDialog.dismiss();
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View cameraView = getLayoutInflater().inflate(R.layout.dialog_camera, null);
        PreviewView previewView = cameraView.findViewById(R.id.preview_view);
        
        builder.setView(cameraView);
        cameraDialog = builder.create();
        
        // Set up camera
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
                ProcessCameraProvider.getInstance(this);
                
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
        
        cameraView.findViewById(R.id.capture_button).setOnClickListener(v -> {
            captureImage(cameraDialog, mode);
        });
        
        cameraDialog.show();
    }
    
    private void captureImage(AlertDialog dialog, String mode) {
        if (imageCapture == null) return;
        
        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        // Convert ImageProxy to Bitmap (with rotation fix in ImageUtils)
                        capturedImage = ImageUtils.imageProxyToBitmap(image);
                        
                        // Save bitmap to file
                        currentImagePath = ImageUtils.saveBitmapToFile(MainActivity.this, capturedImage);
                        
                        image.close();
                        dialog.dismiss();
                        
                        if (currentDialog != null && currentDialog.isShowing()) {
                            // Update existing dialog with new image
                            ImageView imageView = currentDialog.findViewById(R.id.image_view);
                            if (imageView != null) {
                                imageView.setImageBitmap(capturedImage);
                            }
                        } else {
                            // Create new dialog
                            if (mode.equals("barcode")) {
                                // In a real app, process barcode here
                                showAddItemDialog("Scanned Item", capturedImage);
                            } else {
                                // For custom mode, leave title blank
                                showAddItemDialog("", capturedImage);
                            }
                        }
                    }
                    
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(MainActivity.this, "Error capturing image", 
                                Toast.LENGTH_SHORT).
