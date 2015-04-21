package com.nexis.Activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.nexis.Data;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends Activity {

    private String nexcell;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_activity2);
        ParseUser user = ParseUser.getCurrentUser();

        nexcell = (String)user.get("Nexcell");

        List<ParseObject> nexcellObject = ParseOperation.getNexcellData(nexcell, null, this);

        LineChart chart = (LineChart)findViewById(R.id.chart);

        chart.setDescription(nexcell);
        chart.setDrawGridBackground(false);
        chart.setMaxVisibleValueCount(30);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setHighlightEnabled(false);

        ArrayList<Entry> fellowship = Data.getUserMembersLine(nexcellObject, "Fellowship");
        ArrayList<Entry> service = Data.getUserMembersLine(nexcellObject, "Service");
        ArrayList<Entry> college = Data.getUserMembersLine(nexcellObject, "College");
        LineDataSet fellowshipSet = new LineDataSet(fellowship, "Fellowship");
        LineDataSet serviceSet = new LineDataSet(service, "Service");
        LineDataSet collegeSet = new LineDataSet(college, "College");

        //Settings for lines
        fellowshipSet.setColor(Color.DKGRAY);
        fellowshipSet.setCircleColor(Color.DKGRAY);
        fellowshipSet.setLineWidth(2f);
        fellowshipSet.setCircleSize(4f);
        fellowshipSet.setFillAlpha(65);

        serviceSet.setColor(Color.GRAY);
        serviceSet.setCircleColor(Color.GRAY);
        serviceSet.setLineWidth(2f);
        serviceSet.setCircleSize(4f);
        serviceSet.setFillAlpha(65);

        collegeSet.setColor(Color.LTGRAY);
        collegeSet.setCircleColor(Color.LTGRAY);
        collegeSet.setLineWidth(2f);
        collegeSet.setCircleSize(4f);
        collegeSet.setFillAlpha(65);


        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(collegeSet);
        dataSets.add(serviceSet);
        dataSets.add(fellowshipSet);

        // X-Values
        ArrayList<String> dates = Data.getEpochDates(nexcellObject,fellowship.size());

        LineData data = new LineData(dates,dataSets);
        chart.setData(data);
        chart.invalidate();
    }

}
