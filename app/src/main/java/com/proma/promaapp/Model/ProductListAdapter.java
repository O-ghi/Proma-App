package com.proma.promaapp.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.proma.promaapp.R;

import java.util.List;

public class ProductListAdapter extends BaseAdapter {

    private Context context;
    private List<Product> productList;
    private boolean isCartList;
    private boolean isOrderDetail;
    private OnQuantityChangeListener quantityChangeListener;

    public interface OnQuantityChangeListener {
        void onQuantityChanged(int position, int quantity);
    }

    public ProductListAdapter(Context context, List<Product> productList, boolean isCartList, boolean isOrderDetail) {
        this.context = context;
        this.productList = productList;
        this.isCartList = isCartList;
        this.isOrderDetail = isOrderDetail;
    }

    public void setOnQuantityChangeListener(OnQuantityChangeListener listener) {
        this.quantityChangeListener = listener;
    }

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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            if (isCartList) {
                convertView = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);

                    viewHolder.btnDecreaseQuantity = convertView.findViewById(R.id.btnDecreaseQuantity);
                    viewHolder.btnIncreaseQuantity = convertView.findViewById(R.id.btnIncreaseQuantity);


                viewHolder.tvTotalPrice = convertView.findViewById(R.id.totalPriceTextView);

            } else {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_product, parent, false);
                viewHolder.ivProductImage = convertView.findViewById(R.id.productImageView);
                viewHolder.tvProductExpiry = convertView.findViewById(R.id.tvProductExpiry);
            }

            viewHolder.tvProductName = convertView.findViewById(R.id.productNameTextView);
            viewHolder.tvProductPrice = convertView.findViewById(R.id.productPriceTextView);
            viewHolder.tvProductQuantity = convertView.findViewById(R.id.productQuantityTextView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Product product = productList.get(position);

        viewHolder.tvProductName.setText(product.getName());

        if (isCartList) {
            viewHolder.tvProductQuantity.setText(String.valueOf(product.getQuantity()));
            viewHolder.tvProductPrice.setText(String.format("%.2f VNĐ", product.getPrice()));
        if(isOrderDetail) {
            viewHolder.btnDecreaseQuantity.setVisibility(View.GONE);
            viewHolder.btnIncreaseQuantity.setVisibility(View.GONE);
            } else {
            viewHolder.btnDecreaseQuantity.setVisibility(View.VISIBLE);
            viewHolder.btnIncreaseQuantity.setVisibility(View.VISIBLE);
        }

            double totalPrice = product.getPrice() * product.getQuantity();
            viewHolder.tvTotalPrice.setText(String.format("%.2f VNĐ", totalPrice));
            viewHolder.btnDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Decrease the quantity of the product
                    int quantity = product.getQuantity();
                    if (quantity > 1) {
                        product.setQuantity(quantity - 1);
                        notifyDataSetChanged();
                        if (quantityChangeListener != null) {
                            quantityChangeListener.onQuantityChanged(position, quantity - 1);
                        }
                    }
                }
            });

            viewHolder.btnIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Increase the quantity of the product
                    int quantity = product.getQuantity();
                    int availableQuantity = product.getAvailableQuantity(); // Assuming availableQuantity is the maximum quantity allowed for the product

                    if (quantity < availableQuantity) {
                        product.setQuantity(quantity + 1);
                        notifyDataSetChanged();
                        if (quantityChangeListener != null) {
                            quantityChangeListener.onQuantityChanged(position, quantity + 1);
                        }
                    } else {
                        // Display a message indicating that the maximum quantity has been reached
                        Toast.makeText(context, "Maximum quantity reached", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {
            viewHolder.tvProductQuantity.setText("Số lượng: " + String.valueOf(product.getQuantity()));
            viewHolder.tvProductPrice.setText("Giá tiền: " + String.format("%.2f VNĐ", product.getPrice()));
            viewHolder.tvProductExpiry.setText("HSD: " + product.getExpiry());
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
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductPrice;
        TextView tvProductQuantity;
        TextView tvProductExpiry;
        Button btnDecreaseQuantity;
        Button btnIncreaseQuantity;
        TextView tvTotalPrice;
    }
}
