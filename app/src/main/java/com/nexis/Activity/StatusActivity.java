package com.nexis.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.nexis.Constants;
import com.nexis.Data;
import com.nexis.NexisApplication;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.SendMailAsync;
import com.nexis.UIDialog;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatusActivity extends ActionBarActivity {

    private Toolbar mToolbar;
    private DateTime date;
    private List<String> nexcellList;
    private List<Integer> statusList;

    boolean counsVal = NexisApplication.getCouns();
    boolean ESMVal = NexisApplication.getESM();
    boolean devVal = NexisApplication.getDev();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        ParseOperation.refreshAttendanceLocalData(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        nexcellList = new ArrayList<>();
        statusList = new ArrayList<>();

        setupNexcell();

        mToolbar.setTitle("Real-Time Status " + date.toString("MMM-dd"));

        final List<String> missingList = new ArrayList<>();

        for(int i = 0; i < statusList.size(); i++)
        {
            if (statusList.get(i) == 0) missingList.add(nexcellList.get(i));
        }

        Button pushallButton = (Button)findViewById(R.id.pushAllButton);
        pushallButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   sendPush(missingList);
               }
           }
        );

        List<StatusListItem> sList = new ArrayList<>();

        for (int i = 0; i < nexcellList.size(); i++) sList.add(new StatusListItem(nexcellList.get(i), statusList.get(i)));

        StatusListAdapter adapter = new StatusListAdapter(this, sList);

        ListView lv = (ListView) findViewById(R.id.statusList);
        lv.setAdapter(adapter);
    }

    public void setupNexcell()
    {
        date = new DateTime(DateTimeZone.UTC);
        if (date.getDayOfWeek() - 1 < DateTimeConstants.FRIDAY) date = date.minusWeeks(1);
        date = date.withDayOfWeek(DateTimeConstants.FRIDAY);
        date = date.withTimeAtStartOfDay();

        List<ParseObject> nexcellObject = ParseOperation.getNexcellData(null, date, this);
        List<String> dataNexcell = new ArrayList<>();

        for (ParseObject x: nexcellObject) dataNexcell.add((String)x.get("Nexcell"));

        for (int i = 0; i < Data.NEXCELL_ACTIVE_LIST.size(); i++)
        {
            String nexcell = Data.NEXCELL_ACTIVE_LIST.get(i);

            nexcellList.add(nexcell);

            if (dataNexcell.contains(nexcell)) statusList.add(1);
            else statusList.add(0);
        }
    }

    private void sendEmail(final String nexcell)
    {
        if (!(ESMVal||counsVal||devVal)){
            Toast.makeText(StatusActivity.this, "Invalid access level.", Toast.LENGTH_LONG).show();
            return;
        }
        UIDialog.onCreateActionDialog(this, "Confirm", "Are you sure to send email notification to nexcell " + nexcell + "?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                SendMailAsync sendMail = new SendMailAsync(getApplicationContext());

                String toRecipients = ParseOperation.getSubmitDataRecipient(nexcell, getApplicationContext());
                String ccRecipients = Constants.SYSTEM_GMAIL;

                sendMail.execute("SUBMIT ATTENDANCE ASAP", "Please submit the attendance for " + date.toString("YYYY-MM-dd"), toRecipients, ccRecipients, "");
            }
        });
    }

    private void sendPush (List<String> nexcell)
    {
        ParsePush push = new ParsePush();

        ParseQuery pushQuery = ParseInstallation.getQuery();
        pushQuery.whereContainedIn("channels", nexcell);
        push.setQuery(pushQuery);

        push.setMessage("Your nexcell is still missing attendance. Please submit attendance asap!");
        push.sendInBackground();

        Toast.makeText(this, "Notification is sent to " + nexcell.size() + " nexcells", Toast.LENGTH_SHORT).show();
    }

    private class StatusListItem {
        public String name;
        public int status;

        public StatusListItem(String n, int s) {
            name = n;
            status = s;
        }
    }

    private class StatusListAdapter extends ArrayAdapter<StatusListItem> {

        public StatusListAdapter(Context context, List<StatusListItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final StatusListItem c = getItem(position);

            ViewHolder holder;

            if (convertView == null) {

                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_status, parent, false);

                holder = new ViewHolder(convertView);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.nexcellTextView.setText(c.name + ":");

            if (c.status == 1)
            {
                holder.statusTextView.setText("Submitted");
                holder.statusTextView.setTextColor(Color.rgb(10, 126, 10));
            }
            else
            {
                holder.statusTextView.setText("Missing");
                holder.statusTextView.setTextColor(Color.RED);
            }

            holder.emailButton.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      sendEmail(c.name);
                  }
              }
            );

            holder.pushButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (c.status != 1) sendPush(Arrays.asList(c.name));
                        else
                        {
                            UIDialog.onCreateActionDialog(getContext(), "Confirm",
                                    "Nexcell " + c.name + " has already submitted attendance, are you sure to send push notification?", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            sendPush(Arrays.asList(c.name));
                                        }
                                    }
                            );
                        }
                    }
                }
            );
            return convertView;
        }

        public class ViewHolder {
            public TextView nexcellTextView;
            public TextView statusTextView;
            public ButtonRectangle emailButton;
            public ButtonRectangle pushButton;

            public ViewHolder(View itemView) {

                nexcellTextView = (TextView) itemView.findViewById(R.id.nexcellName);
                statusTextView = (TextView) itemView.findViewById(R.id.statusName);
                emailButton = (ButtonRectangle) itemView.findViewById(R.id.emailNotiButton);
                pushButton = (ButtonRectangle) itemView.findViewById(R.id.pushNotiButton);

            }
        }
    }
}
