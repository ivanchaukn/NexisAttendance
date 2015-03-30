package com.nexis.Activity;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.nexis.Data;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends Activity {

    private String nexcell;

//    private ArrayList<Number> graphDates;
//    private static ArrayList<List<Integer>> nexisData = new ArrayList<List<Integer>>(3);
//    private static ArrayList<List<Integer>> hsData = new ArrayList<List<Integer>>(3);
//    private static ArrayList<List<Integer>> uniData = new ArrayList<List<Integer>>(3);

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_activity);
        ParseUser user = ParseUser.getCurrentUser();

        nexcell = (String)user.get("Nexcell");

        List<ParseObject> nexcellObject = ParseOperation.getNexcellData(nexcell, null, this);

        BarChart chart = (BarChart)findViewById(R.id.chart);

        chart.setDescription(nexcell);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);
        chart.setMaxVisibleValueCount(30);

        ArrayList<BarEntry> fellowship = Data.getUserMembers(nexcellObject, "Fellowship");
        ArrayList<BarEntry> service = Data.getUserMembers(nexcellObject,"Service");
        ArrayList<BarEntry> college = Data.getUserMembers(nexcellObject,"College");
        BarDataSet fellowshipSet = new BarDataSet(fellowship, "Fellowship");
        BarDataSet serviceSet = new BarDataSet(service, "Service");
        BarDataSet collegeSet = new BarDataSet(college, "College");

        //Set color for different bars
        fellowshipSet.setColor(Color.rgb(104, 241, 175));
        serviceSet.setColor(Color.rgb(164, 228, 251));
        collegeSet.setColor(Color.rgb(242, 247, 158));

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(fellowshipSet);
        dataSets.add(serviceSet);
        dataSets.add(collegeSet);

        // X-Values
        ArrayList<String> dates = Data.getEpochDates(nexcellObject,fellowship.size());

        BarData data = new BarData(dates,dataSets);
        chart.setData(data);
        chart.invalidate();
    }
//    private void getNexisData()
//    {
//        hsData.clear();
//        uniData.clear();
//        nexisData.clear();
//
//        for(int i = 0; i < 3; i++)
//        {
//            hsData.add(new ArrayList<Integer>());
//            uniData.add(new ArrayList<Integer>());
//            nexisData.add(new ArrayList<Integer>());
//        }
//
//        List<ParseObject> nexcellObject = ParseOperation.getNexcellData(null, null, getActivity());
//
//        int startRow = 0;
//        List<Integer> hsNum = Arrays.asList(0, 0, 0);
//        List<Integer> uniNum = Arrays.asList(0, 0, 0);
//        List<Integer> nexisNum = Arrays.asList(0, 0, 0);
//        graphDates = new ArrayList<Number>();
//
//        while(startRow < nexcellObject.size() && !Constants.NEXCELL_LIST.contains(nexcellObject.get(startRow).get("Nexcell"))) startRow++;
//
//        if (startRow == nexcellObject.size()) return;
//
//        DateTime rowDate = new DateTime((Date)nexcellObject.get(startRow).get("Date"), DateTimeZone.UTC);
//
//        for(int i = startRow; i < nexcellObject.size(); i++)
//        {
//            Date d = (Date)nexcellObject.get(i).get("Date");
//            DateTime currentDate = new DateTime(d, DateTimeZone.UTC);
//            String currentNexcell = (String)nexcellObject.get(i).get("Nexcell");
//
//            if (!Constants.NEXCELL_LIST.contains(currentNexcell)) continue;
//
//            if (!currentDate.equals(rowDate))
//            {
//                for(int j = 0; j <= 2; j++)
//                {
//                    hsData.get(j).add(hsNum.get(j));
//                    uniData.get(j).add(uniNum.get(j));
//                    nexisData.get(j).add(nexisNum.get(j));
//                }
//
//                hsNum = Arrays.asList(0,0,0);
//                uniNum = Arrays.asList(0,0,0);
//                nexisNum = Arrays.asList(0,0,0);
//                graphDates.add(rowDate.toDate().getTime());
//                rowDate = currentDate;
//            }
//            for(int j = 0; j <= 2; j++)
//            {
//                int num = nexcellObject.get(i).getInt(Constants.CATEGORY_LIST.get(j));
//
//                if (Constants.NEXCELL_MAP.get(currentNexcell).equals("HighSchool"))
//                    hsNum.set(j, hsNum.get(j)+num);
//                else
//                    uniNum.set(j, uniNum.get(j)+num);
//
//                nexisNum.set(j, nexisNum.get(j)+num);
//            }
//        }
//
//        for(int j = 0; j <= 2; j++)
//        {
//            hsData.get(j).add(hsNum.get(j));
//            uniData.get(j).add(uniNum.get(j));
//            nexisData.get(j).add(nexisNum.get(j));
//        }
//        graphDates.add(rowDate.toDate().getTime());
//    }
}
