package com.example.promaapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.example.promaapp.Model.CartList;
import com.example.promaapp.Model.Product;
import com.example.promaapp.Model.ProductListAdapter;

import java.util.ArrayList;
import java.util.List;

public class CartListActivity extends AppCompatActivity {

    private ListView lvCartList;
    private List<Product> cartList;
    private ProductListAdapter cartListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_list);

        lvCartList = findViewById(R.id.lvCartList);
        cartList = new ArrayList<>();
        cartListAdapter = new ProductListAdapter(this, cartList);
        lvCartList.setAdapter(cartListAdapter);

        // Retrieve the cart items from the CartList class
        // You need to implement the logic to fetch the cart items from the data structure or database
        // For this example, let's assume there is a CartList class with static methods to manage the cart items
        List<Product> items = CartList.getCartItems();

        // Add the items to the cart list
        cartList.addAll(items);
        cartListAdapter.notifyDataSetChanged();
    }
}
