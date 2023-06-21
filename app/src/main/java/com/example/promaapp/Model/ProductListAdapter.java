package com.example.promaapp.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.promaapp.R;

import java.util.List;

public class ProductListAdapter extends BaseAdapter {
    // Existing fields
    private Context context;
    private List<Product> productList;

    public ProductListAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    // Rest of the methods...

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_product, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.ivProductImage = convertView.findViewById(R.id.productImageView);
            viewHolder.tvProductName = convertView.findViewById(R.id.productNameTextView);
            viewHolder.tvProductPrice = convertView.findViewById(R.id.productPriceTextView);
            viewHolder.tvProductQuantity = convertView.findViewById(R.id.productQuantityTextView);
            viewHolder.tvProductExpiry = convertView.findViewById(R.id.tvProductExpiry); // New TextView for expiry

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Product product = productList.get(position);

        viewHolder.tvProductName.setText(product.getName());
        viewHolder.tvProductPrice.setText(String.format("$%.2f", product.getPrice()));
        viewHolder.tvProductQuantity.setText(String.valueOf(product.getQuantity()));
        viewHolder.tvProductExpiry.setText("HSD: " + product.getExpiry()); // Set the expiry date

        // Load the product image using Glide library
        // Load the product image using Glide library
        if (product.getImage() != null) {
            Glide.with(context)
                    .load(product.getImage())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(viewHolder.ivProductImage);
        } else {
            viewHolder.ivProductImage.setImageResource(R.drawable.placeholder_image);
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductPrice;
        TextView tvProductQuantity;
        TextView tvProductExpiry; // New TextView for expiry
    }
}

