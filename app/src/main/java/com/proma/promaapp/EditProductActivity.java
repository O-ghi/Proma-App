package com.proma.promaapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.proma.promaapp.Model.Product;
import com.proma.promaapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class EditProductActivity extends AppCompatActivity {

    private Product product;
    private EditText editTextProductName;
    private EditText editTextProductPrice;
    private EditText editTextProductQuantity;
    private EditText editTextProductExpiry;
    private Button buttonSaveChanges;

    private String productId;
    private String storeId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // Retrieve the productId and StoreId from intent
        productId = getIntent().getStringExtra("productId");
        storeId = getIntent().getStringExtra("storeId");

        // Initialize views
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        editTextProductQuantity = findViewById(R.id.editTextProductQuantity);
        editTextProductExpiry = findViewById(R.id.editTextProductExpiry);
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);

        // Retrieve product details from Firestore using the productId and storeId
        retrieveProductDetailsFromFirestore();

        // Add text change listeners to update the product details on user input
        editTextProductName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (product != null) {
                    product.setName(s.toString());
                }
            }
        });

        editTextProductPrice.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (product != null) {
                    double price = s.toString().isEmpty() ? 0.0 : Double.parseDouble(s.toString());
                    product.setPrice(price);
                }
            }
        });

        editTextProductQuantity.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (product != null) {
                    int quantity = s.toString().isEmpty() ? 0 : Integer.parseInt(s.toString());
                    product.setQuantity(quantity);
                }
            }
        });

        editTextProductExpiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Save changes button click listener
        buttonSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the edited product details to Firestore
                saveEditedProductDetails();
            }
        });
    }

    private void retrieveProductDetailsFromFirestore() {
        // Initialize the Firebase Firestore reference
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference productRef = firestore.collection("products").document(productId);

        // Retrieve the product details from Firestore
        productRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Document exists, retrieve product details and set them to the views
                        product = document.toObject(Product.class);
                        if (product != null) {
                            // Set the product details to the views
                            editTextProductName.setText(product.getName());
                            editTextProductPrice.setText(String.valueOf(product.getPrice()));
                            editTextProductQuantity.setText(String.valueOf(product.getQuantity()));
                            editTextProductExpiry.setText(product.getExpiry());
                        }
                    } else {
                        // Document does not exist or was deleted
                        showToast("Product not found");
                        finish(); // Close the activity if the product is not found
                    }
                } else {
                    // Error occurred while fetching document
                    Log.e("EditProductActivity", "Error getting product document: " + task.getException());
                    showToast("Failed to retrieve product details");
                }
            }
        });
    }

    private void showDatePickerDialog() {
        // Get the current date for initializing the DatePickerDialog
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog and show it
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                EditProductActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        editTextProductExpiry.setText(selectedDate);
                        product.setExpiry(selectedDate);
                    }
                },
                year,
                month,
                dayOfMonth
        );

        // Set the minimum date to the current date
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    // Method to save the edited product details to Firestore
    private void saveEditedProductDetails() {
        // Check if the product object is not null
        if (product != null) {
            // Update the product details in Firestore
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("products").document(productId)
                    .set(product)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Product details updated successfully
                                showToast("Đã lưu thay đổi");

                                // Set the result to indicate successful product update
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("updatedProduct", productId);
                                setResult(RESULT_OK, resultIntent);

                                finish(); // Close the activity after saving changes
                            } else {
                                // Failed to update product details
                                Log.e("EditProductActivity", "Error updating product details: " + task.getException());
                                showToast("Có lỗi xảy ra, thay đổi chưa được lưu");
                            }
                        }
                    });
        }
    }

    // Simple TextWatcher to avoid redundant code
    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    // Helper method to show a toast message
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
