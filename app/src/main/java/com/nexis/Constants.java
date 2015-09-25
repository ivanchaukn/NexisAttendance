package com.nexis;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.parse.ParseObject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public final class Constants {
	
	private Constants() {
        // restrict instantiation
	}

    public static final int NUMBERPICKER_MAX = 50;
	public static final int NUMBERPICKER_MIN = 0;

	public static final String PARSE_APPLICATION_ID = "n4gVHiC7PcB6fAvCkfMKSsAEyUODifl7JL33w3xT";
	public static final String PARSE_CLEINT_KEY = "sag1RtZfVIVqJFhDbLTiHACtDPPCmBQbmdmOv670";

	public static final String SYSTEM_GMAIL = "nexisapplication@gmail.com";
	public static final String SENDGRID_USER_NAMAE = "nexisapplication";
	public static final String SENDGRID_PASSWORD = "(Nexis14!#";

	public static final String HS_STRING = "HighSchool";
	public static final String UNI_STRING = "University";
	public static final String NEXIS_STRING = "Nexis";

	public static final String FELLOWSHIP_STRING = "Fellowship";
	public static final String SERVICE_STRING = "Service";
	public static final String COLLEGE_STRING = "College";
	public static final String NEWCOMER_STRING = "NewComer";

	public static Date NEXIS_START_DATE;
	public static Date NEXIS_END_DATE;

	public static List<String> USER_LEVEL_LIST = Arrays.asList("member", "esm", "committee", "counsellor", "developer");

    public static List<String> FRAGMENT_NAME = Arrays.asList("Attendance","Statistics","Registration Form", "System Administration");

	public static List<String> CATEGORY_LIST = Arrays.asList(FELLOWSHIP_STRING, SERVICE_STRING, COLLEGE_STRING, NEWCOMER_STRING);
	public static List<String> NEXCELL_CATEGORY_LIST = Arrays.asList(HS_STRING, UNI_STRING, NEXIS_STRING);

	public static List<Integer> BLUE_COLOR_TEMPLATE = Arrays.asList(Color.rgb(0, 67, 87), Color.rgb(118, 174, 175), Color.rgb(136, 180, 187), Color.rgb(148, 212, 212), Color.rgb(159, 249, 249));
	public static List<Integer> GREEN_COLOR_TEMPLATE = Arrays.asList(Color.rgb(48, 111, 44), Color.rgb(55, 146, 49), Color.rgb(115, 175, 111), Color.rgb(129, 161, 127), Color.rgb(165, 218, 161));
	public static List<Integer> RED_COLOR_TEMPLATE = Arrays.asList(Color.rgb(208, 44, 44), Color.rgb(210, 63, 63), Color.rgb(213, 95, 95), Color.rgb(222, 127, 127), Color.rgb(240, 156, 156));

}