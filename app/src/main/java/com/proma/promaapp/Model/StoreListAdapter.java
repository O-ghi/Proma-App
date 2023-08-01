package com.proma.promaapp.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.proma.promaapp.R;

import java.util.List;

public class StoreListAdapter extends BaseAdapter {

    private Context context;
    private List<Store> storeList;

    public StoreListAdapter(Context context, List<Store> storeList) {
        this.context = context;
        this.storeList = storeList;
    }

    @Override
    public int getCount() {
        return storeList.size();
    }

    @Override
    public Object getItem(int position) {
        return storeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_store, null);
        }

        TextView tvStoreName = view.findViewById(R.id.tvStoreName);
        TextView tvStoreAddress = view.findViewById(R.id.tvStoreAddress);

        Store store = storeList.get(position);
        tvStoreName.setText(store.getStoreName());
        tvStoreAddress.setText(store.getStoreAddress());

        return view;
    }
}
