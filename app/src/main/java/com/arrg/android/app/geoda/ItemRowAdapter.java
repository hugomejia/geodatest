package com.arrg.android.app.geoda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemRowAdapter extends ArrayAdapter<ItemRow> {

    private final ArrayList<ItemRow> itemRowArrayList;
    private final Context context;

    public ItemRowAdapter(ArrayList<ItemRow> itemRowArrayList, Context context) {
        super(context, R.layout.listview_item_row, itemRowArrayList);
        this.context = context;
        this.itemRowArrayList = itemRowArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (!itemRowArrayList.get(position).isGroupHeader()) {
            rowView = inflater.inflate(R.layout.listview_item_row, parent, false);

            TextView tvFile = (TextView) rowView.findViewById(R.id.tvFile);
            TextView tvPath = (TextView) rowView.findViewById(R.id.tvPath);

            tvFile.setText(itemRowArrayList.get(position).getNameOfFile());
            tvPath.setText(itemRowArrayList.get(position).getPathOfFile());
        } else {
            rowView = inflater.inflate(R.layout.listview_header_row, parent, false);

            TextView header = (TextView) rowView.findViewById(R.id.tvHeader);
            header.setText(itemRowArrayList.get(position).getNameOfFile());
        }

        return rowView;
    }
}
