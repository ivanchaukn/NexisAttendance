package com.nexis;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.parse.ParseObject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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

	public static final String HS_STRING = "HighSchool";
	public static final String UNI_STRING = "University";
	public static final String NEXIS_STRING = "Nexis";
	
	public static HashMap<String, String> NEXCELL_STAGE;
    public static HashMap<String, String> NEXCELL_PARENT;
    public static HashMap<String, DateTime> NEXCELL_MERGE_DATE;
	public static HashMap<String, String> USERID_MAP;

	public static List<String> NEXCELL_LIST;
    public static List<String> NEXCELL_ACTIVE_LIST;

	public static List<String> USER_LEVEL_LIST;

    public static List<String> FRAGMENT_NAME = Arrays.asList("Attendance","Statistics","New Comer Form", "System Administration");

	public static List<String> CATEGORY_LIST = Arrays.asList("Fellowship", "Service", "College", "NewComer");
	public static List<String> NEXCELL_CATEGORY_LIST = Arrays.asList(HS_STRING, UNI_STRING, NEXIS_STRING);

	public static List<Integer> BLUE_COLOR_TEMPLATE = Arrays.asList(Color.rgb(0, 67, 87), Color.rgb(118, 174, 175), Color.rgb(136, 180, 187), Color.rgb(148, 212, 212), Color.rgb(159, 249, 249));
	public static List<Integer> GREEN_COLOR_TEMPLATE = Arrays.asList(Color.rgb(48, 111, 44), Color.rgb(55, 146, 49), Color.rgb(115, 175, 111), Color.rgb(129, 161, 127), Color.rgb(165, 218, 161));
	public static List<Integer> RED_COLOR_TEMPLATE = Arrays.asList(Color.rgb(208, 44, 44), Color.rgb(210, 63, 63), Color.rgb(213, 95, 95), Color.rgb(222, 127, 127), Color.rgb(240, 156, 156));

	public static void initializeNexcell(List<ParseObject> nexcellObject)
	{
		NEXCELL_STAGE = new HashMap<>();
        NEXCELL_PARENT = new HashMap<>();
        NEXCELL_MERGE_DATE = new HashMap<>();
		NEXCELL_LIST = new ArrayList<>();
        NEXCELL_ACTIVE_LIST = new ArrayList<>();

		for(ParseObject x: nexcellObject)
		{
			String nexcell = x.get("Name").toString();
			String stage = x.get("Stage").toString();
            String parentGroup = x.get("Parent").toString();
            DateTime mergeDate = new DateTime(x.get("Merge_Date"), DateTimeZone.UTC);

            NEXCELL_STAGE.put(nexcell, stage);
            NEXCELL_PARENT.put(nexcell, parentGroup);
            NEXCELL_MERGE_DATE.put(nexcell, mergeDate);
            NEXCELL_LIST.add(nexcell);

            if (parentGroup.equals("")) NEXCELL_ACTIVE_LIST.add(nexcell);
		}
	}
	
	public static void initializeUserLevel(List<ParseObject> nexcellObject)
	{
		USER_LEVEL_LIST = new ArrayList<>();
		 
		for(ParseObject x: nexcellObject) 
		{
			String level = x.get("levelName").toString();
			USER_LEVEL_LIST.add(level);
		}
	}
}
