package com.nexis;

import com.github.mikephil.charting.data.BarEntry;
import com.parse.ParseObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Data {

    private Data() {
        // restrict instantiation
    }

    public static ArrayList<BarEntry> getUserMembers(List<ParseObject> nexcellObject, String dataCol)
    {
        ArrayList<BarEntry> memberList = new ArrayList<BarEntry>();

        for(int i = 0;i< nexcellObject.size();i++)
        {
            BarEntry members = new BarEntry (nexcellObject.get(i).getInt(dataCol),i);
            memberList.add(members);
        }

        return memberList;
    }

    public static ArrayList<String> getEpochDates(List<ParseObject> nexcellObject, int dataPoints)
    {
        ArrayList<String> dates = new ArrayList<String>();
        int listSize = nexcellObject.size();

        for (int i = 0; i < dataPoints; i++)
        {
            Date date = (Date)nexcellObject.get(listSize-i-1).get("Date");
            String timeStamp = new SimpleDateFormat("MMMdd").format(date);
            dates.add(0, timeStamp);
        }

        return dates;
    }
}
