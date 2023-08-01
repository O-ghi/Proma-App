package com.proma.promaapp.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.proma.promaapp.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class OrderListAdapter extends BaseAdapter {

    private Context context;
    private List<Order> orderList;

    public OrderListAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @Override
    public int getCount() {
        return orderList.size();
    }

    @Override
    public Object getItem(int position) {
        return orderList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_order, parent, false);
            viewHolder.tvOrderId = convertView.findViewById(R.id.tvOrderId);
            viewHolder.tvTotalPrice = convertView.findViewById(R.id.tvTotalPrice);
            viewHolder.tvCreateDate = convertView.findViewById(R.id.tvCreateDate); // Add TextView for createDate
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Order order = orderList.get(position);

        viewHolder.tvOrderId.setText(String.valueOf(order.getOrderId()));
        viewHolder.tvTotalPrice.setText(String.format("%.2f VNĐ", order.getTotalPrice()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String createDate = dateFormat.format(order.getCreateDate());
        viewHolder.tvCreateDate.setText(createDate); // Set formatted createDate text

        return convertView;
    }


    private static class ViewHolder {
        TextView tvOrderId;
        TextView tvTotalPrice;
        TextView tvCreateDate; // Add TextView for createDate
    }
}
