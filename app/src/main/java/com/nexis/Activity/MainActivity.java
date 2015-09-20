package com.nexis.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nexis.Constants;
import com.nexis.Data;
import com.nexis.ExcelReports.ContactForm;
import com.nexis.Fragments.FragmentAdmin;
import com.nexis.Fragments.FragmentNewComer;
import com.nexis.Fragments.AttendancePackage.FragmentAttendance;
import com.nexis.Fragments.FragmentStat;
import com.nexis.NavigationDrawer.NavigationDrawerCallbacks;
import com.nexis.NavigationDrawer.NavigationFooterCallbacks;
import com.nexis.NavigationDrawer.NavigationDrawerFragment;
import com.nexis.NexisApplication;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends ActionBarActivity implements NavigationDrawerCallbacks, NavigationFooterCallbacks {

    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private static String userName, userFirstName, userLastName, userInitial, userNexcell, fullName;

    private ArrayMap<String, String> usernameMap;

    List<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try
        {
            setupUser();
            usernameMap = Data.getNexcellMemberNameMap(userNexcell, this);

            setContentView(R.layout.activity_main);
            mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_drawer);
            mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);

            if (userInitial.equals("")) fullName = userFirstName + " " + userLastName;
            else fullName = userFirstName + " " + userInitial + " " + userLastName;

            mNavigationDrawerFragment.updateProfile(fullName, Data.getNexcellLabel(userNexcell));

            // Subscribe the Broadcast push channel
            ParsePush.subscribeInBackground("");
            ParsePush.subscribeInBackground(userNexcell);

            ParseInstallation.getCurrentInstallation().put("lastSignIn", userName);
            ParseInstallation.getCurrentInstallation().saveInBackground();
        }
        catch (Exception e)
        {
            UIDialog.onCreateErrorDialog(this, "MainActivity error");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        List<Integer> levs = NexisApplication.getLevels();
        for (Integer x : levs) {
            if (x > 0) {
                getMenuInflater().inflate(R.menu.main_menu, menu);
                break;
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        switch (id) {
            case R.id.gen_contact:
                UIDialog.onCreateActionDialog(this, "Download Contact List", "Are you sure you want to send nexcell contact list?", sendContactListListener);
        }
        return super.onOptionsItemSelected(item);
    }

    private DialogInterface.OnClickListener sendContactListListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int id) {
            String toRep = ParseOperation.getNexcellLeadersRecipient(userNexcell, getApplicationContext());
            contactReportAsync crp = new contactReportAsync();
            crp.execute(userNexcell, toRep);
        }
    };


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
                UIDialog.onCreateMsgDialog(this, "Not Available", "Setting tab is currently not available");
                //Intent i = new Intent(getApplicationContext(), SettingActivity.class);
                //startActivity(i);
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

        if (frag.isAdded()) ft.show(frag);
        else ft.add(R.id.container, frag);

        for(int i = 0; i < fragments.size(); i++)
        {
            Fragment otherFrag = fragments.get(position);
            if (i != position && otherFrag.isAdded()) ft.hide(otherFrag);
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

    public void setToolbarElevation(int elevation)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mToolbar.setElevation(elevation);
        }
    }

    public void genContactReport(String nexcell, String toRep)
    {
        contactReportAsync crp = new contactReportAsync();
        crp.execute(nexcell, toRep);
    }

    public class contactReportAsync extends AsyncTask<String, Void, String[]>
    {
        String filePath;

        protected void onPreExecute () {
            filePath = getApplication().getFilesDir().getPath() +  "/New Comer" + ".xls";
        }

        protected String[] doInBackground(String... info) {

            List<ParseObject> userList = ParseOperation.getUserList(info[0], Arrays.asList(false, false, false, false, false), getApplication());

            ContactForm form = new ContactForm(getApplication(), filePath, userList);
            form.genReport();

            return info;
        }

        protected void onPostExecute(String[] info) {
            sendEmail(filePath, info[0], info[1]);
            Toast.makeText(getApplication(), "Contact List Sent", Toast.LENGTH_LONG).show();
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

    private void sendEmail(String filePath, String nexcell, String recipients)
    {
        String toRecipients = recipients;
        String ccRecipients = Constants.SYSTEM_GMAIL;

        DateTime currentTime = new DateTime();
        String currentDateTimeString = currentTime.toString("yyyy-MM-dd HH:mm:ss.SSS");

        String emailSubject = "Contact List for " + nexcell;
        if (nexcell == null) emailSubject = "Nexis Master Contact List";

        String emailBody = String.format("Attached file is the contact list as of %s", currentDateTimeString);

        SendMailAsync sendMail = new SendMailAsync(this);
        sendMail.execute(emailSubject, emailBody, toRecipients, ccRecipients, filePath);
    }

    private void setupUser()
    {
        try {
            ParseUser user = ParseUser.getCurrentUser();
            userName = user.getUsername();
            userFirstName = (String) user.get("firstName");
            userLastName = (String) user.get("lastName");
            userInitial = (String) user.get("initial");
            userNexcell = (String) user.get("nexcell");

            ParseQuery<ParseObject> userQuery = ParseQuery.getQuery("_User");
            userQuery.whereEqualTo("username", userName);
            List<ParseObject> obj = userQuery.find();

            NexisApplication.setMember(obj.get(0).getBoolean("member"));
            NexisApplication.setESM(obj.get(0).getBoolean("esm"));
            NexisApplication.setDev(obj.get(0).getBoolean("developer"));
            NexisApplication.setCommi(obj.get(0).getBoolean("committee"));
            NexisApplication.setCouns(obj.get(0).getBoolean("counsellor"));
        }
        catch(ParseException e)
        {
            UIDialog.onCreateMsgDialog(this, "Error", "User set up error");
        }
    }

    public String getUserNexcell()
    {
        return userNexcell;
    }

    public String getUserNexcellLabel()
    {
        return Data.getNexcellLabel(userNexcell);
    }

    public String getUserName()
    {
        return userName;
    }

    public void refreshUserMap()
    {
        usernameMap = Data.getNexcellMemberNameMap(userNexcell, this);
    }

    public ArrayMap<String, String> getNexcellUserMap()
    {
        return usernameMap;
    }

    public void setNexcellUserMap(ArrayMap<String, String> map)
    {
        usernameMap = map;
    }

    private void endCurrentActivity()
    {
        this.finish();
    }
}
