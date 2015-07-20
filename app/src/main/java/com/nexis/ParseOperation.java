package com.nexis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.content.Context;

public class ParseOperation {

	static public int getMostRecentVersionCode(Context actv)
	{
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
	
	
	static public List<ParseObject> getUserList(String nexcell, List<Boolean> levels, Context actv)
	{
		List<ParseObject> nexcellObject = new ArrayList<>();
		
		try
        {
            List<ParseQuery<ParseObject>> queries = new ArrayList<>();

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

            if(nexcell != null) mainQuery.whereEqualTo("Nexcell", nexcell);

            mainQuery.orderByAscending("username");

            nexcellObject = mainQuery.find();

        }
        catch (ParseException e)
        {
        	UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }

    	return nexcellObject;
	}

    static public String getSubmitDataRecipient(String nexcell, Context actv)
    {
        List<ParseObject> users = getUserList(nexcell, Arrays.asList(true, false, true, false), actv);

        return extractEmail(users);
    }

    static public String getWeeklyReportRecipient(Context actv)
    {
        List<ParseObject> users = getUserList(null, Arrays.asList(false, true, true, false), actv);

        return extractEmail(users);
    }

    static public String getNewComerFormRecipient(String nexcell, Context actv)
    {
        List<ParseObject> users = getUserList(nexcell, Arrays.asList(true, true, true, true), actv);
        List<ParseObject> commi = getUserList(null, Arrays.asList(false, true, false, false), actv);
        users.addAll(commi);

        return extractEmail(users);
    }

    static private String extractEmail(List<ParseObject> list)
    {
        List<String> emails = new ArrayList<>();

        for(ParseObject x: list) emails.add(x.get("email").toString());

        String recipients = StringUtils.join(emails, ", ");

        return recipients;
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
	
	static public List<ParseObject> getNexcellData(String nexcell, DateTime date, Context actv) {
		
        List<ParseObject> nexcellObject = new ArrayList<>();
        
        try
        {
        	ParseQuery<ParseObject> query = ParseQuery.getQuery("Attendance");
        	
            if (nexcell != null) query.whereEqualTo("Nexcell", nexcell);
            if (date != null) query.whereEqualTo("Date", date.toDate());
            
            query.orderByAscending("Date");
            query.addAscendingOrder("Nexcell");
            query.setLimit(1000);
            
        	nexcellObject = query.find();
        }
        catch (ParseException e)
        {
        	UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }

    	return nexcellObject;
	}
	
	static public void updateUser(String objectID, final int level, final Context actv) {
        //TODO: user cloud codee to update user levels
	}
	
	static public void updateData(String objectID, final int f, final int s, final int c, final int n, final String userName, Context actv) {
		
    	ParseQuery<ParseObject> query = ParseQuery.getQuery("Attendance");
    	
    	query.getInBackground(objectID, new GetCallback<ParseObject>() {
  		  public void done(ParseObject nexcellData, ParseException e) {
  		    if (e == null) {
  		    	nexcellData.put("Fellowship", f);
  		    	nexcellData.put("Service", s);
  		    	nexcellData.put("College", c);
  		    	nexcellData.put("NewComer", n);
  		    	nexcellData.put("saveBy", userName);
  		    	nexcellData.saveInBackground();
  		    }
  		  }
    	});
	}

    static public void deleteData(String nexcell, DateTime date, Context actv) {

        ParseObject obj = getNexcellData(nexcell, date, actv).get(0);
        obj.deleteInBackground();
    }
	
	static public void saveData(int f, int s, int c, int n, DateTime date, String nexcell, String userName, Context actv) {

            ParseObject data = new ParseObject("Attendance");
            data.put("Fellowship", f);
            data.put("Service", s);
            data.put("College", c);
            data.put("NewComer", n);
            data.put("Date", date.toDate());
            data.put("Nexcell", nexcell);
            data.put("saveBy", userName);

            data.saveInBackground();
	}
}
