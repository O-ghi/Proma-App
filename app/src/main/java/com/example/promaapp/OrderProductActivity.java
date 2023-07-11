package com.example.promaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.promaapp.Model.CartList;
import com.example.promaapp.Model.Product;
import com.example.promaapp.Model.ProductListAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderProductActivity extends AppCompatActivity {

    private ListView lvProductList;
    private List<Product> productList;
    private ProductListAdapter productListAdapter;
    private FirebaseFirestore firestore;
    private Button btnScanBarcode;
    private Button btnViewCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_product);

        firestore = FirebaseFirestore.getInstance();
        lvProductList = findViewById(R.id.lvProductList);
        btnScanBarcode = findViewById(R.id.btnScanBarcode);
        btnViewCart = findViewById(R.id.btnViewCart);

        productList = new ArrayList<>();
        productListAdapter = new ProductListAdapter(this, productList);
        lvProductList.setAdapter(productListAdapter);
        String storeId = getIntent().getStringExtra("storeId");

        // Retrieve the products from the Firestore database
        retrieveProductsForStore(storeId);

        btnScanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to ScanBarcodeOrderActivity
                Intent intent = new Intent(OrderProductActivity.this, ScanBarcodeOrderActivity.class);
                startActivity(intent);
            }
        });

        btnViewCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to CartListActivity
                Intent intent = new Intent(OrderProductActivity.this, CartListActivity.class);
                startActivity(intent);
            }
        });

        // Handle item click on the product list
        lvProductList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected product
                Product product = productList.get(position);

                // Add the product to the cart list
                addToCartList(product);

                // Show success message
                showSuccessMessage("Product added to cart");
            }
        });
    }

    private void retrieveProductsForStore(String storeId) {
        firestore.collection("products")
                .whereEqualTo("storeId", storeId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            // Error occurred
                            Toast.makeText(OrderProductActivity.this, "Failed to retrieve products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            productList.clear();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String productId = document.getId();
                                String productName = document.getString("name");
                                double productPrice = document.getDouble("price");
                                int productQuantity = Math.toIntExact(document.getLong("quantity"));
                                String imageURL = document.getString("image");
                                String expiry = document.getString("expiry");
                                Product product = new Product(productId, storeId, productName, productPrice, productQuantity, imageURL, expiry);
                                productList.add(product);
                            }

                            productListAdapter.notifyDataSetChanged();

                        } else {
                            // No products found for the store
                            Toast.makeText(OrderProductActivity.this, "No products found for the store", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addToCartList(Product product) {
        // Implement the logic to add the product to the cart list
        // You can use a data structure or a database to store the cart items
        // For this example, let's assume there is a CartList class with static methods to manage the cart items

        // Check if the product already exists in the cart list
        int existingIndex = CartList.getCartItemIndex(product);

        if (existingIndex != -1) {
            // Product already exists in the cart list, update the quantity
            Product existingProduct = CartList.getCartItem(existingIndex);
            existingProduct.setQuantity(existingProduct.getQuantity() + product.getQuantity());
        } else {
            // Product doesn't exist in the cart list, add it
            CartList.addToCart(product);
        }

        // Show success message
        showSuccessMessage("Product added to cart");
    }


    private void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
