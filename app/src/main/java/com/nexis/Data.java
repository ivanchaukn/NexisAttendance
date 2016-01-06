package com.nexis;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.ArrayMap;

import com.parse.ParseObject;
import com.parse.ParseUser;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Data {

    public static HashMap<String, List<Object>> NEXCELL_DETAILS;

    public static List<String> NEXCELL_LIST;
    public static List<String> NEXCELL_ACTIVE_LIST;
    public static List<String> SCHOOL_LIST;

    private Data() {
        // restrict instantiation
    }

    static public String getNexcellLabel(String nexcell)
    {
        return (String) NEXCELL_DETAILS.get(nexcell).get(0);
    }

    static public String getNexcellStage(String nexcell)
    {
        return (String) NEXCELL_DETAILS.get(nexcell).get(1);
    }

    static public List<String> getLabels(List<String> nexcellList)
    {
        List<String> labels = new ArrayList<>();

        for (String x : nexcellList) labels.add(getNexcellLabel(x));

        return labels;
    }

    static public void initializeSchools(Context context)
    {
        SCHOOL_LIST = new ArrayList<>();
        List<ParseUser> nexcellObject = ParseOperation.getSchools(true, context);

        for (ParseObject x : nexcellObject)
        {
            String school = x.get("school").toString();
            if (!(SCHOOL_LIST.contains(school))) SCHOOL_LIST.add(school);
        }
    }

    static public void initializeNexcell(Context context)
    {
        List<ParseObject> nexcellObject = ParseOperation.getNexcellList(false, true, context);

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

    static public void setupApp(String nexcell, boolean conn, Context context)
    {
        if (conn) Data.syncAllData(nexcell, context);

        //Initialize global variables
        Data.setYearDate(context);
        Data.initializeNexcell(context);
        Data.initializeSchools(context);
    }

    static public void syncYearDate(Context actv) {
        ParseOperation.getYearDate(false, actv);
    }

    static public void syncNexcellData(String nexcell, DateTime date, Context actv) {
        ParseOperation.getUserAttendance(nexcell, null, date, false, actv);
    }

    static public void syncUserList(String nexcell, Context actv) {
        ParseOperation.getUserList(nexcell, Arrays.asList(false, false, false, false, false), false, actv);
    }

    static public void syncNexcellList(Context actv) {
        ParseOperation.getNexcellList(false, false, actv);
    }

    static public void syncSchoolList(Context actv) {
        ParseOperation.getSchools(false, actv);
    }

    static public void syncAllData(String nexcell, Context actv) {
        syncYearDate(actv);
        syncNexcellData(nexcell, null, actv);
        syncUserList(nexcell, actv);
        syncNexcellList(actv);
        syncSchoolList(actv);
    }

    static private String extractEmail(List<ParseObject> list) {
        List<String> emails = new ArrayList<>();

        for(ParseObject x: list) emails.add(x.get("email").toString());

        String recipients = StringUtils.join(emails, ", ");

        return recipients;
    }

    static public String getSubmitDataRecipient(String nexcell, Context actv) {
        List<ParseObject> users = ParseOperation.getUserList(nexcell, Arrays.asList(false, true, true, false, false), true, actv);

        return extractEmail(users);
    }

    static public String getCommCounsRecipient(Context actv) {
        List<ParseObject> users = ParseOperation.getUserList(null, Arrays.asList(false, false, true, true, false), true, actv);

        return extractEmail(users);
    }

    static public String getNexcellLeadersRecipient(String nexcell, Context actv) {
        List<ParseObject> users = ParseOperation.getUserList(nexcell, Arrays.asList(false, true, true, false, false), true, actv);

        return extractEmail(users);
    }

    static public String getNewComerFormRecipient(String nexcell, Context actv) {
        return getCommCounsRecipient(actv) + "," + getNexcellLeadersRecipient(nexcell, actv);
    }

    static public void setYearDate(Context actv) {
        List<ParseObject> obj = ParseOperation.getYearDate(true, actv);

        Constants.NEXIS_START_DATE = obj.get(0).getDate("Start_Date");
        Constants.NEXIS_END_DATE = obj.get(0).getDate("End_Date");
    }

    static public void saveUserLevels(ParseUser user, Context actv) {

        SharedPreferences.Editor editor = actv.getSharedPreferences("levels", actv.MODE_PRIVATE).edit();

        editor.putBoolean(Constants.USER_LEVEL_LIST.get(0), user.getBoolean("member"));
        editor.putBoolean(Constants.USER_LEVEL_LIST.get(1), user.getBoolean("esm"));
        editor.putBoolean(Constants.USER_LEVEL_LIST.get(2), user.getBoolean("counsellor"));
        editor.putBoolean(Constants.USER_LEVEL_LIST.get(3), user.getBoolean("committee"));
        editor.putBoolean(Constants.USER_LEVEL_LIST.get(4), user.getBoolean("developer"));

        editor.commit();
    }

    public static ArrayMap<String, String> getNexcellMemberNameMap(String nexcell, Context actv)
    {
        ArrayMap<String, String> names = new ArrayMap<>();
        List<ParseObject> users = ParseOperation.getUserList(nexcell, Arrays.asList(false, false, false, false, false), true, actv);

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
