package com.nexis.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.utils.Utils;
import com.nexis.Activity.StatusActivity;
import com.nexis.Constants;
import com.nexis.Data;
import com.nexis.ExcelReports.WeeklyReport;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.SendMailAsync;
import com.nexis.UIDialog;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class FragmentAdmin extends DialogFragment implements AdapterView.OnItemClickListener {

    public static FragmentAdmin newInstance() {
        FragmentAdmin fragment = new FragmentAdmin();
        Bundle args = new Bundle();
        args.putInt("Admin", 4);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentAdmin() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_admin, container, false);

        // initialize the utilities
        Utils.init(getResources());

        ArrayList<AdminListItem> objects = new ArrayList<>();

        objects.add(new AdminListItem("Real-Time Status", "Monitor the attendance status for each nexcell"));
        objects.add(new AdminListItem("User Management", "Modify user information and access level"));
        objects.add(new AdminListItem("Nexcell Management", "Nexis group distribution"));
        objects.add(new AdminListItem("Send Report", "Send out weekly report to committee and counsellors"));

        GraphListAdapter adapter = new GraphListAdapter(getActivity(), objects);

        ListView lv = (ListView) rootView.findViewById(R.id.adminList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(this);

        return rootView;
    }

    public void onItemClick(AdapterView<?> av, View v, int pos, long arg3) {

        Intent i;

        switch (pos) {
            case 0:
                i = new Intent(getActivity(), StatusActivity.class);
                startActivity(i);
                break;
            case 3:
                UIDialog.onCreateActionDialog(getActivity(), "Send Report", "Are you sure you want to send weekly report?", sendReportListener);
                break;
        }
    }

    private class AdminListItem {
        String name;
        String desc;

        public AdminListItem(String n, String d) {
            name = n;
            desc = d;
        }
    }

    private class GraphListAdapter extends ArrayAdapter<AdminListItem> {

        public GraphListAdapter(Context context, List<AdminListItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            AdminListItem c = getItem(position);

            ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_desc, null);
                holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
                holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvName.setText(c.name);
            holder.tvDesc.setText(c.desc);

            return convertView;
        }

        private class ViewHolder {

            TextView tvName, tvDesc;
        }
    }

    private DialogInterface.OnClickListener sendReportListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int id) {
            ParseOperation.refreshAttendanceLocalData(getActivity());
            sendWeeklyReport();
        }
    };

    private void sendWeeklyReport()
    {
        String today = new DateTime().toString("yyyy-MM-dd");

        String filePath = getActivity().getFilesDir().getPath() +  "/Nexis Attendance " + today + ".xls";

        String toRecipients = ParseOperation.getWeeklyReportRecipient(getActivity());
        String ccRecipients = Constants.SYSTEM_GMAIL;

        List<String> nexcellTitles = new ArrayList<>(Data.NEXCELL_LIST);
        nexcellTitles.addAll(Constants.NEXCELL_CATEGORY_LIST);

        WeeklyReport report = new WeeklyReport(getActivity(), filePath);
        report.genReport();

        SendMailAsync sendMail = new SendMailAsync(getActivity());
        sendMail.execute("Nexis Weekly Attendance Report", "Nexis Weekly Attendance Report for " + today , toRecipients, ccRecipients, filePath);
    }
}
