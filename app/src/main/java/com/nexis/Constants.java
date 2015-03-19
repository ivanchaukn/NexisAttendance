package com.nexis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.parse.ParseObject;

public final class Constants {
	
	private Constants() {
        // restrict instantiation
	}

    public static final int MASTER_LEVEL = 7;
	public static final int DEV_LEVEL = 6;
	public static final int ADMIN_LEVEL = 5;
    public static final int COMM_LEVEL = 4;
	public static final int COUS_LEVEL = 3;
    public static final int ESM_LEVEL = 2;
	public static final int MEMBER_LEVEL = 1;

    public static final int NUMBERPICKER_MAX = 50;
    public static final int NUMBERPICKER_MIN = 0;

    public static final String SYSTEM_GMAIL = "nexisapplication@gmail.com";
	public static final String SENDGRID_USER_NAMAE = "nexisapplication";
	public static final String SENDGRID_PASSWORD = "(Nexis14!#";
	
	public static HashMap<String, String> NEXCELL_MAP;
	public static HashMap<String, String> USERID_MAP;
	
	public static List<String> NEXCELL_LIST;
	public static CharSequence[] NEXCELL_CHARLIST;
	
	public static List<String> USER_LEVEL_LIST;

    public static List<String> FRAGMENT_NAME = Arrays.asList("Attendance","Statistics","New Comer Form");

	public static List<String> CATEGORY_LIST = Arrays.asList("Fellowship", "Service", "College", "NewComer");
	public static List<String> NEXCELL_CATEGORY_LIST = Arrays.asList("HighSchool","University","Nexis");
	
	public static void initializeNexcell(List<ParseObject> nexcellObject)
	{
		NEXCELL_MAP = new HashMap<String, String>();
		NEXCELL_LIST = new ArrayList<String>();
		 
		for(ParseObject x: nexcellObject) 
		{
			String name = x.get("Name").toString();
			String stage = x.get("Stage").toString();
			
			NEXCELL_LIST.add(name);
			NEXCELL_MAP.put(name, stage);
		}
		
		NEXCELL_CHARLIST = NEXCELL_LIST.toArray(new CharSequence[NEXCELL_LIST.size()]);
	}
	
	public static void initializeUserLevel(List<ParseObject> nexcellObject)
	{
		USER_LEVEL_LIST = new ArrayList<String>();
		 
		for(ParseObject x: nexcellObject) 
		{
			String level = x.get("levelName").toString();
			USER_LEVEL_LIST.add(level);
		}
	}
}
