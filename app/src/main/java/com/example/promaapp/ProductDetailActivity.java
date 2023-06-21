package com.example.promaapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imageViewProduct;
    private TextView textViewProductName;
    private TextView textViewProductPrice;
    private TextView textViewProductQuantity;
    private TextView textViewProductExpiry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail_activity);

        // Retrieve the product details from the intent
        String productName = getIntent().getStringExtra("productName");
        double productPrice = getIntent().getDoubleExtra("productPrice", 0.0);
        int productQuantity = getIntent().getIntExtra("productQuantity", 0);
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String expiry = getIntent().getStringExtra("expiry");
        Log.d("imageUrl", "check " + imageUrl);

        // Initialize views
        imageViewProduct = findViewById(R.id.imageViewProduct);
        textViewProductName = findViewById(R.id.textViewProductName);
        textViewProductPrice = findViewById(R.id.textViewProductPrice);
        textViewProductQuantity = findViewById(R.id.textViewProductQuantity);
        textViewProductExpiry = findViewById(R.id.textViewProductExpiry);

        // Set the product details
        textViewProductName.setText("Tên sản phẩm: " + productName);
        textViewProductPrice.setText("Giá: " + String.format("Price: $%.2f", productPrice));
        textViewProductQuantity.setText("Tổng số lượng: " + String.format("Quantity: %d", productQuantity));
        textViewProductExpiry.setText("Hạn sử dụng: " + expiry);
        // Load and display the product image using Picasso
        Picasso.get().load(imageUrl).into(imageViewProduct);
    }
}
