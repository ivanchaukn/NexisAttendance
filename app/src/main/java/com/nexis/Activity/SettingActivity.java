package com.nexis.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nexis.R;
import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends ActionBarActivity {

    private Toolbar mToolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        List<SettingListItem> sList = new ArrayList<>();
        sList.add(new SettingListItem("Dummy 1"));
        sList.add(new SettingListItem("Dummy 2"));
        sList.add(new SettingListItem("Dummy 3"));

        SettingListAdapter adapter = new SettingListAdapter(this, sList);

        ListView lv = (ListView) findViewById(R.id.settingList);
        lv.setAdapter(adapter);
    }

    public void onItemClick(int pos) {

        Intent i;

        switch (pos) {
            case 0:
                //i = new Intent(this, SummaryActivity.class);
                //startActivity(i);
                break;
        }
    }

    private class SettingListItem {
        public String name;

        public SettingListItem(String n) {
            name = n;
        }
    }

    private class SettingListAdapter extends ArrayAdapter<SettingListItem> {

        public SettingListAdapter(Context context, List<SettingListItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            SettingListItem c = getItem(position);

            ViewHolder holder;

            if (convertView == null) {

                convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);

                holder = new ViewHolder(convertView);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.textView.setText(c.name);
            holder.v.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                onItemClick(position);
                                            }
                                        }
            );

            return convertView;
        }

        public class ViewHolder {
            public TextView textView;
            public View v;

            public ViewHolder(View itemView) {
                v = itemView;
                textView = (TextView) itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
