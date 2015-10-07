package com.nexis.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mikephil.charting.utils.Utils;
import com.nexis.Activity.BarChartListActivity;
import com.nexis.Activity.PieChartActivity;
import com.nexis.Constants;
import com.nexis.Data;
import com.nexis.DescriptionList.DescListAdapter;
import com.nexis.DescriptionList.DescListItem;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.parse.ParseObject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
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

        recentDate = new DateTime(DateTimeZone.UTC);
        if (recentDate.getDayOfWeek() - 1 < DateTimeConstants.FRIDAY) recentDate = recentDate.minusWeeks(1);
        recentDate = recentDate.withDayOfWeek(DateTimeConstants.FRIDAY);
        recentDate = recentDate.withTimeAtStartOfDay();

        // initialize the utilities
        Utils.init(getResources());

        ArrayList<DescListItem> objects = new ArrayList<>();

        objects.add(new DescListItem("Fellowship Attendance Chart", "Display fellowship attendance record", null));
        objects.add(new DescListItem("Service Statistic Chart", "Display the weekly service attendance", null));
        objects.add(new DescListItem("College Statistic Chart", "Display the weekly college attendance", null));
        objects.add(new DescListItem("Pie Chart Distribution", "Display nexcell distribution", null));

        DescListAdapter adapter = new DescListAdapter(getActivity(), objects);

        ListView lv = (ListView) rootView.findViewById(R.id.graphList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(this);

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.stat_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        switch (id) {
            case R.id.refresh_menu:
                ParseOperation.refreshAttendanceLocalData(getActivity());
                Toast.makeText(getActivity(), "Data Updated", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> av, View v, int pos, long arg3) {

        Intent i;
        Bundle b;

        nexcellObject = ParseOperation.getNexcellData(null, null, null, true, getActivity());

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

                for (int j = 0; j < Data.NEXCELL_ACTIVE_LIST.size(); j++)
                {
                    String nName = Data.NEXCELL_ACTIVE_LIST.get(j);

                    if (Data.getNexcellStage(nName).equals(Constants.HS_STRING)) {
                        hsData.add(rawData.get(j));
                        hsLabel.add(Data.getNexcellLabel(nName));
                        c1++;
                    }
                    else {
                        uniData.add(rawData.get(j));
                        uniLabel.add(Data.getNexcellLabel(nName));
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
}
