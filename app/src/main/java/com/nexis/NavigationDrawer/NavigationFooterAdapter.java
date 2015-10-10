package com.nexis.NavigationDrawer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nexis.R;
import java.util.List;

public class NavigationFooterAdapter extends ArrayAdapter<NavigationItem> {

    private NavigationFooterCallbacks mNavigationFooterCallbacks;
    private int mSelectedPosition;

    public NavigationFooterAdapter(Context context, List<NavigationItem> objects) {
        super(context, 0, objects);
    }

    public void setNavigationFooterCallbacks(NavigationFooterCallbacks navigationDrawerCallbacks) {
        mNavigationFooterCallbacks = navigationDrawerCallbacks;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        NavigationItem c = getItem(position);

        ViewHolder holder;

        if (convertView == null) {

            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_row, parent, false);

            holder = new ViewHolder(convertView);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(c.getText());
        holder.imageView.setImageDrawable(c.getDrawable());

        holder.v.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               if (mNavigationFooterCallbacks != null) {
                                                   mNavigationFooterCallbacks.onNavigationFooterItemSelected(position);
                                               }
                                           }
                                       }
        );



        return convertView;
    }

    public void selectPosition(int position) {
        //int lastPosition = mSelectedPosition;
        mSelectedPosition = position;
    }


    public static class ViewHolder {
        public TextView textView;
        public ImageView imageView;
        public View v;

        public ViewHolder(View itemView) {
            v = itemView;
            textView = (TextView) itemView.findViewById(R.id.item_name);
            imageView = (ImageView) itemView.findViewById(R.id.drawer_row_icon);
        }
    }


}

