package com.proma.promaapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.proma.promaapp.Model.Product;
import com.proma.promaapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imageViewProduct;
    private TextView textViewProductName;
    private TextView textViewProductPrice;
    private TextView textViewProductQuantity;
    private TextView textViewProductExpiry;

    private String productId;
    private String storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail_activity);

        // Retrieve the productId and storeId from intent
        productId = getIntent().getStringExtra("productId");
        storeId = getIntent().getStringExtra("storeId");

        // Initialize views
        imageViewProduct = findViewById(R.id.imageViewProduct);
        textViewProductName = findViewById(R.id.textViewProductName);
        textViewProductPrice = findViewById(R.id.textViewProductPrice);
        textViewProductQuantity = findViewById(R.id.textViewProductQuantity);
        textViewProductExpiry = findViewById(R.id.textViewProductExpiry);

        // Retrieve product details from Firestore
        retrieveProductDetailsFromFirestore();

        // Initialize the "Edit" button and set its click listener
        Button buttonEditProduct = findViewById(R.id.buttonEditProduct);
        buttonEditProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start EditProductActivity and pass the productId and storeId
                Intent intent = new Intent(ProductDetailActivity.this, EditProductActivity.class);
                intent.putExtra("productId", productId);
                intent.putExtra("storeId", storeId);
                startActivityForResult(intent, 1); // Use requestCode 1 for identification
            }
        });

        // Initialize the "Delete" button and set its click listener
        Button buttonDeleteProduct = findViewById(R.id.buttonDeleteProduct);
        buttonDeleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle delete product button click here
                // Delete the product from Firestore
                deleteProductFromFirestore();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // Receive the updated product information from EditProductActivity
            String updatedId = data.getStringExtra("updatedProduct");
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            DocumentReference productRef = firestore.collection("products").document(updatedId);
            productRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        // Document exists, retrieve product details and set them to the views
                        Product product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            // Set the product details to the views
                            textViewProductName.setText("Tên sản phẩm: " + product.getName());
                            textViewProductPrice.setText("Giá: " + String.format("%.2f VNĐ", product.getPrice()));
                            textViewProductQuantity.setText("Tổng số lượng: " + String.format("Quantity: %d", product.getQuantity()));
                            textViewProductExpiry.setText("Hạn sử dụng: " + product.getExpiry());

                            // Load and display the product image using Picasso
                            Picasso.get().load(product.getImage()).into(imageViewProduct);
                        }
                    } else {
                        // Document does not exist or was deleted
                        finish(); // Close the activity if the product is not found
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Error occurred while fetching document
                    Log.e("ProductDetailActivity", "Error getting product document: " + e.getMessage());
                }
            });
        }
    }

    private void retrieveProductDetailsFromFirestore() {
        // Initialize the Firebase Firestore reference
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference productRef = firestore.collection("products").document(productId);

        // Retrieve the product details from Firestore
        productRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Document exists, retrieve product details and set them to the views
                    Product product = documentSnapshot.toObject(Product.class);
                    if (product != null) {
                        // Set the product details to the views
                        textViewProductName.setText("Tên sản phẩm: " + product.getName());
                        textViewProductPrice.setText("Giá: " + String.format("%.2f VNĐ", product.getPrice()));
                        textViewProductQuantity.setText("Tổng số lượng: " + String.format("Quantity: %d", product.getQuantity()));
                        textViewProductExpiry.setText("Hạn sử dụng: " + product.getExpiry());

                        // Load and display the product image using Picasso
                        Picasso.get().load(product.getImage()).into(imageViewProduct);
                    }
                } else {
                    // Document does not exist or was deleted
                    finish(); // Close the activity if the product is not found
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error occurred while fetching document
                Log.e("ProductDetailActivity", "Error getting product document: " + e.getMessage());
            }
        });
    }

    private void deleteProductFromFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference productRef = firestore.collection("products").document(productId);

        productRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Product successfully deleted
                showToast("Đã xóa sản phẩm");
                finish(); // Close the activity after deletion
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to delete product
                Log.e("ProductDetailActivity", "Error deleting product: " + e.getMessage());
            }
        });
    }

    // Helper method to show a toast message
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
