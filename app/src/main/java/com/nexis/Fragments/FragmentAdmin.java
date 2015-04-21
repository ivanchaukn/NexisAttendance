package com.nexis.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.utils.Utils;
import com.nexis.Activity.StatusActivity;
import com.nexis.Activity.SummaryActivity;
import com.nexis.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentAdmin extends DialogFragment implements AdapterView.OnItemClickListener {

    public static FragmentAdmin newInstance() {
        FragmentAdmin fragment = new FragmentAdmin();
        Bundle args = new Bundle();
        args.putInt("Admin", 4);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentAdmin() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_admin, container, false);

        // initialize the utilities
        Utils.init(getResources());

        ArrayList<AdminListItem> objects = new ArrayList<>();

        objects.add(new AdminListItem("Real-Time Status", "Monitor the attendance status for each nexcell"));
        objects.add(new AdminListItem("User Management", "Modify user information and access level"));
        objects.add(new AdminListItem("Nexcell Management", "Nexis group distribution"));

        GraphListAdapter adapter = new GraphListAdapter(getActivity(), objects);

        ListView lv = (ListView) rootView.findViewById(R.id.adminList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> av, View v, int pos, long arg3) {

        Intent i;

        switch (pos) {
            case 0:
                i = new Intent(getActivity(), StatusActivity.class);
                startActivity(i);
                break;
        }
    }


    private class AdminListItem {
        String name;
        String desc;

        public AdminListItem(String n, String d) {
            name = n;
            desc = d;
        }
    }

    private class GraphListAdapter extends ArrayAdapter<AdminListItem> {

        public GraphListAdapter(Context context, List<AdminListItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            AdminListItem c = getItem(position);

            ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_desc, null);
                holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
                holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvName.setText(c.name);
            holder.tvDesc.setText(c.desc);

            return convertView;
        }

        private class ViewHolder {

            TextView tvName, tvDesc;
        }
    }
}
