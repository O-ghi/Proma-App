package com.example.promaapp.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.promaapp.Model.Product;
import com.example.promaapp.R;

import java.util.List;

public class ProductListAdapter extends ArrayAdapter<Product> {

    public ProductListAdapter(Context context, List<Product> products) {
        super(context, 0, products);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_product, parent, false);
        }

        Product product = getItem(position);

        TextView tvProductName = convertView.findViewById(R.id.tvProductName);
        TextView tvProductPrice = convertView.findViewById(R.id.tvProductPrice);
        TextView tvProductDescription = convertView.findViewById(R.id.tvProductDescription);

        tvProductName.setText(product.getName());
        tvProductPrice.setText(String.valueOf(product.getPrice()));
        tvProductDescription.setText("product.getProductDescription()");

        return convertView;
    }
}
