// app/src/main/java/com/example/myapp/CameraManager.java
package com.example.myapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class CameraManager {
    private static ImageCapture imageCapture;
    private static String currentImagePath;
    private static boolean isEditMode = false;
    private static FoodItem currentEditItem;
    private static String currentCameraMode;

    private static ActivityResultLauncher<String> requestPermissionLauncher;

    public static void initPermissionLauncher(Activity activity) {
        requestPermissionLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted, proceed with camera
                    openCamera(activity, currentCameraMode);
                } else {
                    Toast.makeText(activity, "Camera permission is required to use this feature", 
                            Toast.LENGTH_SHORT).show();
                }
            });
    }

    public static boolean checkCameraPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            // Always request permission when not granted
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            return false;
        }
        return true;
    }

    public static void openCamera(Activity activity, String mode) {
        currentCameraMode = mode;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View cameraView = activity.getLayoutInflater().inflate(R.layout.dialog_camera, null);
        PreviewView previewView = cameraView.findViewById(R.id.preview_view);
        
        builder.setView(cameraView);
        AlertDialog dialog = builder.create();
        
        // Set up camera
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
                ProcessCameraProvider.getInstance(activity);
                
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
                cameraProvider.bindToLifecycle((LifecycleOwner) activity, 
                        cameraSelector, preview, imageCapture);
                
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(activity, "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(activity));
        
        cameraView.findViewById(R.id.capture_button).setOnClickListener(v -> {
            captureImage(activity, dialog, mode);
        });
        
        dialog.show();
    }
    
    private static void captureImage(Activity activity, AlertDialog dialog, String mode) {
        if (imageCapture == null) return;
        
        imageCapture.takePicture(ContextCompat.getMainExecutor(activity),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        // Convert ImageProxy to Bitmap
                        Bitmap capturedImage = ImageUtils.imageProxyToBitmap(image);
                        
                        // Save bitmap to file
                        currentImagePath = ImageUtils.saveBitmapToFile(activity, capturedImage);
                        
                        image.close();
                        dialog.dismiss();
                        
                        if (mode.equals("barcode")) {
                            // In a real app, process barcode here
                            DialogManager.showAddItemDialog(activity, "Scanned Item", capturedImage,
                                isEditMode, currentEditItem, currentImagePath, currentCameraMode);
                        } else {
                            // For custom mode, leave title blank
                            DialogManager.showAddItemDialog(activity, "", capturedImage,
                                isEditMode, currentEditItem, currentImagePath, currentCameraMode);
                        }
                    }
                    
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(activity, "Error capturing image", 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    // Getters and setters for static fields
    public static void setIsEditMode(boolean editMode) {
        isEditMode = editMode;
    }
    
    public static void setCurrentEditItem(FoodItem item) {
        currentEditItem = item;
    }
    
    public static String getCurrentImagePath() {
        return currentImagePath;
    }
    
    public static void setCurrentImagePath(String path) {
        currentImagePath = path;
    }
    
    public static void setCurrentCameraMode(String mode) {
        currentCameraMode = mode;
    }
}
