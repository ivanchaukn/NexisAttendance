package com.nexis.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mikephil.charting.utils.Utils;
import com.nexis.Activity.MainActivity;
import com.nexis.Activity.StatusActivity;
import com.nexis.Constants;
import com.nexis.Data;
import com.nexis.DescriptionList.DescListAdapter;
import com.nexis.DescriptionList.DescListItem;
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

        ArrayList<DescListItem> objects = new ArrayList<>();

        objects.add(new DescListItem("Real-Time Status", "Monitor the attendance status for each nexcell", null));
        objects.add(new DescListItem("User Management", "Modify user information and access level", null));
        objects.add(new DescListItem("Nexcell Management", "Nexis group distribution", null));
        objects.add(new DescListItem("Weekly Report", "Send out weekly report to committee and counsellors", null));
        objects.add(new DescListItem("Master Contact List", "Generate master contact list for Nexis", null));

        DescListAdapter adapter = new DescListAdapter(getActivity(), objects);

        ListView lv = (ListView) rootView.findViewById(R.id.adminList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(this);

        return rootView;
    }

    public void onItemClick(AdapterView<?> av, View v, int pos, long arg3) {

        Intent i;
        final AlertDialog d;

        final String selfEmail = ((MainActivity)getActivity()).getUserEmail() + "," + Constants.SYSTEM_GMAIL;
        final String ccEmail = ParseOperation.getCommCounsRecipient(getActivity());

        View vi = getActivity().getLayoutInflater().inflate(R.layout.listview_dialog, null);
        ListView lv = (ListView) vi.findViewById(R.id.dialogList);

        switch (pos) {
            case 0:
                i = new Intent(getActivity(), StatusActivity.class);
                startActivity(i);
                break;
            case 3:
                d = UIDialog.onCreateListViewDialog(getActivity(), "Send Weekly Report", lv, true);
                d.show();

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int pos, long id) {
                        d.dismiss();

                        String recipient = "";
                        switch (pos) {
                            case 0:
                                recipient = selfEmail;
                                break;
                            case 1:
                                recipient = ccEmail;

                        }

                        ParseOperation.refreshAttendanceLocalData(getActivity());

                        Toast.makeText(getActivity(), "Creating Report...", Toast.LENGTH_SHORT).show();
                        weeklyReportAsync report = new weeklyReportAsync();
                        report.execute(recipient);
                    }
                });
                break;

            case 4:
                d = UIDialog.onCreateListViewDialog(getActivity(), "Send Master Contact", lv, true);
                d.show();

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int pos, long id) {
                        d.dismiss();

                        String recipient = "";
                        switch (pos) {
                            case 0:
                                recipient = selfEmail;
                                break;
                            case 1:
                                recipient = ccEmail;

                        }

                        Toast.makeText(getActivity(), "Creating Report...", Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).genContactReport(null, recipient);
                    }
                });
        }
    }

    public class weeklyReportAsync extends AsyncTask<String, Void, String>
    {
        String today = new DateTime().toString("yyyy-MM-dd");
        String filePath = getActivity().getFilesDir().getPath() +  "/Nexis Attendance " + today + ".xls";

        protected String doInBackground(String... info) {
            List<String> nexcellTitles = new ArrayList<>(Data.NEXCELL_LIST);
            nexcellTitles.addAll(Constants.NEXCELL_CATEGORY_LIST);

            WeeklyReport report = new WeeklyReport(getActivity(), filePath);
            report.genReport();

            return info[0];
        }

        protected void onPostExecute(String rep) {
            SendMailAsync sendMail = new SendMailAsync(getActivity());
            sendMail.execute("Nexis Weekly Attendance Report", "Nexis Weekly Attendance Report for " + today , rep, Constants.SYSTEM_GMAIL, filePath);
        }
    }
}
