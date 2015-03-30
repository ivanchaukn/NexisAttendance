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
import com.nexis.Activity.SummaryActivity;
import com.nexis.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentStat extends DialogFragment implements AdapterView.OnItemClickListener {

    public static FragmentStat newInstance() {
        FragmentStat fragment = new FragmentStat();
        Bundle args = new Bundle();
        args.putInt("Stat", 2);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentStat() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stat, container, false);

        // initialize the utilities
        Utils.init(getResources());

        ArrayList<GraphListItem> objects = new ArrayList<>();

        objects.add(new GraphListItem("Line Chart", "A simple demonstration of the linechart."));
        objects.add(new GraphListItem("Bar Chart", "A simple demonstration of the barchart."));

        GraphListAdapter adapter = new GraphListAdapter(getActivity(), objects);

        ListView lv = (ListView) rootView.findViewById(R.id.graphList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> av, View v, int pos, long arg3) {

        Intent i;

        switch (pos) {
            case 0:
                i = new Intent(getActivity(), SummaryActivity.class);
                startActivity(i);
                break;
        }

        getActivity().overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
    }


    private class GraphListItem {
        String name;
        String desc;

        public GraphListItem(String n, String d) {
            name = n;
            desc = d;
        }
    }

    private class GraphListAdapter extends ArrayAdapter<GraphListItem> {

        public GraphListAdapter(Context context, List<GraphListItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            GraphListItem c = getItem(position);

            ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                convertView = LayoutInflater.from(getContext()).inflate(R.layout.graph_list_item, null);
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
