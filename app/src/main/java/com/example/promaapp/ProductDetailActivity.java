package com.example.promaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView tvId, tvName, tvPrice, tvQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail_activity);

        tvId = findViewById(R.id.tv_id);
        tvName = findViewById(R.id.tv_name);
        tvPrice = findViewById(R.id.tv_price);
        tvQuantity = findViewById(R.id.tv_quantity);

        // Retrieve the product ID from the intent
        String productId = getIntent().getStringExtra("productId");

        // Retrieve the product information from Firestore based on the ID
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("products").document(productId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Retrieve the product details from the document
                        String name = document.getString("name");
                        double price = document.getDouble("price");
                        int quantity = document.getLong("total").intValue();

                        // Set the product details in the TextViews
                        tvId.setText(productId);
                        tvName.setText(name);
                        tvPrice.setText(String.valueOf(price));
                        tvQuantity.setText(String.valueOf(quantity));
                    } else {
                        Log.d("ProductDetailActivity", "No such document");
                    }
                } else {
                    Log.d("ProductDetailActivity", "get failed with " + task.getException());
                }
            }
        });
    }
}
