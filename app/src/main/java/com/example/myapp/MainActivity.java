package com.example.myapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FoodItemAdapter adapter;
    private List<FoodItem> foodItems;
    private ImageCapture imageCapture;
    private Bitmap capturedImage;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted, proceed with camera
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        
        // Initialize food items list
        foodItems = new ArrayList<>();
        
        // Add sample items for demonstration
        foodItems.add(new FoodItem("Milk", "2025-03-20", null));
        foodItems.add(new FoodItem("Bread", "2025-03-18", null));
        
        // Initialize adapter
        adapter = new FoodItemAdapter(foodItems);
        recyclerView.setAdapter(adapter);

        // Initialize FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showAddOptionsDialog());
    }

    private void showAddOptionsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_options, null);
        
        view.findViewById(R.id.option_camera).setOnClickListener(v -> {
            dialog.dismiss();
            // Handle barcode scanning (just open camera for now)
            if (checkCameraPermission()) {
                openCamera("barcode");
            }
        });
        
        view.findViewById(R.id.option_search).setOnClickListener(v -> {
            dialog.dismiss();
            showSearchDialog();
        });
        
        view.findViewById(R.id.option_custom).setOnClickListener(v -> {
            dialog.dismiss();
            if (checkCameraPermission()) {
                openCamera("custom");
            }
        });
        
        dialog.setContentView(view);
        dialog.show();
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            return false;
        }
        return true;
    }

    private void openCamera(String mode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View cameraView = getLayoutInflater().inflate(R.layout.dialog_camera, null);
        PreviewView previewView = cameraView.findViewById(R.id.preview_view);
        
        builder.setView(cameraView);
        AlertDialog dialog = builder.create();
        
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
            captureImage(dialog, mode);
        });
        
        dialog.show();
    }
    
    private void captureImage(AlertDialog dialog, String mode) {
        if (imageCapture == null) return;
        
        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        // Convert ImageProxy to Bitmap (simplified)
                        capturedImage = null; // In a real app, convert ImageProxy to Bitmap
                        image.close();
                        dialog.dismiss();
                        
                        if (mode.equals("barcode")) {
                            // In a real app, process barcode here
                            showAddItemDialog("Scanned Item", capturedImage);
                        } else {
                            showAddItemDialog("", capturedImage);
                        }
                    }
                    
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(MainActivity.this, "Error capturing image", 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void showAddItemDialog(String title, Bitmap image) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        
        EditText titleEdit = view.findViewById(R.id.edit_title);
        DatePicker datePicker = view.findViewById(R.id.date_picker);
        ImageView imageView = view.findViewById(R.id.image_view);
        
        titleEdit.setText(title);
        if (image != null) {
            imageView.setImageBitmap(image);
        }
        
        builder.setView(view)
               .setPositiveButton("Add", (dialog, which) -> {
                   String itemTitle = titleEdit.getText().toString();
                   Calendar calendar = Calendar.getInstance();
                   calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                   SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                   String expiryDate = dateFormat.format(calendar.getTime());
                   
                   foodItems.add(new FoodItem(itemTitle, expiryDate, image));
                   adapter.notifyItemInserted(foodItems.size() - 1);
               })
               .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        
        builder.create().show();
    }
    
    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_search, null);
        
        builder.setView(view)
               .setPositiveButton("Search", (dialog, which) -> {
                   // Search functionality to be implemented later
                   Toast.makeText(this, "Search not implemented yet", Toast.LENGTH_SHORT).show();
               })
               .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        
        builder.create().show();
    }
    
    // Food Item class
    private static class FoodItem {
        String title;
        String expiryDate;
        Bitmap image;
        
        FoodItem(String title, String expiryDate, Bitmap image) {
            this.title = title;
            this.expiryDate = expiryDate;
            this.image = image;
        }
    }
    
    // RecyclerView Adapter
    private class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.ViewHolder> {
        private List<FoodItem> items;
        
        FoodItemAdapter(List<FoodItem> items) {
            this.items = items;
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
            holder.titleText.setText(item.title);
            holder.dateText.setText(item.expiryDate);
            if (item.image != null) {
                holder.imageView.setImageBitmap(item.image);
            } else {
                holder.imageView.setImageResource(R.drawable.ic_food_placeholder);
            }
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView titleText;
            TextView dateText;
            
            ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.item_image);
                titleText = itemView.findViewById(R.id.item_title);
                dateText = itemView.findViewById(R.id.item_date);
            }
        }
    }
}
