// app/src/main/java/com/example/myapp/DialogManager.java
package com.example.myapp;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;

public class DialogManager {
    private MainActivity activity;
    private CameraManager cameraManager;
    private FoodItem currentEditItem;
    private boolean isEditMode = false;
    private AlertDialog currentDialog;
    
    public DialogManager(MainActivity activity, CameraManager cameraManager) {
        this.activity = activity;
        this.cameraManager = cameraManager;
    }
    
    public void showAddOptionsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_add_options, null);
        
        view.findViewById(R.id.option_camera).setOnClickListener(v -> {
            dialog.dismiss();
            // Set current camera mode before checking permission
            isEditMode = false;
            cameraManager.openCamera("barcode", (image, imagePath) -> {
                // In a real app, process barcode here
                showAddItemDialog("Scanned Item", image);
            });
        });
        
        view.findViewById(R.id.option_search).setOnClickListener(v -> {
            dialog.dismiss();
            showSearchDialog();
        });
        
        view.findViewById(R.id.option_custom).setOnClickListener(v -> {
            dialog.dismiss();
            isEditMode = false;
            cameraManager.openCamera("custom", (image, imagePath) -> {
                // For custom mode, leave title blank
                showAddItemDialog("", image);
            });
        });
        
        dialog.setContentView(view);
        dialog.show();
    }
    
    public void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_search_food, null);
        EditText searchInput = view.findViewById(R.id.search_input);
        
        builder.setView(view)
               .setTitle("Search Food Database")
               .setPositiveButton("Search", (dialog, which) -> {
                   String query = searchInput.toString().trim();
                   if (!query.isEmpty()) {
                       // In a real app, make API call to food database
                       showAddItemDialog("Search Result: " + query, null);
                   }
               })
               .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
               
        builder.create().show();
    }
    
    public void showAddItemDialog(String title, Bitmap image) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        
        EditText titleInput = view.findViewById(R.id.title_input);
        EditText notesInput = view.findViewById(R.id.notes_input);
        DatePicker datePicker = view.findViewById(R.id.date_picker);
        ImageView imageView = view.findViewById(R.id.image_view);
        
        if (image != null) {
            imageView.setImageBitmap(image);
        }
        
        if (title != null && !title.isEmpty()) {
            titleInput.setText(title);
        }
        
        // Setup camera button
        view.findViewById(R.id.camera_button).setOnClickListener(v -> {
            cameraManager.openCamera("custom", (capturedImage, imagePath) -> {
                imageView.setImageBitmap(capturedImage);
            });
        });
        
        if (isEditMode && currentEditItem != null) {
            // Fill form with item data for editing
            titleInput.setText(currentEditItem.getTitle());
            notesInput.setText(currentEditItem.getNotes());
            
            // Set date picker to item's expiry date
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentEditItem.getExpiryDate());
            datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 
                    cal.get(Calendar.DAY_OF_MONTH), null);
            
            if (currentEditItem.getImagePath() != null) {
                Bitmap savedImage = ImageUtils.loadBitmapFromFile(currentEditItem.getImagePath());
                if (savedImage != null) {
                    imageView.setImageBitmap(savedImage);
                }
            }
        }
        
        builder.setView(view)
               .setTitle(isEditMode ? "Edit Item" : "Add New Item")
               .setPositiveButton(isEditMode ? "Update" : "Add", (dialog, which) -> {
                   String itemTitle = titleInput.getText().toString().trim();
                   String notes = notesInput.getText().toString().trim();
                   
                   if (itemTitle.isEmpty()) {
                       return;
                   }
                   
                   // Get date from DatePicker
                   Calendar calendar = Calendar.getInstance();
                   calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                   
                   if (isEditMode && currentEditItem != null) {
                       // Update existing item
                       currentEditItem.setTitle(itemTitle);
                       currentEditItem.setNotes(notes);
                       currentEditItem.setExpiryDate(calendar.getTime());
                       
                       if (cameraManager.getCurrentImagePath() != null) {
                           currentEditItem.setImagePath(cameraManager.getCurrentImagePath());
                       }
                       
                       activity.getDbHelper().updateFoodItem(currentEditItem);
                   } else {
                       // Create new food item
                       FoodItem newItem = new FoodItem();
                       newItem.setTitle(itemTitle);
                       newItem.setNotes(notes);
                       newItem.setExpiryDate(calendar.getTime());
                       newItem.setImagePath(cameraManager.getCurrentImagePath());
                       
                       long id = activity.getDbHelper().addFoodItem(newItem);
                       newItem.setId(id);
                       
                       activity.getFoodItems().add(newItem);
                   }
                   
                   // Refresh the list
                   String currentQuery = activity.getSearchView().getQuery().toString();
                   new SearchManager(activity).filterItems(currentQuery);
               })
               .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
               
        currentDialog = builder.create();
        currentDialog.show();
    }
    
    public void editItem(FoodItem item) {
        currentEditItem = item;
        isEditMode = true;
        showAddItemDialog(null, null);
    }
    
    public void confirmDeleteItem(FoodItem item) {
        new AlertDialog.Builder(activity)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete " + item.getTitle() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    activity.getDbHelper().deleteFoodItem(item.getId());
                    activity.getFoodItems().remove(item);
                    activity.getFilteredItems().remove(item);
                    activity.getAdapter().notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
