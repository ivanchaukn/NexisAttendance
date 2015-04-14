package com.nexis.Activity;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.nexis.Constants;
import com.nexis.Data;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FellowshipActivity extends Activity {

    private String nexcell;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_activity);

        List<ParseObject> nexcellObject = ParseOperation.getNexcellData(null, null, this);

        BarChart chart = (BarChart)findViewById(R.id.chart);

        chart.setDescription("Fellowship");
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setMaxVisibleValueCount(30);

        ArrayList<BarEntry> current = Data.getRecentFellowship(nexcellObject);
        ArrayList<BarEntry> average = Data.getAverageFellowship(nexcellObject);

        BarDataSet fellowshipSet = new BarDataSet(current, "Recent Week Attendance");
        BarDataSet averageSet = new BarDataSet(average, "Average Attendance");


        //Set color for different bars
        fellowshipSet.setColor(Color.rgb(104, 241, 175));
        averageSet.setColor(Color.rgb(164, 228, 251));


        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(fellowshipSet);
        dataSets.add(averageSet);

        // X-Values
        ArrayList<String> groups = new ArrayList<>();
        for(int i = 0; i<Constants.NEXCELL_LIST.size();i++)
            groups.add(Constants.NEXCELL_LIST.get(i));

        BarData data = new BarData(groups,dataSets);
        chart.setData(data);
        chart.invalidate();
    }
}
