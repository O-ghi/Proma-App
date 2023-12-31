package com.proma.promaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.proma.promaapp.Model.CartList;
import com.proma.promaapp.Model.Product;
import com.proma.promaapp.Model.ProductListAdapter;
import com.proma.promaapp.R;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderProductActivity extends AppCompatActivity {

    private GridView gvProductList; // Use GridView instead of ListView
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
        gvProductList = findViewById(R.id.gvProductList);
        btnScanBarcode = findViewById(R.id.btnScanBarcode);
        btnViewCart = findViewById(R.id.btnViewCart);

        productList = new ArrayList<>();
        productListAdapter = new ProductListAdapter(this, productList, false,false);
        gvProductList.setAdapter(productListAdapter);
        String storeId = getIntent().getStringExtra("storeId");

        // Retrieve the products from the Firestore database
        retrieveProductsForStore(storeId);

        btnScanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to ScanBarcodeOrderActivity
                Intent intent = new Intent(OrderProductActivity.this, ScanBarcodeOrderActivity.class);
                intent.putExtra("storeId",storeId);
                startActivity(intent);
            }
        });

        btnViewCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to CartListActivity
                Intent intent = new Intent(OrderProductActivity.this, CartListActivity.class);
                intent.putExtra("storeId",storeId);
                startActivity(intent);
            }
        });

        // Handle item click on the product list
        gvProductList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                                product.setAvailableQuantity(productQuantity);
                                productList.add(product);
                            }

                            productListAdapter.notifyDataSetChanged();

                        } else {
                            // No products found for the store
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
            showSuccessMessage("Product added to cart");
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




    private void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
