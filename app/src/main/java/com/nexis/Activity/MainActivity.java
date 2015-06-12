package com.nexis.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;
import com.nexis.Constants;
import com.nexis.ExcelReports.genWeeklyReport;
import com.nexis.Fragments.FragmentAdmin;
import com.nexis.Fragments.FragmentNewComer;
import com.nexis.Fragments.AttendancePackage.FragmentAttendance;
import com.nexis.Fragments.FragmentStat;
import com.nexis.NavigationDrawer.NavigationDrawerCallbacks;
import com.nexis.NavigationDrawer.NavigationFooterCallbacks;
import com.nexis.NavigationDrawer.NavigationDrawerFragment;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.SendMailAsync;
import com.nexis.UIDialog;
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


public class MainActivity extends ActionBarActivity implements NavigationDrawerCallbacks, NavigationFooterCallbacks {

    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private static String userName, userFirstName, userLastName, userNexcell, userEmail;
    private static int userAuthLevel;

    List<ParseObject> nexcellObject;

    HashMap<String, String> userIdMap = new HashMap<>();
    List<String> tempUserIDList = new ArrayList<>();

    List<Fragment> fragments = new ArrayList<>();

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
        userFirstName = (String)user.get("firstName");
        userLastName = (String)user.get("lastName");
        userEmail = (String)user.get("email");
        userNexcell = (String)user.get("Nexcell");

        mNavigationDrawerFragment.updateProfile(userFirstName + " " + userLastName, userNexcell);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Dialog d;

        int id = item.getItemId();
        switch (id)
        {
            case R.id.ChangeAuthorLevel:
                List<ParseObject> nexcellObject = ParseOperation.getUserLevelList(1, this);

                for(ParseObject x: nexcellObject) userIdMap.put(x.get("username").toString(), x.getObjectId());
                d = changeLevelDialog(nexcellObject);

                d.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, int prevPosition)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentfrag = fragmentManager.findFragmentById(R.id.container);

        if (currentfrag != null)
        {
            if (position == prevPosition) return;
            fragmentManager.beginTransaction().remove(currentfrag).commit();
        }

        getSupportActionBar().setTitle(Constants.FRAGMENT_NAME.get(position));

        fragments.add(FragmentAttendance.newInstance());
        fragments.add(FragmentStat.newInstance());
        fragments.add(FragmentNewComer.newInstance());
        fragments.add(FragmentAdmin.newInstance());

        displayFragment(position);
    }

    @Override
    public void onNavigationFooterItemSelected(int position)
    {
        switch (position)
        {
            case 0:
                Intent i = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(i);
                break;

            case 1:
                UIDialog.onCreateActionDialog(this, "Logout", "Are you sure you want to exit?", logoutListener);
                break;
        }
    }

    private void displayFragment(int position)
    {
        getSupportFragmentManager().executePendingTransactions();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment frag = fragments.get(position);

        if (frag.isAdded())
        {
            ft.show(frag);
        }
        else
        {
            ft.add(R.id.container, frag);
        }

        for(int i = 0; i < fragments.size(); i++)
        {
            Fragment otherFrag = fragments.get(position);

            if (i != position && otherFrag.isAdded())
            {
                ft.hide(otherFrag);
            }
        }

        ft.commit();
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
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Dialog changeLevelDialog(List<ParseObject> userObject)
    {
        final List<String> userList = new ArrayList<>();
        for(ParseObject x: userObject) userList.add(x.get("username") + " (" +  x.get("level") + ")");

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

    public void setToolbarElevation(int elevation)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mToolbar.setElevation(elevation);
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
