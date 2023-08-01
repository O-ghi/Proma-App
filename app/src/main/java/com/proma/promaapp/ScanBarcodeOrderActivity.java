package com.proma.promaapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.proma.promaapp.Model.CartList;
import com.proma.promaapp.Model.Product;
import com.proma.promaapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class ScanBarcodeOrderActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private CaptureManager captureManager;
    private Button btnCart;
    private Button btnProductList;
    private boolean isScanning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanbarcode_order);

        barcodeView = findViewById(R.id.scanner_view);
        btnCart = findViewById(R.id.btnCart);
        btnProductList = findViewById(R.id.btnProductList);

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

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to CartListActivity
                Intent intent = new Intent(ScanBarcodeOrderActivity.this, CartListActivity.class);
                intent.putExtra("storeId", getIntent().getStringExtra("storeId"));
                startActivity(intent);
            }
        });

//        btnProductList.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Switch to OrderProductActivity
//                Intent intent = new Intent(ScanBarcodeOrderActivity.this, OrderProductActivity.class);
//                intent.putExtra("storeId", getIntent().getStringExtra("storeId"));
//                startActivity(intent);
//                finish();
//            }
//        });
        btnProductList.setOnClickListener(new View.OnClickListener() {
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
        Log.d("handleDecode", "check " + scannedCode);

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
                        // ID exists, retrieve the product data and add it to the cart list
                        Log.d("Scanner", "onComplete: ID exists");
                        Product product = document.toObject(Product.class);
                        product.setId(scannedCode);
                        addToCartList(product);
                    } else {
                        // ID doesn't exist, show error message
                        Log.d("Scanner", "onComplete: ID doesn't exist");
                        showErrorMessage("Invalid barcode");
                    }
                } else {
                    Log.d("Scanner", "Error getting document: " + task.getException());
                    // Handle the error
                    showErrorMessage("Error retrieving product");
                }
            }
        });
    }


    private void addToCartList(Product product) {
        if (product.getQuantity() > 0) {
            // Product is available, add it to the cart list
            int existingIndex = CartList.getCartItemIndex(product);

            if (existingIndex != -1) {
                // Product already exists in the cart list, update the quantity
                Product existingProduct = CartList.getCartItem(existingIndex);
                existingProduct.setQuantity(existingProduct.getQuantity() + 1); // Add one quantity
            } else {
                // Product doesn't exist in the cart list, add it
                product.setQuantity(1); // Set the quantity to one
                CartList.addToCart(product);
            }

            // Show success message
            showSuccessMessage("Đã thêm vào giỏ hàng");
        } else {
            // Product is out of stock, show a popup message
            showOutOfStockPopup(product);
        }
    }
        private void showOutOfStockPopup(Product product) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Hết Hàng");
            builder.setMessage(product.getName() + " không còn mặt hàng trong kho");
            builder.setPositiveButton("Đồng ý", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    private void showErrorMessage(String message) {
        // Display an error message to the user (e.g., using a dialog, toast, or UI component)
        // For this example, let's use a toast to show the error message
        Toast.makeText(ScanBarcodeOrderActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccessMessage(String message) {
        // Display a success message to the user (e.g., using a dialog, toast, or UI component)
        // For this example, let's use a toast to show the success message
        Toast.makeText(ScanBarcodeOrderActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
