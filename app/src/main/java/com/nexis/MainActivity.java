package com.nexis;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

//import nexis.FragmentUpload.OnFragmentUploadSelectedListener;

import com.nexis.R;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener, FragmentUpload.OnFragmentUploadSelectedListener{

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	private static String userName, userNexcell, userEmail;
	private static int userAuthLevel;

	List<ParseObject> nexcellObject;

	HashMap<String, String> userIdMap = new HashMap<String, String>();
	List<String> tempUserIDList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		mViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount() - 1);

		// When swiping between different sections, select the corresponding
		
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		//Current user info
		ParseUser user = ParseUser.getCurrentUser();
		userName = user.getUsername();
		userEmail = (String)user.get("email");
		userNexcell = (String)user.get("Nexcell");
		
		try
		{
			ParseQuery<ParseObject> userQuery = ParseQuery.getQuery("UserLevelMap");
			userQuery.whereEqualTo("username", userName);
			List<ParseObject> obj = userQuery.find();
			
			userAuthLevel = obj.get(0).getInt("level");
		}
		catch (ParseException e)
		{
		}
		
		nexcellObject = ParseOperation.getNexcellList(false, this);
		Constants.initializeNexcell(nexcellObject);
		
		nexcellObject = ParseOperation.getAuthorLevel(this);
		Constants.initializeUserLevel(nexcellObject);
		
		// Subscribe the Broadcast push channel
		ParsePush.subscribeInBackground("");
		ParsePush.subscribeInBackground(userNexcell);

		ParseInstallation.getCurrentInstallation().put("lastSignIn", userName);
		ParseInstallation.getCurrentInstallation().saveInBackground();

		//Hide keyboard until user actually touches the edittext view
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		getOverflowMenu();
	}

	private void getOverflowMenu() {

	     try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		if (userAuthLevel < Constants.ADMIN_LEVEL) menu.setGroupVisible(R.id.groupAdmin, false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		Dialog d;
		
		int id = item.getItemId();
		switch (id)
		{
			case R.id.menu_logout:
				UIDialog.onCreateActionDialog(this, "Logout", "Are you sure you want to logout?", logoutListener);
				break;
				
			case R.id.ChangeAuthorLevel:
				List<ParseObject> nexcellObject = ParseOperation.getUserLevelList(1, this);

				for(ParseObject x: nexcellObject) userIdMap.put(x.get("username").toString(), x.getObjectId());
				d = changeLevelDialog(nexcellObject);
				
				d.show();
				break;
				
			case R.id.SendReport:
				UIDialog.onCreateActionDialog(this, "Send Report", "Are you sure you want to send weekly report?", sendReportListener);
				break;

			case R.id.checkStatus:
				checkStatusDialog();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			if (position == 0)
			{
				return FragmentUpload.newInstance();	
			}
			else if (position == 1)
			{
				return FragmentHData.newInstance();
			}
			else
			{
				return FragmentNewComer.newInstance();
			}
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}
	
	
	public void onUploadAttendance(DateTime date, int f, int s, int c)
	{
		FragmentHData statFrag = (FragmentHData) getSupportFragmentManager().findFragmentById(R.layout.fragment_hdata);
	}
	
	private DialogInterface.OnClickListener logoutListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int id) {
			 ParsePush.unsubscribeInBackground(userNexcell);

	   		 ParseInstallation.getCurrentInstallation().put("lastSignIn", "");
	   		 ParseInstallation.getCurrentInstallation().saveInBackground();

	       	 ParseUser.logOut();

	       	 endCurrentActivity();

	       	 Intent i = new Intent(getApplicationContext(), LoginActivity.class);
	       	 startActivity(i);
		}
	};

	private DialogInterface.OnClickListener sendReportListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int id) {
			sendWeeklyReport();
		}
	}; 
	
	private Dialog changeLevelDialog(List<ParseObject> userObject)
	{
		final List<String> userList = new ArrayList<String>();
		for(ParseObject x: userObject) userList.add((String) x.get("username") + " (" +  x.get("level") + ")");
		
		final List<Integer> mSelectedItems = new ArrayList<Integer>();
		final CharSequence[] nList = userList.toArray(new CharSequence[userList.size()]);
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle("Select User Name:");
    	
    	builder.setMultiChoiceItems(nList, null, new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    mSelectedItems.add(which);
                } else if (mSelectedItems.contains(which)) {
                    // Else, if the item is already in the array, remove it 
                    mSelectedItems.remove(Integer.valueOf(which));
                }
			}
		});
    	
    	// Set the action buttons
        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked Next, so save the mSelectedItems results somewhere
                // or return them to the component that opened the dialog
            	tempSaveUserID(userList, mSelectedItems);
            	
            	userLevel(builder, Constants.USER_LEVEL_LIST);
            	
            	builder.show();
            }
        });
    	    	
        return builder.create();
	}
	
	private void userLevel(AlertDialog.Builder builder, List<String> userLevelList)
	{
		final List<Integer> mSelectedItems = Arrays.asList(1);
		
		final CharSequence[] nList = userLevelList.toArray(new CharSequence[userLevelList.size()]);
		
		builder.setTitle("Select Authorized Level:");
		builder.setSingleChoiceItems(nList, 0, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mSelectedItems.set(0, which+1);
			}
		});
    	
    	// Set the action buttons
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            	updateUserLevel(mSelectedItems.get(0));
            	
            	Toast.makeText(MainActivity.this, "Authorized Level Changed", Toast.LENGTH_LONG).show();
            }
        });
	}
	
	private void checkStatusDialog()
	{
		FragmentUpload uploadFrag = (FragmentUpload) mSectionsPagerAdapter.getItem(0);
		DateTime date = uploadFrag.getDateTime();
		
		List<ParseObject> nexcellObject = ParseOperation.getNexcellData(null, date, this);
		final CharSequence[] nList = new CharSequence[Constants.NEXCELL_LIST.size()];
		
		List<String> dataNexcell = new ArrayList<String>();
		final List<String> missingNexcell = new ArrayList<String>();
		
		for (ParseObject x: nexcellObject) dataNexcell.add((String)x.get("Nexcell"));
		
		for (int i = 0; i < Constants.NEXCELL_LIST.size(); i++)
		{
			String nexcell = Constants.NEXCELL_LIST.get(i);
			
			if (dataNexcell.contains(nexcell)) nList[i] = nexcell + " : Submitted";
			else 
			{
				nList[i] = nexcell + " : Missing";
				missingNexcell.add(nexcell);
			}
		}

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle("Attendance Status: " + date.getYear() + "-" + date.getMonthOfYear() + "-" + date.getDayOfMonth());
    	builder.setItems(nList, null);
    	
    	// Set the action buttons
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        
        builder.setNegativeButton("Send Reminder", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog d =  builder.create();
        d.show();
        
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener()
        {            
            @Override
            public void onClick(View v)
            {
            	ArrayList<String> channels = new ArrayList<String>();
            	for(String nexcell: missingNexcell) channels.add(nexcell);
            	 
            	ParsePush push = new ParsePush();
            	push.setChannels(channels); // Notice we use setChannels not setChannel
            	push.setMessage("*REMINDER*: We have not receive your submission. Please submit attendance asap!");
            	push.sendInBackground();
            	
            	Toast.makeText(MainActivity.this, "Notification is sent to " + channels.size() + " groups", Toast.LENGTH_SHORT).show();
            }
       });
	}
	
	private void tempSaveUserID(List<String> userList, List<Integer> mSelectedItems)
	{
 		for (Integer x: mSelectedItems) 
 		{
 			tempUserIDList.add(userIdMap.get(userList.get(x).split(Pattern.quote("("))[0].trim()));
 		}
	}
	
	private void updateUserLevel(int level)
	{
		for(String x: tempUserIDList) 
    	{
    		ParseOperation.updateUser(x, level, this);
    	}
	}
	
	private void sendWeeklyReport()
	{
		String today = new DateTime().toString("yyyy-MM-dd");
		
		String filePath = this.getFilesDir().getPath().toString() +  "/Nexis Attendance " + today + ".xls";
		
		String toRecipients = ParseOperation.getMultilevelUserEmail(2, null, "G", this);
		String ccRecipients = null;
		
		List<String> nexcellTitles = new ArrayList<String>(Constants.NEXCELL_LIST);
		nexcellTitles.addAll(Constants.NEXCELL_CATEGORY_LIST);
		
		List<DateTime> dateList = new ArrayList<DateTime>();
		List<List<Integer>> mainList = new ArrayList<List<Integer>>();

		parseData(dateList, mainList, Constants.CATEGORY_LIST);	
		
		if (mainList.size() == 0)
		{
			UIDialog.onCreateInvalidDialog(this, "No data are found! Report cannot be generated");
			return;
		}
		
		Boolean excel = Excel.createWeeklyReport(this, filePath, nexcellTitles, Constants.CATEGORY_LIST, dateList, mainList);
		if (!excel) return;
		
		sendMailAsync sendMail = new sendMailAsync(this);
		sendMail.execute("Nexis Weekly Attendance Report", "Nexis Weekly Attendance Report for " + today , toRecipients, ccRecipients, filePath);
	}
	
	private void parseData(List<DateTime> dateList, List<List<Integer>> mainList, List<String> categoryList)
	{
		List<ParseObject> nexcellObject = ParseOperation.getNexcellData(null, null, this);
		
		int startRow = 0;
		
		while(startRow < nexcellObject.size() && !Constants.NEXCELL_LIST.contains(nexcellObject.get(startRow).get("Nexcell"))) startRow++;
		
		if (startRow == nexcellObject.size()) return;
		
		DateTime rowDate = new DateTime((Date)nexcellObject.get(startRow).get("Date"), DateTimeZone.UTC);
		
		List<Integer> row = new ArrayList<Integer>();
		List<Integer> hsData = Arrays.asList(0, 0, 0, 0);
		List<Integer> uniData = Arrays.asList(0, 0, 0, 0);
		List<Integer> nexisData = Arrays.asList(0, 0, 0, 0);
		
		int nexcellCount = 0;
		
		for(int i = startRow; i < nexcellObject.size(); i++)
    	{	
			DateTime currentDate = new DateTime((Date)nexcellObject.get(i).get("Date"), DateTimeZone.UTC);
			String currentNexcell = (String)nexcellObject.get(i).get("Nexcell");
			
			if (!Constants.NEXCELL_LIST.contains(currentNexcell)) continue;
			
			if (!currentDate.equals(rowDate))
			{
				while (nexcellCount != Constants.NEXCELL_LIST.size())
				{
					row.addAll(Arrays.asList(0, 0, 0, 0));
					nexcellCount++;
				}
				
				for(int j = 0; j < nexisData.size(); j++) nexisData.set(j, hsData.get(j) + uniData.get(j));
				
				//Add data to the main list
				dateList.add(rowDate);
				row.addAll(hsData);
				row.addAll(uniData);
				row.addAll(nexisData);
				mainList.add(row);
				
				//clear containers and update variable
				rowDate = currentDate;
				row = new ArrayList<Integer>();
				hsData = Arrays.asList(0, 0, 0, 0);
				uniData = Arrays.asList(0, 0, 0, 0);
				
				nexcellCount = 0;
			}
			
			while (!currentNexcell.equals(Constants.NEXCELL_LIST.get(nexcellCount)))
			{
				row.addAll(Arrays.asList(0, 0, 0, 0));
				nexcellCount++;
			}
			
			for(int j = 0; j < categoryList.size(); j++)
			{
				int num = nexcellObject.get(i).getInt(categoryList.get(j));
				
				row.add(num);
				
				List<Integer> tempData;
				
				if (Constants.NEXCELL_MAP.get(currentNexcell).equals("HighSchool")) tempData = hsData;
				else tempData = uniData;
				
				int newTotal = tempData.get(j) + num;
				tempData.set(j, newTotal);
			}
			
			nexcellCount++;
    	}
		
		while (nexcellCount != Constants.NEXCELL_LIST.size())
		{
			row.addAll(Arrays.asList(0, 0, 0, 0));
			nexcellCount++;
		}
		
		for(int j = 0; j < nexisData.size(); j++) nexisData.set(j, hsData.get(j) + uniData.get(j));
		
		//Add the last row to the main list
		dateList.add(rowDate);
		row.addAll(hsData);
		row.addAll(uniData);
		row.addAll(nexisData);
		mainList.add(row);
	}
	
	public String getUserNexcell()
	{
		return userNexcell; 
	}
	
	public String getUserEmail()
	{
		return userEmail; 
	}
	
	public String getUserName()
	{
		return userName; 
	}
	
	public int getUserAuthlevel()
	{
		return userAuthLevel; 
	}
	
	private void endCurrentActivity()
	{
		this.finish();
	}
}
