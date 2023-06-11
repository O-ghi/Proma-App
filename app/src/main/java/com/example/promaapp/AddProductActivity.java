package com.example.promaapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.promaapp.Model.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

public class AddProductActivity extends AppCompatActivity {

    private EditText etId, etName, etPrice, etQuantity;
    private Button btnSave;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = FirebaseFirestore.getInstance();
        String productIdString = getIntent().getStringExtra("productId");

        etId = findViewById(R.id.et_id);
        etName = findViewById(R.id.et_name);
        etPrice = findViewById(R.id.et_price);
        etQuantity = findViewById(R.id.et_quantity);
        btnSave = findViewById(R.id.btn_save);
        if (!productIdString.isEmpty()) {
            etId.setText(productIdString);
        }
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void saveProduct() {

        String id = etId.getText().toString().trim();
        String name = etName.getText().toString().trim();
        double price = Double.parseDouble(etPrice.getText().toString().trim());
        int quantity = Integer.parseInt(etQuantity.getText().toString().trim());

        // Create a new product object
        Product product = new Product(id, name, price, quantity);

        // Get the reference to the products collection
        CollectionReference productsRef = db.collection("products");

        // Add the product to Firestore
        productsRef.document(id).set(product)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddProductActivity.this, "Product saved successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after saving the product
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddProductActivity.this, "Failed to save product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
