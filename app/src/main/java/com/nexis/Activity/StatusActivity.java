package com.nexis.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ButtonRectangle;
import com.nexis.Constants;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.SendMailAsync;
import com.nexis.UIDialog;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SendCallback;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StatusActivity extends ActionBarActivity {

    private Toolbar mToolbar;

    private DateTime date;
    private List<String> nexcellList;
    private List<Integer> statusList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        nexcellList = new ArrayList<>();
        statusList = new ArrayList<>();

        setupNexcell();

        mToolbar.setTitle("Real-Time Status " + date.toString("yyyy-mm-dd"));

        final List<String> missingList = new ArrayList<>();

        for(int i = 0; i < statusList.size(); i++)
        {
            if (statusList.get(i) == 0) missingList.add(nexcellList.get(i));
        }

        ButtonRectangle pushallButton = (ButtonRectangle)findViewById(R.id.pushAllButton);
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

        for (int i = 0; i < Constants.NEXCELL_LIST.size(); i++)
        {
            String nexcell = Constants.NEXCELL_LIST.get(i);

            if (Constants.NEXCELL_PARENT.get(nexcell).equals(""))
            {
                nexcellList.add(nexcell);

                if (dataNexcell.contains(nexcell)) statusList.add(1);
                else statusList.add(0);
            }
        }
    }

    private void sendEmail(final String nexcell)
    {
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
                        sendPush(Arrays.asList(c.name));
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
