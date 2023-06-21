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
        String storeId = getIntent().getStringExtra("storeId");

        etId = findViewById(R.id.et_id);
        etName = findViewById(R.id.et_name);
        etPrice = findViewById(R.id.et_price);
        etQuantity = findViewById(R.id.et_quantity);
        btnSave = findViewById(R.id.btn_save);

        etId.setText(productIdString);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productId = etId.getText().toString().trim();
                String productName = etName.getText().toString().trim();
                double price = Double.parseDouble(etPrice.getText().toString().trim());
                int quantity = Integer.parseInt(etQuantity.getText().toString().trim());

                if (productId.isEmpty() || productName.isEmpty() || etPrice.getText().toString().isEmpty() || etQuantity.getText().toString().isEmpty()) {
                    Toast.makeText(AddProductActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    saveProduct(productId, productName, price, quantity, storeId);
                }
            }
        });
    }

    private void saveProduct(String productId, String productName, double price, int quantity, String storeId) {
        CollectionReference productsRef = db.collection("products");

        Product product = new Product(productId, storeId, productName, price, quantity);

        productsRef.document(productId).set(product)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddProductActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Optional: Finish the add product activity after saving the product
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddProductActivity.this, "Failed to add product", Toast.LENGTH_SHORT).show();
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

}
