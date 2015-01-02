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
import com.parse.ParseUser;

import android.app.Activity;
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
	
	
	static public List<ParseUser> getUserList(int level, String nexcell, String lessThan, Context actv)
	{
		List<ParseObject> nexcellObject = new ArrayList<ParseObject>();
		List<ParseUser> userObject = new ArrayList<ParseUser>();
		List<String> userNameList = new ArrayList<String>();
		
		try
        {
        	ParseQuery<ParseObject> query = ParseQuery.getQuery("UserLevelMap");
        	
        	if (lessThan == "L") query.whereLessThanOrEqualTo("level", level);
        	else if (lessThan == "G") query.whereGreaterThanOrEqualTo("level", level);
        	else query.whereEqualTo("level", level);
        	
        	nexcellObject = query.find();
        	
        	for(ParseObject x: nexcellObject) userNameList.add((String) x.get("username"));
        	
        	ParseQuery<ParseUser> userquery = ParseUser.getQuery();
        	userquery.whereContainedIn("username", userNameList);
        	
        	if(nexcell != null) userquery.whereEqualTo("Nexcell", nexcell);
        	
        	userquery.orderByAscending("username");
        	
        	userObject = userquery.find();
        	
        }
        catch (ParseException e)
        {
        	UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }

    	return userObject;
	}
	
	static public String getUserEmail(int level, String nexcell, Context actv)
	{
		List<String> emails = new ArrayList<String>();
		List<ParseUser> users = getUserList(level, nexcell, "", actv);
		
		for(ParseUser x: users) emails.add(x.get("email").toString());
		
		String recipients = StringUtils.join(emails, ", ");
		
		return recipients;
	}
	
	static public String getMultilevelUserEmail(int level, String nexcell, String lessThan, Context actv)
	{
		List<String> emails = new ArrayList<String>();
		List<ParseUser> users = getUserList(level, nexcell, lessThan, actv);
		
		for(ParseUser x: users) emails.add(x.get("email").toString());
		
		String recipients = StringUtils.join(emails, ", ");
		
		return recipients;
	}
	
	static public List<ParseObject> getUserLevelList(int level, Context actv)
	{
		List<ParseObject> nexcellObject = new ArrayList<ParseObject>();
		
		try
        {
        	ParseQuery<ParseObject> query = ParseQuery.getQuery("UserLevelMap");
        	query.whereGreaterThanOrEqualTo("level", level);
        	query.orderByAscending("level");
        	query.addAscendingOrder("username");
        	
        	nexcellObject = query.find();

        }
        catch (ParseException e)
        {
        	UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }
		
    	return nexcellObject;
	}
	
	static public List<ParseObject> getAuthorLevel(Context actv)
	{
		List<ParseObject> nexcellObject = new ArrayList<ParseObject>();
		
		try
        {
        	ParseQuery<ParseObject> query = ParseQuery.getQuery("AuthorizedLevel");
        	query.orderByAscending("level");
        	query.selectKeys(Arrays.asList("levelName"));
        			
        	nexcellObject = query.find();
        }
        catch (ParseException e)
        {
        	UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
        }
		
		return nexcellObject;
	}
	

	static public List<ParseObject> getNexcellList(boolean nameOnly, Context actv) {
		
        List<ParseObject> nexcellObject = new ArrayList<ParseObject>();
        
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
		
        List<ParseObject> nexcellObject = new ArrayList<ParseObject>();
        
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
		
    	ParseQuery<ParseObject> query = ParseQuery.getQuery("UserLevelMap");
    	
    	query.getInBackground(objectID, new GetCallback<ParseObject>() {
  		  public void done(ParseObject userData, ParseException e) {
  		    if (e == null) {
  		    	userData.put("level", level);
  		    	userData.saveInBackground();
  		    }
  		  }
    	}); 
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
	
	static public void saveData(int f, int s, int c, int n, DateTime date, String nexcell, String userName, Context actv) {
		
		DateTime lastWeek = date.minusWeeks(1);
		
		List<ParseObject> nexcellObject;
		
		try
		{
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Attendance");
			query.whereEqualTo("Nexcell", nexcell);
			query.whereEqualTo("Date", lastWeek.toDate());
		
			nexcellObject = query.find();
		}
		catch(ParseException e)
		{
			UIDialog.onCreateErrorDialog(actv, e + ". Parse Query");
		}

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
