package com.nexis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.content.Context;
import android.support.v4.util.ArrayMap;

public class ParseOperation {

    private static final String ATD_LABEL = "attendance";

	static public int getMostRecentVersionCode(Context actv) {
		try
        {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("VersionCode");
			query.orderByDescending("ReleaseDate");
			
			int code = query.getFirst().getInt("VersionCode");
			
			return code;
			
        }
		catch (ParseException e)
        {
        	UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }
		
		return -1;
	}

    static public void saveYearDate(Context actv) {
        List<ParseObject> nexcellObject = new ArrayList<>();

        try
        {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Nexcell");
            query.orderByDescending("End_Date");

            nexcellObject = query.find();

        }
        catch (ParseException e)
        {
            UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }

        Constants.NEXIS_START_DATE = nexcellObject.get(0).getDate("Start_Date");
        Constants.NEXIS_END_DATE = nexcellObject.get(0).getDate("End_Date");
    }

    static public ParseUser getUserByUserName(String username, Context actv) {
        List<ParseUser> nexcellObject = new ArrayList<>();

        try
        {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", username);
            nexcellObject = query.find();
        }
        catch (ParseException e)
        {
            UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }

        if (!nexcellObject.isEmpty()) return nexcellObject.get(0);
        else return null;
    }

	static public List<ParseObject> getUserList(String nexcell, List<Boolean> levels, Context actv) {
        List<ParseObject> nexcellObject = new ArrayList<>();

		try
        {
            List<ParseQuery<ParseObject>> queries = new ArrayList<>();

            if (levels.contains(true))
            {
                for (int i = 0; i < levels.size(); i++)
                {
                    if (levels.get(i))
                    {
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
                        query.whereEqualTo(Constants.USER_LEVEL_LIST.get(i), true);
                        queries.add(query);
                    }
                }

                ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);

                if(nexcell != null) mainQuery.whereEqualTo("nexcell", nexcell);

                mainQuery.orderByAscending("username");

                nexcellObject = mainQuery.find();

            } else {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
                if(nexcell != null) query.whereEqualTo("nexcell", nexcell);
                nexcellObject = query.find();

            }

        }
        catch (ParseException e)
        {
        	UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }

