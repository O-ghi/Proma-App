package com.example.promaapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeResult;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class ScanBarcodeActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private CaptureManager captureManager;
    private Button scanButton;
    private boolean isScanning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanbarcode);

        barcodeView = findViewById(R.id.scanner_view);
        scanButton = findViewById(R.id.scan_button);

        // Create an instance of the CaptureManager to handle scanning
        captureManager = new CaptureManager(this, barcodeView);
        captureManager.initializeFromIntent(getIntent(), savedInstanceState);
        captureManager.decode();

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                handleDecode(result);
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // No need to implement this method for now
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barcodeView.decodeSingle(new BarcodeCallback() {
                    @Override
                    public void barcodeResult(BarcodeResult result) {
                        isScanning = true;

                        handleDecode(result);
                    }

                    @Override
                    public void possibleResultPoints(List<ResultPoint> resultPoints) {
                        // No need to implement this method for now
                    }
                });
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (captureManager != null) {
            captureManager.onResume();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        captureManager.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        captureManager.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        captureManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        captureManager.onSaveInstanceState(outState);
    }

    public void handleDecode(BarcodeResult result) {
        if (!isScanning) {
            // Scanning is stopped, return early
            return;
        }

        final String scannedCode = result.getText();

        checkIfIdExists(scannedCode);

        // Stop scanning after finding a barcode
        isScanning = false;

    }

    private void checkIfIdExists(final String scannedCode) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference productsRef = db.collection("products");

        DocumentReference docRef = productsRef.document(scannedCode);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // ID exists, navigate to the product detail page
                        Log.d("Scanner", "onComplete: ID exists");
                        Intent intent = new Intent(ScanBarcodeActivity.this, ProductDetailActivity.class);
                        intent.putExtra("productId", scannedCode);
                        startActivity(intent);
                        finish(); // Optional: Finish the scanner activity after navigating to the product detail page
                    } else {
                        // ID doesn't exist, navigate to the add product page
                        Log.d("Scanner", "onComplete: ID doesn't exist");
                        Intent intent = new Intent(ScanBarcodeActivity.this, AddProductActivity.class);
                        intent.putExtra("productId", scannedCode);
                        startActivity(intent);
                        finish(); // Optional: Finish the scanner activity after navigating to the add product page
                    }
                } else {
                    Log.d("Scanner", "Error getting document: " + task.getException());
                    // Handle the error
                }
            }
        });
    }


}
