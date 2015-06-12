package com.nexis.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.PercentFormatter;
import com.nexis.R;

import java.util.ArrayList;
import java.util.List;

public class PieChartActivity extends ActionBarActivity {

    private Toolbar mToolbar;
    private Bundle bun;

    PieChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.piechart_activity);

        bun = getIntent().getExtras();

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mToolbar.setTitle(bun.getString("title"));

        ArrayList<Integer> currentData  = bun.getIntegerArrayList("data1");
        ArrayList<String> currentLabel  = bun.getStringArrayList("label1");
        ArrayList<Integer> colors  = bun.getIntegerArrayList("color1");

        mChart = (PieChart) this.findViewById(R.id.pieChart);

        mChart.setUsePercentValues(true);

        // change the color of the center-hole
        // mChart.setHoleColor(Color.rgb(235, 235, 235));
        mChart.setHoleColorTransparent(true);

        mChart.setHoleRadius(60f);

        mChart.setDescription("");

        mChart.setDrawCenterText(true);

        mChart.setDrawHoleEnabled(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);

        // mChart.setTouchEnabled(false);

        mChart.setCenterText("Nexis Distribution");

        setData(currentData ,currentLabel, colors);

        mChart.animateXY(1500, 1500);
        // mChart.spin(2000, 0, 360);

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
    }

    private void setData(List<Integer> dataInt, List<String> xValsList, List<Integer> colorList) {

        ArrayList<Entry> yVals1 = new ArrayList<>();

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        for (int i = 0; i < dataInt.size(); i++) {
            yVals1.add(new Entry(dataInt.get(i), i));
        }

        ArrayList<String> xVals = new ArrayList<>();

        for (int i = 0; i < dataInt.size(); i++)
            xVals.add(xValsList.get(i));

        PieDataSet dataSet = new PieDataSet(yVals1, "Distribution in %");
        dataSet.setSliceSpace(3f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : colorList)
            colors.add(c);

        dataSet.setColors(colors);

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }
}
