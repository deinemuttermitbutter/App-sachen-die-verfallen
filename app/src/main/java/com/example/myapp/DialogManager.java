// app/src/main/java/com/example/myapp/DialogManager.java
package com.example.myapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DialogManager {

    public static void showAddOptionsDialog(Activity activity, RecyclerView.Adapter adapter, 
                                           List<FoodItem> foodItems, DatabaseHelper dbHelper) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_add_options, null);
        
        view.findViewById(R.id.option_camera).setOnClickListener(v -> {
            dialog.dismiss();
            // Set current camera mode before checking permission
            CameraManager.setCurrentCameraMode("barcode");
            CameraManager.setIsEditMode(false);
            if (CameraManager.checkCameraPermission(activity)) {
                CameraManager.openCamera(activity, "barcode");
            }
        });
        
        view.findViewById(R.id.option_search).setOnClickListener(v -> {
            dialog.dismiss();
            showSearchDialog(activity);
        });
        
        view.findViewById(R.id.option_custom).setOnClickListener(v -> {
            dialog.dismiss();
            // Set current camera mode before checking permission
            CameraManager.setCurrentCameraMode("custom");
            CameraManager.setIsEditMode(false);
            if (CameraManager.checkCameraPermission(activity)) {
                CameraManager.openCamera(activity, "custom");
            }
        });
        
        dialog.setContentView(view);
        dialog.show();
    }

    public static void showAddItemDialog(Activity activity, String title, Bitmap image,
                                   boolean isEditMode, FoodItem currentEditItem, 
                                   String currentImagePath, String currentCameraMode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        
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
            CameraManager.setIsEditMode(false); // Ensure we're not in edit mode for capture
            if (CameraManager.checkCameraPermission(activity)) {
                CameraManager.openCamera(activity, currentCameraMode);
            }
        });

        DatabaseHelper dbHelper = new DatabaseHelper(activity);
        FoodItemAdapter adapter = null;
        List<FoodItem> foodItems = dbHelper.getAllFoodItems();
        
        if (activity instanceof MainActivity) {
            adapter = new FoodItemAdapter(activity, foodItems);
        }
        
        final FoodItemAdapter finalAdapter = adapter;
        
        AlertDialog dialog = builder.setView(view)
               .setPositiveButton("Save", null) // Set later to prevent auto-dismiss
               .setNegativeButton("Cancel", (dialogInterface, which) -> {
                   if (!isEditMode && currentImagePath != null) {
                       // Delete the captured image if canceling a new item
                       ImageUtils.deleteImage(currentImagePath);
                       CameraManager.setCurrentImagePath(null);
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
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
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
                    CameraManager.setIsEditMode(false);
                    CameraManager.setCurrentEditItem(null);
                } else {
                    // Create new item
                    FoodItem newItem = new FoodItem(itemTitle, expiryDate, currentImagePath);
                    long id = dbHelper.insertFoodItem(itemTitle, expiryDate, currentImagePath);
                    newItem.setId((int) id);
                    
                    // Add to list
                    foodItems.add(newItem);
                }
                
                // Refresh adapter
                if (finalAdapter != null) {
                    finalAdapter.notifyDataSetChanged();
                }
                
                // Reset image path
                CameraManager.setCurrentImagePath(null);
                
                dialog.dismiss();
            });
            
            // Request focus on title field and show keyboard
            titleEdit.requestFocus();
            titleEdit.postDelayed(() -> {
                android.view.inputmethod.InputMethodManager imm = 
                    (android.view.inputmethod.InputMethodManager)
