package com.nexis.DescriptionList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexis.R;

import java.util.List;

public class DescListAdapter extends ArrayAdapter<DescListItem> {

    public DescListAdapter(Context context, List<DescListItem> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DescListItem c = getItem(position);

        ViewHolder holder;

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_desc, null);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
            holder.descIcon = (ImageView) convertView.findViewById(R.id.descIcon);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvName.setText(c.name);
        holder.tvDesc.setText(c.desc);

        if (c.img != null) holder.descIcon.setImageResource(c.img);

        return convertView;
    }

    private class ViewHolder {

        TextView tvName, tvDesc;
        ImageView descIcon;
    }
}