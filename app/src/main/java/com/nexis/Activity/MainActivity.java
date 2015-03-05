package com.nexis.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.ViewConfiguration;
import android.widget.Toast;
import com.nexis.Constants;
import com.nexis.ExcelReports.genWeeklyReport;
import com.nexis.Fragments.FragmentStat;
import com.nexis.Fragments.FragmentNewComer;
import com.nexis.Fragments.FragmentAttendance;
import com.nexis.NavigationDrawer.NavigationDrawerCallbacks;
import com.nexis.NavigationDrawer.NavigationDrawerFragment;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.SendMailAsync;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import org.joda.time.DateTime;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;


public class MainActivity extends ActionBarActivity implements NavigationDrawerCallbacks {

    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private static String userName, userNexcell, userEmail;
    private static int userAuthLevel;

    List<ParseObject> nexcellObject;

    HashMap<String, String> userIdMap = new HashMap<String, String>();
    List<String> tempUserIDList = new ArrayList<String>();

    List<Fragment> fragments = new ArrayList<Fragment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);

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

        getOverflowMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position)
    {
        getSupportActionBar().setTitle(Constants.FRAGMENT_NAME.get(position));

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragments.size() < 3)
        {
            fragments.add(FragmentAttendance.newInstance());
            fragments.add(FragmentStat.newInstance());
            fragments.add(FragmentNewComer.newInstance());
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragments.get(position))
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
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

    /*private void checkStatusDialog()
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
        }*/

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

        String toRecipients = ParseOperation.getWeeklyReportRecipient(this);
        String ccRecipients = Constants.SYSTEM_GMAIL;

        List<String> nexcellTitles = new ArrayList<String>(Constants.NEXCELL_LIST);
        nexcellTitles.addAll(Constants.NEXCELL_CATEGORY_LIST);

        genWeeklyReport report = new genWeeklyReport(this, filePath);
        report.genReport();

        SendMailAsync sendMail = new SendMailAsync(this);
        sendMail.execute("Nexis Weekly Attendance Report", "Nexis Weekly Attendance Report for " + today , toRecipients, ccRecipients, filePath);
    }

    public void setToolbarElevation(int elevation)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mToolbar.setElevation(elevation);
        }
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
