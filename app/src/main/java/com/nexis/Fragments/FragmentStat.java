package com.nexis.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.nexis.Activity.BarChartListActivity;
import com.nexis.Activity.PieChartActivity;
import com.nexis.Constants;
import com.nexis.Data;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.parse.ParseObject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentStat extends DialogFragment implements AdapterView.OnItemClickListener {

    private DateTime recentDate;
    private List<ParseObject> nexcellObject;

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

        nexcellObject = ParseOperation.getNexcellData(null, null, getActivity());

        recentDate = new DateTime(DateTimeZone.UTC);
        if (recentDate.getDayOfWeek() - 1 < DateTimeConstants.FRIDAY) recentDate = recentDate.minusWeeks(1);
        recentDate = recentDate.withDayOfWeek(DateTimeConstants.FRIDAY);
        recentDate = recentDate.withTimeAtStartOfDay();

        // initialize the utilities
        Utils.init(getResources());

        ArrayList<GraphListItem> objects = new ArrayList<>();

        objects.add(new GraphListItem("Fellowship Attendance Chart", "Display fellowship attendance record"));
        objects.add(new GraphListItem("Service Statistic Chart", "Display the weekly service attendance"));
        objects.add(new GraphListItem("College Statistic Chart", "Display the weekly college attendance"));
        objects.add(new GraphListItem("Pie Chart Distribution", "Display nexcell distribution"));

        GraphListAdapter adapter = new GraphListAdapter(getActivity(), objects);

        ListView lv = (ListView) rootView.findViewById(R.id.graphList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> av, View v, int pos, long arg3) {

        Intent i;
        Bundle b;

        switch (pos) {
            case 0:
                i = new Intent(getActivity(), BarChartListActivity.class);
                b = new Bundle();
                b.putString("title", "Fellowship Attendance");

                b.putString("desc1", "Real-Time (" + recentDate.toString("YYYY-MM-dd") + ")");
                b.putIntegerArrayList("data1", Data.getRecentFellowshipData(nexcellObject, recentDate));
                b.putString("format1", "D");

                b.putString("desc2", "Average");
                b.putIntegerArrayList("data2", Data.getAverageData(nexcellObject, "Fellowship"));
                b.putString("format2", "D");

                i.putExtras(b);
                startActivity(i);
                break;
            case 1:
                i = new Intent(getActivity(), BarChartListActivity.class);
                b = new Bundle();
                b.putString("title", "Service Statistics");

                b.putString("desc1", "(Average in Number)");
                b.putIntegerArrayList("data1", Data.getAverageData(nexcellObject, "Service"));
                b.putString("format1", "D");

                b.putString("desc2", "(Average in %)");
                b.putIntegerArrayList("data2", Data.getRelativeData(nexcellObject, "Service", "Fellowship"));
                b.putString("format2", "P");

                i.putExtras(b);
                startActivity(i);
                break;
            case 2:
                i = new Intent(getActivity(), BarChartListActivity.class);
                b = new Bundle();
                b.putString("title", "College Statistics");

                b.putString("desc1", "(Average in Number)");
                b.putIntegerArrayList("data1", Data.getAverageData(nexcellObject, "College"));
                b.putString("format1", "D");

                b.putString("desc2", "(Average in %)");
                b.putIntegerArrayList("data2", Data.getRelativeData(nexcellObject, "College", "Fellowship"));
                b.putString("format2", "P");

                i.putExtras(b);
                startActivity(i);
                break;
            case 3:
                i = new Intent(getActivity(), PieChartActivity.class);
                b = new Bundle();
                b.putString("title", "Nexis Distribution");
                b.putString("desc1", "(Average in Number)");

                ArrayList <Integer> color = new ArrayList<>();
                ArrayList <String> hsLabel = new ArrayList<>();
                ArrayList <String> uniLabel = new ArrayList<>();

                ArrayList <Integer> hsData = new ArrayList<>();
                ArrayList <Integer> uniData = new ArrayList<>();
                ArrayList<Integer> rawData = Data.getAverageData(nexcellObject, "Fellowship");

                int c1 = 0;
                int c2 = 0;

                for (int j = 0; j < Constants.NEXCELL_ACTIVE_LIST.size(); j++)
                {
                    String nName = Constants.NEXCELL_ACTIVE_LIST.get(j);
                    if (Constants.NEXCELL_STAGE.get(nName).equals(Constants.HS_STRING)) {
                        hsData.add(rawData.get(j));
                        hsLabel.add(nName);
                        c1++;
                    }
                    else {
                        uniData.add(rawData.get(j));
                        uniLabel.add(nName);
                        c2++;
                    }
                }

                hsData.addAll(uniData);
                hsLabel.addAll(uniLabel);
                color.addAll(Constants.BLUE_COLOR_TEMPLATE.subList(0, c1));
                color.addAll(Constants.GREEN_COLOR_TEMPLATE.subList(0, c2));

                b.putIntegerArrayList("data1", hsData);
                b.putStringArrayList("label1", hsLabel);
                b.putIntegerArrayList("color1", color);

                i.putExtras(b);
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
