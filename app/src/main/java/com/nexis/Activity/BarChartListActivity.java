package com.nexis.Activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.nexis.Data;
import com.nexis.Formats.DeciFormat;
import com.nexis.Formats.PercentFormat;
import com.nexis.R;

import java.util.ArrayList;
import java.util.List;

public class BarChartListActivity extends ActionBarActivity {

    private Toolbar mToolbar;
    private Bundle bun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.barchartlist_activity);

        ListView lv = (ListView) findViewById(R.id.barChartListView);

        ArrayList<BarData> dataList = new ArrayList<>();

        bun = getIntent().getExtras();

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mToolbar.setTitle(bun.getString("title"));

        int i = 1;

        while (bun.getIntegerArrayList("data" + i) != null)
        {
            ArrayList<Integer> current  = bun.getIntegerArrayList("data" + i);
            BarData bData = generateBarData(current, Data.getLabels(Data.NEXCELL_ACTIVE_LIST));
            dataList.add(bData);
            i++;
        }

        ChartDataAdapter cda = new ChartDataAdapter(getApplicationContext(), dataList);
        lv.setAdapter(cda);
    }

    private class ChartDataAdapter extends ArrayAdapter<BarData> {

        public ChartDataAdapter(Context context, List<BarData> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            BarData data = getItem(position);

            ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_barchart, null);
                holder.chart = (BarChart) convertView.findViewById(R.id.barChart);
                holder.chart.getLayoutParams().height = 800;

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ValueFormatter form;
            String formatString = bun.getString("format" + (position + 1));
            if (formatString.equals("P")) form = new PercentFormat();
            else form = new DeciFormat();

            holder.chart.setDescription(bun.getString("desc" + (position + 1)));
            holder.chart.setDrawGridBackground(false);
            data.setValueTextColor(Color.BLACK);
            data.setValueFormatter(form);

            XAxis xAxis = holder.chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setSpaceBetweenLabels(0);

            YAxis leftAxis = holder.chart.getAxisLeft();
            leftAxis.setLabelCount(5);
            leftAxis.setSpaceTop(15f);
            leftAxis.setValueFormatter(form);

            YAxis rightAxis = holder.chart.getAxisRight();
            rightAxis.setEnabled(false);

            holder.chart.setData(data);
            holder.chart.animateY(700);
            holder.chart.setClickable(false);
            holder.chart.setPinchZoom(false);
            holder.chart.setDoubleTapToZoomEnabled(false);

            holder.chart.invalidate();

            return convertView;
        }

        private class ViewHolder {

            BarChart chart;
        }
    }

    private BarData generateBarData(List<Integer> data, List<String> xVals) {

        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            entries.add(new BarEntry(data.get(i), i));
        }

        BarDataSet d = new BarDataSet(entries, "Number of members");
        d.setBarSpacePercent(20f);
        d.setColors(ColorTemplate.VORDIPLOM_COLORS);
        d.setBarShadowColor(Color.rgb(203, 203, 203));

        ArrayList<BarDataSet> sets = new ArrayList<>();
        sets.add(d);

        BarData cd = new BarData(new ArrayList<>(xVals), sets);
        return cd;
    }
}
