package com.nexis;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.parse.ParseObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Data {

    public static HashMap<String, List<Object>> NEXCELL_DETAILS;

    public static List<String> NEXCELL_LIST;
    public static List<String> NEXCELL_ACTIVE_LIST;

    private Data() {
        // restrict instantiation
    }

    public static String getNexcellLabel(String nexcell)
    {
        return (String) NEXCELL_DETAILS.get(nexcell).get(0);
    }

    public static String getNexcellStage(String nexcell)
    {
        return (String) NEXCELL_DETAILS.get(nexcell).get(1);
    }

    public static void initializeNexcell(Context context)
    {
        List<ParseObject> nexcellObject = ParseOperation.getNexcellList(false, context);

        List<String> chdgroupsList = new ArrayList<>();
        for (ParseObject x : nexcellObject)
        {
            String childGroup = x.get("Child").toString();
            if (!(childGroup.equals(""))) chdgroupsList.add(childGroup);
        }

        NEXCELL_DETAILS = new HashMap<>();
        NEXCELL_LIST = new ArrayList<>();
        NEXCELL_ACTIVE_LIST = new ArrayList<>();

        for(ParseObject x: nexcellObject)
        {
            String nexcell = x.get("Name").toString();
            String stage = x.get("Stage").toString();
            String childGroup = x.get("Child").toString();
            DateTime mergeDate = new DateTime(x.get("Merge_Date"), DateTimeZone.UTC);

            List<Object> details = new ArrayList<>();

            if (childGroup.equals("")) details.add(nexcell);
            else details.add(nexcell + " + " + childGroup);

            details.add(stage);
            details.add(childGroup);
            details.add(mergeDate);
            NEXCELL_DETAILS.put(nexcell, details);

            NEXCELL_LIST.add(nexcell);
            if (!chdgroupsList.contains(nexcell)) NEXCELL_ACTIVE_LIST.add(nexcell);
        }
    }

    public static List<String> getLabels(List<String> nexcellList)
    {
        List<String> labels = new ArrayList<>();

        for (String x : nexcellList) labels.add(getNexcellLabel(x));

        return labels;
    }

    public static ArrayMap<String, String> getNexcellMemberNameMap(String nexcell, Context actv)
    {
        ArrayMap<String, String> names = new ArrayMap<>();
        List<ParseObject> users = ParseOperation.getUserList(nexcell, Arrays.asList(true, true, true, true, true), actv);

        for(ParseObject x : users)
        {
            String initial;
            if (x.get("initial").toString().equals("")) initial = "";
            else initial = x.get("initial").toString() + " ";

            names.put(x.get("username").toString(), x.get("firstName") + " " + initial + x.get("lastName"));
        }
        return names;
    }

    public static ArrayList<Integer> getRecentFellowshipData (List<ParseObject> nexcellObject, DateTime date)
    {
        ArrayList<Integer> memberList = new ArrayList<>();

        for (int i = 0; i < NEXCELL_ACTIVE_LIST.size(); i++) memberList.add(0);

        DateTime rowDate = date;

        for(int row = nexcellObject.size() - 1; date.equals(rowDate) && row >= 0; row--)
        {
            String rowNexcell = (String) nexcellObject.get(row).get("Nexcell");

            for(int i = 0; i < NEXCELL_ACTIVE_LIST.size(); i++)
            {
                if (NEXCELL_ACTIVE_LIST.get(i).equals(rowNexcell))
                {
                    memberList.set(i, nexcellObject.get(row).getInt("Fellowship"));
                    rowDate = new DateTime(nexcellObject.get(row - 1).get("Date"), DateTimeZone.UTC);
                }
            }
        }
        return memberList;
    }

    public static ArrayList<Integer> getAverageData (List<ParseObject> nexcellObject, String type)
    {
        ArrayList<Integer> memberList = new ArrayList<>();
        ArrayList<Integer> data = new ArrayList<>();
        ArrayList<Integer> numOfData = new ArrayList<>();
        for (int i = 0; i < NEXCELL_ACTIVE_LIST.size(); i++) {
            data.add(0);
            numOfData.add(0);
        }

        for(int row = nexcellObject.size() - 1; row >= 0; row--)
        {
            String rowNexcell = (String) nexcellObject.get(row).get("Nexcell");

            if (NEXCELL_ACTIVE_LIST.contains(nexcellObject.get(row).get("Nexcell")))
            {
                int pos = NEXCELL_ACTIVE_LIST.indexOf(rowNexcell);

                data.set(pos, data.get(pos) + nexcellObject.get(row).getInt(type));
                numOfData.set(pos, numOfData.get(pos) + 1);
            }
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
}
