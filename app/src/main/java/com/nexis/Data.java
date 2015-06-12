package com.nexis;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.parse.ParseObject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Data {

    private Data() {
        // restrict instantiation
    }

    public static ArrayList<Entry> getUserMembersLine(List<ParseObject> nexcellObject, String dataCol)
    {
        ArrayList<Entry> memberList = new ArrayList<>();

        for(int i = 0;i< nexcellObject.size();i++)
        {
            Entry members = new Entry (nexcellObject.get(i).getInt(dataCol),i);
            memberList.add(members);
        }

        return memberList;
    }

    public static ArrayList<Integer> getRecentFellowshipData (List<ParseObject> nexcellObject, DateTime date)
    {
        ArrayList<Integer> memberList = new ArrayList<>();

        for (int i = 0; i < Constants.NEXCELL_ACTIVE_LIST.size(); i++) memberList.add(0);

        DateTime rowDate = date;

        for(int row = nexcellObject.size() - 1; date.equals(rowDate); row--)
        {
            int n = 0;

            if (!date.equals(rowDate)) break;

            while(!nexcellObject.get(row).get("Nexcell").equals(Constants.NEXCELL_ACTIVE_LIST.get(n))) n++;

            memberList.set(n, nexcellObject.get(row).getInt("Fellowship"));
            rowDate = new DateTime(nexcellObject.get(row - 1).get("Date"), DateTimeZone.UTC);
        }
        return memberList;
    }

    public static ArrayList<Integer> getAverageData (List<ParseObject> nexcellObject, String type)
    {
        ArrayList<Integer> memberList = new ArrayList<>();
        ArrayList<Integer> data = new ArrayList();
        ArrayList<Integer> numOfData = new ArrayList();
        for (int i = 0; i< Constants.NEXCELL_ACTIVE_LIST.size();i++) {
            data.add(0);
            numOfData.add(0);
        }

        for(int row = nexcellObject.size() - 1; Constants.NEXCELL_ACTIVE_LIST.contains(nexcellObject.get(row).get("Nexcell")); row--)
        {
            int pos = Constants.NEXCELL_ACTIVE_LIST.indexOf(nexcellObject.get(row).get("Nexcell"));

            data.set(pos, data.get(pos) + nexcellObject.get(row).getInt(type));
            numOfData.set(pos, numOfData.get(pos) + 1);
        }

        for(int grp = 0; grp < data.size(); grp++)
        {
            int average = data.get(grp)/numOfData.get(grp);
            memberList.add(average);
        }
        return memberList;
    }

    public static ArrayList<Integer> getRelativeData (List<ParseObject> nexcellObject, String type1, String type2)
    {
        ArrayList<Integer> data1 = getAverageData(nexcellObject, type1);
        ArrayList<Integer> data2 = getAverageData(nexcellObject, type2);

        ArrayList<Integer> newData = new ArrayList<>();
        for (int i = 0; i < data1.size(); i++)
        {
            double percent = (double)data1.get(i).intValue() / (double)data2.get(i).intValue();
            newData.add(((Double)(percent * 100)).intValue());
        }

        return newData;
    }

    public static ArrayList<Integer> getDistributionData (List<ParseObject> nexcellObject, String type1)
    {
        double sum = 0;
        ArrayList<Integer> data1 = getAverageData(nexcellObject, type1);

        for(Integer x : data1) sum = sum + (double) x;

        ArrayList<Integer> newData = new ArrayList<>();
        for (int i = 0; i < data1.size(); i++)
        {
            double percent = (double)data1.get(i).intValue() / sum;
            newData.add(((Double)(percent * 100)).intValue());
        }

        return newData;
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
