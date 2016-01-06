package com.nexis.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.nexis.Constants;
import com.nexis.Data;
import com.nexis.ExcelReports.ContactForm;
import com.nexis.Fragments.FragmentAdmin;
import com.nexis.Fragments.FragmentRegistration;
import com.nexis.Fragments.AttendancePackage.FragmentAttendance;
import com.nexis.Fragments.FragmentStat;
import com.nexis.GeneralOperation;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends ActionBarActivity implements NavigationDrawerCallbacks, NavigationFooterCallbacks {

    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private static String userName, userFirstName, userLastName, userInitial, userNexcell, userEmail, fullName;

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

            if (GeneralOperation.checkNetworkConnection(this))
            {
                // Subscribe the Broadcast push channel
                ParsePush.subscribeInBackground("");
                ParsePush.subscribeInBackground(userNexcell);

                ParseInstallation.getCurrentInstallation().put("lastSignIn", userName);
                ParseInstallation.getCurrentInstallation().saveInBackground();
            }
        }
        catch (Exception e)
        {
            UIDialog.onCreateErrorDialog(this, "MainActivity error");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (GeneralOperation.hasRole(this)) getMenuInflater().inflate(R.menu.main_menu, menu);
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
                View vi = this.getLayoutInflater().inflate(R.layout.listview_dialog, null);
                ListView lv = (ListView) vi.findViewById(R.id.dialogList);

                final AlertDialog d = UIDialog.onCreateListViewDialog(this, "Download Contact List", lv, false);
                d.show();

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int pos, long id) {
                        d.dismiss();

                        String recipient = "";
                        switch (pos) {
                            case 0:
                                recipient = userEmail + "," + Constants.SYSTEM_GMAIL;
                                break;
                            case 1:
                                recipient = Data.getNexcellLeadersRecipient(userNexcell, getApplicationContext());

                        }
                        contactReportAsync crp = new contactReportAsync();
                        crp.execute(userNexcell, recipient);
                    }
                });


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, int prevPosition)
    {
        //TODO: fix graphs
        if (position == 1)
        {
            UIDialog.onCreateMsgDialog(this, "Not Available", "Statistics tab is currently not available");
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentfrag = fragmentManager.findFragmentById(R.id.container);

        if (currentfrag != null)
        {
            if (position == prevPosition) return;
            fragmentManager.beginTransaction().remove(currentfrag).commit();
        }

        getSupportActionBar().setTitle(Constants.FRAGMENT_NAME.get(position));

        if (fragments.isEmpty())
        {
            fragments.add(FragmentAttendance.newInstance());
            fragments.add(FragmentStat.newInstance());
            fragments.add(FragmentRegistration.newInstance());
            fragments.add(FragmentAdmin.newInstance());
        }

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

    private void setupUser()
    {
        ParseUser user = ParseUser.getCurrentUser();
        userName = user.getUsername();
        userEmail = user.getEmail();
        userFirstName = (String) user.get("firstName");
        userLastName = (String) user.get("lastName");
        userInitial = (String) user.get("initial");
        userNexcell = (String) user.get("nexcell");

        Data.saveUserLevels(user, this);
    }

    public class contactReportAsync extends AsyncTask<String, Void, String[]>
    {
        String filePath = getApplication().getFilesDir().getPath() +  "/Nexcell Contact List" + ".xls";

        protected void onPreExecute () {
            Toast.makeText(MainActivity.this, "Creating Contact List...", Toast.LENGTH_LONG).show();
        }

        protected String[] doInBackground(String... info) {

            List<ParseObject> userList = ParseOperation.getUserList(info[0], Arrays.asList(false, false, false, false, false), false, getApplication());

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

    private void sendEmail(String filePath, String nexcell, String recipients) {
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

    public String getUserEmail()
    {
        return userEmail;
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