    	return nexcellObject;
	}

    static public String getSubmitDataRecipient(String nexcell, Context actv) {
        List<ParseObject> users = getUserList(nexcell, Arrays.asList(false, true, true, false, false), actv);

        return extractEmail(users);
    }

    static public String getCommCounsRecipient(Context actv) {
        List<ParseObject> users = getUserList(null, Arrays.asList(false, false, true, true, false), actv);

        return extractEmail(users);
    }

    static public String getNexcellLeadersRecipient(String nexcell, Context actv) {
        List<ParseObject> users = getUserList(nexcell, Arrays.asList(false, true, true, false, false), actv);

        return extractEmail(users);
    }

    static public String getNewComerFormRecipient(String nexcell, Context actv) {
        return getCommCounsRecipient(actv) + "," + getNexcellLeadersRecipient(nexcell, actv);
    }

    static private String extractEmail(List<ParseObject> list) {
        List<String> emails = new ArrayList<>();

        for(ParseObject x: list) emails.add(x.get("email").toString());

        String recipients = StringUtils.join(emails, ", ");

        return recipients;
    }

    static public List<ParseUser> getSchools(Context actv)
    {
        List<ParseUser> nexcellObject = new ArrayList<>();

        try
        {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.orderByAscending("school");

            nexcellObject = query.find();
        }
        catch(Exception e)
        {
            UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }

        return nexcellObject;
    }

	static public List<ParseObject> getNexcellList(boolean nameOnly, Context actv) {
		
        List<ParseObject> nexcellObject = new ArrayList<>();
        
        try
        {
        	ParseQuery<ParseObject> query = ParseQuery.getQuery("Nexcell");
            query.whereLessThanOrEqualTo("Start_Date", new Date());
            query.whereGreaterThanOrEqualTo("End_Date", new Date());
            query.orderByAscending("Name");
            if (nameOnly) query.selectKeys(Arrays.asList("Name"));

            nexcellObject = query.find();
        }
        catch (ParseException e)
        {
        	UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }
    	
    	return nexcellObject;
	}
	
    static public List<ParseObject> getNexcellData(String nexcell, String cat, DateTime date, boolean localDb, Context actv) {

        List<ParseObject> nexcellObject = new ArrayList<>();

        try
        {
            int limit = 1000;
            int skip = 0;

            while (true)
            {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("UserAttendance");

                if (localDb)
                {
                    query.fromLocalDatastore();
                    query.fromPin(ATD_LABEL);
                }

                if (nexcell != null) query.whereEqualTo("nexcell", nexcell);
                if (date != null) query.whereEqualTo("date", date.toDate());
                if (cat != null) query.whereEqualTo("category", cat);

                query.orderByAscending("date");
                query.addAscendingOrder("nexcell");
                query.setSkip(skip);
                query.setLimit(limit);

                List<ParseObject> temp = query.find();
                nexcellObject.addAll(temp);

                if (temp.size() > limit) skip = skip + limit;
                else break;
            }

        }
        catch (ParseException e)
        {
            UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }

        return nexcellObject;
    }

    static public void refreshAttendanceLocalData(String nexcell, DateTime date, Context actv) {

        List<ParseObject> nexcellObject = new ArrayList<>();

        try
        {
            ParseObject.unpinAllInBackground(ATD_LABEL);

            int limit = 1000;
            int skip = 0;

            while (true)
            {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("UserAttendance");

                query.whereGreaterThanOrEqualTo("date", Constants.NEXIS_START_DATE);
                query.whereLessThanOrEqualTo("date", Constants.NEXIS_END_DATE);

                if (nexcell != null) query.whereEqualTo("nexcell", nexcell);
                if (date != null) query.whereEqualTo("date", date.toDate());

                query.setSkip(skip);
                query.setLimit(limit);

                List<ParseObject> temp = query.find();
                nexcellObject.addAll(temp);

                if (temp.size() == limit) skip = skip + limit;
                else break;
            }

            ParseObject.pinAll(ATD_LABEL, nexcellObject);
        }
        catch (ParseException e)
        {
            UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }
    }
	
	static public void updateUser(String objectID, final int level, final Context actv) {
        //TODO: user cloud code to update user levels
	}

    static public List<ParseObject> getDeleteData(String nexcell, DateTime date, List<String> catList, Context actv) {

        List<ParseObject> nexcellObject = new ArrayList<>();

        try
        {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("UserAttendance");

            query.fromLocalDatastore();
            query.fromPin(ATD_LABEL);

            query.whereEqualTo("nexcell", nexcell);
            query.whereEqualTo("date", date.toDate());
            query.whereContainedIn("category", catList);

            nexcellObject = query.find();
        }
        catch (ParseException e)
        {
            UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }

        return nexcellObject;
    }

    static public void deleteData(String nexcell, DateTime date, Context actv) {

        List<ParseObject> nexcellObject;

        try
        {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("UserAttendance");

            query.fromLocalDatastore();
            query.fromPin(ATD_LABEL);

            query.whereEqualTo("nexcell", nexcell);
            query.whereEqualTo("date", date.toDate());
            query.whereContainedIn("category", Arrays.asList("Fellowship", "Service", "College"));

            nexcellObject = query.find();

            if (!nexcellObject.isEmpty())
            {
                ParseObject.deleteAllInBackground(nexcellObject);
                ParseObject.unpinAllInBackground(nexcellObject);
            }
        }
        catch (ParseException e)
        {
            UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }
    }

    static public void saveUserAttendance(ArrayMap<String, List<String>> dataMap, DateTime date, String nexcell, String userName, Context actv) {

        deleteData(nexcell, date, actv);

        List<ParseObject> users = new ArrayList<>();

        for(int i = 0; i < Constants.CATEGORY_LIST.size() - 1; i++)
        //for (int i = 0; i < dataMap.size(); i++)
        {
            String cat = Constants.CATEGORY_LIST.get(i);
            for(String nameString : dataMap.get(cat))
            {
                ParseObject data = new ParseObject("UserAttendance");
                data.put("date", date.toDate());
                data.put("nexcell", nexcell);
                data.put("userName", nameString);
                data.put("category", cat);
                data.put("saveBy", userName);
                users.add(data);
            }
        }

        ParseObject.pinAllInBackground(ATD_LABEL, users);
        ParseObject.saveAllInBackground(users);
    }

    static public void saveNewComer(DateTime date, String nexcell, String newComerName, String userName) {

        ParseObject data = new ParseObject("UserAttendance");
        data.put("date", date.toDate());
        data.put("nexcell", nexcell);
        data.put("userName", newComerName);
        data.put("category", "NewComer");
        data.put("saveBy", userName);

        data.saveEventually();
    }
}
