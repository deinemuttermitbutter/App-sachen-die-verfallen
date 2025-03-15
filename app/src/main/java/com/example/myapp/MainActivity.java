// app/src/main/java/com/example/myapp/MainActivity.java
package com.example.myapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FoodItemAdapter adapter;
    private List<FoodItem> foodItems;
    private DatabaseHelper dbHelper;
    private ImageCapture imageCapture;
    private Bitmap capturedImage;
    private String currentCameraMode;
    private String currentImagePath;
    private FoodItem currentEditItem;
    private boolean isEditMode = false;

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
        
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        
        // Load food items from database
        foodItems = dbHelper.getAllFoodItems();
        
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
                        // Convert ImageProxy to Bitmap
                        capturedImage = ImageUtils.imageProxyToBitmap(image);
                        
                        // Save bitmap to file
                        currentImagePath = ImageUtils.saveBitmapToFile(MainActivity.this, capturedImage);
                        
                        image.close();
                        dialog.dismiss();
                        
                        if (mode.equals("barcode")) {
                            // In a real app, process barcode here
                            showAddItemDialog("Scanned Item", capturedImage);
                        } else {
                            // For custom mode, leave title blank
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
        Button retakeButton = view.findViewById(R.id.retake_button);
        
        // Set minimum date to today
        Calendar today = Calendar.getInstance();
        datePicker.setMinDate(today.getTimeInMillis());

        // Show image if available
        if (image != null) {
            imageView.setImageBitmap(image);
        }
        
        // If edit mode, fill in existing data
        if (isEditMode && currentEditItem != null) {
            titleEdit.setText(currentEditItem.getTitle());
            
            // Parse expiry date
            String[] dateParts = currentEditItem.getExpiryDate().split("-");
            if (dateParts.length == 3) {
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]) - 1; // DatePicker months are 0-based
                int day = Integer.parseInt(dateParts[2]);
                datePicker.updateDate(year, month, day);
            }
            
            // Load image if available
            if (currentEditItem.getImage() != null) {
                imageView.setImageBitmap(currentEditItem.getImage());
            }
        } else {
            titleEdit.setText(title);
        }
        
        // Retake button listener
        retakeButton.setOnClickListener(v -> {
            isEditMode = false; // Ensure we're not in edit mode for capture
            if (checkCameraPermission()) {
                openCamera(currentCameraMode);
            }
        });
        
        AlertDialog dialog = builder.setView(view)
               .setPositiveButton("Save", null) // Set later to prevent auto-dismiss
               .setNegativeButton("Cancel", (dialogInterface, which) -> {
                   if (!isEditMode && currentImagePath != null) {
                       // Delete the captured image if canceling a new item
                       ImageUtils.deleteImage(currentImagePath);
                       currentImagePath = null;
                   }
                   dialogInterface.dismiss();
               })
               .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String itemTitle = titleEdit.getText().toString().trim();
                
                // Validate title
                if (itemTitle.isEmpty()) {
                    titleEdit.setError("Title is required");
                    return;
                }
                
                Calendar calendar = Calendar.getInstance();
                calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                String expiryDate = dateFormat.format(calendar.getTime());
                
                if (isEditMode && currentEditItem != null) {
                    // Update existing item
                    if (currentImagePath != null && !currentImagePath.equals(currentEditItem.getImagePath())) {
                        // Delete old image if a new one was captured
                        ImageUtils.deleteImage(currentEditItem.getImagePath());
                        currentEditItem.setImagePath(currentImagePath);
                    }
                    
                    currentEditItem.setTitle(itemTitle);
                    currentEditItem.setExpiryDate(expiryDate);
                    
                    // Update in database
                    dbHelper.updateFoodItem(currentEditItem);
                    
                    // Reset flags
                    isEditMode = false;
                    currentEditItem = null;
                } else {
                    // Create new item
                    FoodItem newItem = new FoodItem(itemTitle, expiryDate, currentImagePath);
                    long id = dbHelper.insertFoodItem(itemTitle, expiryDate, currentImagePath);
                    newItem.setId((int) id);
                    
                    // Add to list
                    foodItems.add(newItem);
                }
                
                // Refresh adapter
                adapter.notifyDataSetChanged();
                
                // Reset image path
                currentImagePath = null;
                
                dialog.dismiss();
            });
            
            // Request focus on title field and show keyboard
            titleEdit.requestFocus();
            titleEdit.postDelayed(() -> {
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(titleEdit, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }, 300);
        });
        
        dialog.show();
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
    
    // Show item options dialog for long-press
    private void showItemOptionsDialog(FoodItem item, int position) {
        String[] options = {"Edit", "Delete"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.getTitle())
               .setItems(options, (dialog, which) -> {
                   if (which == 0) {
                       // Edit option
                       currentEditItem = item;
                       isEditMode = true;
                       currentImagePath = item.getImagePath();
                       showAddItemDialog(item.getTitle(), item.getImage());
                   } else if (which == 1) {
                       // Delete option
                       confirmDelete(item, position);
                   }
               })
               .show();
    }
    
    // Confirm delete dialog
    private void confirmDelete(FoodItem item, int position) {
        new AlertDialog.Builder(this)
            .setTitle("Delete " + item.getTitle())
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Delete", (dialog, which) -> {
                // Delete from database
                dbHelper.deleteFoodItem(item.getId());
                
                // Delete image file
                ImageUtils.deleteImage(item.getImagePath());
                
                // Remove from list
                foodItems.remove(position);
                
                // Notify adapter
                adapter.notifyItemRemoved(position);
            })
            .setNegativeButton("Cancel", null)
            .show();
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
            holder.titleTextView.setText(item.getTitle());
            holder.dateTextView.setText("Expires: " + item.getExpiryDate());
            
            if (item.getImage() != null) {
                holder.imageView.setImageBitmap(item.getImage());
            } else {
                holder.imageView.setImageResource(R.drawable.ic_food_placeholder);
            }
            
            holder.itemView.setOnLongClickListener(v -> {
                showItemOptionsDialog(item, holder.getAdapterPosition());
                return true;
            });
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
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
}
