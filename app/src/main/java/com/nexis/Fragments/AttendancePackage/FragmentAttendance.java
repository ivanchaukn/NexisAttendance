package com.nexis.Fragments.AttendancePackage;

import com.nexis.AttendanceView.AttendanceAdapter;
import com.nexis.AttendanceView.AttendanceItem;
import com.nexis.Activity.MainActivity;
import com.nexis.Constants;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.SendMailAsync;
import com.nexis.UIDialog;
import com.parse.Parse;
import com.parse.ParseObject;

import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class FragmentAttendance extends DialogFragment {

    private View rootView;

    private DateTime nextDate;
    private String userName, userNexcell;
    private List<AttendanceItem> attendanceList;
    private SubmitDialog submitDialog;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private AttendanceAdapter mAttendanceAdapter;

	public static FragmentAttendance newInstance() {
		FragmentAttendance fragment = new FragmentAttendance();
		return fragment;
	}

	public FragmentAttendance() {
	}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_attendance, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.cardList);

        attendanceList = new ArrayList<AttendanceItem>();

        nextDate = new DateTime();
        nextDate = nextDate.dayOfWeek().setCopy(DateTimeConstants.FRIDAY);
        nextDate = nextDate.withTimeAtStartOfDay();

        TextView dateText = (TextView) rootView.findViewById(R.id.attendanceDate);
        dateText.setText(nextDate.toString("MMM dd, YYYY"));

        userName = ((MainActivity)getActivity()).getUserName();
        userNexcell = ((MainActivity)getActivity()).getUserNexcell();

        checkAndUpdate();

        FloatingActionButton addButton = (FloatingActionButton)rootView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitDialog = SubmitDialog.newInstance(getActivity().getLayoutInflater());
                UIDialog.onCreateCustomDialog(getActivity(), "New Record - " + nextDate.toString("MMM dd"), submitDialog.getView(), "Submit", "Change Date", submitAttendanceListener, null);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).setToolbarElevation(0);

        populateCards();

        mAttendanceAdapter = new AttendanceAdapter(attendanceList);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setAdapter(mAttendanceAdapter);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void populateCards() {
        List<ParseObject> nexcellObject = ParseOperation.getNexcellData(userNexcell, null, getActivity());

        for (int i = nexcellObject.size() - 1; i >= 0; i--) {
            List<Integer> rowData = new ArrayList<Integer>();
            final DateTime rowDt = new DateTime(nexcellObject.get(i).get("Date"), DateTimeZone.UTC);

            for (int j = 0; j < Constants.CATEGORY_LIST.size(); j++) {
                rowData.add((Integer) nexcellObject.get(i).get(Constants.CATEGORY_LIST.get(j)));
            }

            attendanceList.add(new AttendanceItem(rowData, rowDt, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SubmitDialog submitDialog = SubmitDialog.newInstance(getActivity().getLayoutInflater());
                    UIDialog.onCreateCustomDialog(getActivity(), "Edit Record - " + rowDt.toString("MMM dd") , submitDialog.getView(), "Modify", "Delete", editAttendanceListener, deleteAttendanceListener);
                }
            }));
        }
    }

    private DialogInterface.OnClickListener editAttendanceListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int id) {
            uploadConfirmation();
        }
    };

    private DialogInterface.OnClickListener deleteAttendanceListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int id) {
            UIDialog.onCreateActionDialog(getActivity(), "Delete Record ?", "Are you sure you want to delete the record?", deleteListener);
        }
    };

    private DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int id) {
            int pos = mAttendanceAdapter.getmTouchedPosition();
            DateTime date = attendanceList.get(pos).getDate();

            ParseOperation.deleteData(userNexcell, date, getActivity());

            attendanceList.remove(pos);
            mAttendanceAdapter.notifyItemRangeRemoved(pos, 1);

            checkAndUpdate();
        }
    };

    private DialogInterface.OnClickListener submitAttendanceListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int id) {
            uploadConfirmation();
        }
    };

    private void addCardtoList(AttendanceItem item)
    {
        if (!existInList(item.getDate())) attendanceList.add(0, item);
    }

    private boolean existInList(DateTime date)
    {
        for (int i = 0; i < attendanceList.size(); i++) {
            if (attendanceList.get(i).getDate() == date) return true;
        }
        return false;
    }

    private void checkAndUpdate()
    {
        DateTime lastUpdate = checkUpdate();

        if (lastUpdate == null) {
            updateStatus(false, null);
        }
        else {
            updateStatus(true, lastUpdate);
        }
    }

    private DateTime checkUpdate()
    {
        List<ParseObject> nexcellObject = ParseOperation.getNexcellData(userNexcell, nextDate, getActivity());

        if (nexcellObject.isEmpty()) return null;
        else return new DateTime(nexcellObject.get(0).getUpdatedAt());
    }

    private void updateStatus(boolean submitted, DateTime updateTime)
    {
        TextView lastUpdated = (TextView) rootView.findViewById(R.id.updateTextView);
        TextView status = (TextView) rootView.findViewById(R.id.statusTextView);

        ImageView statusImage = (ImageView) rootView.findViewById(R.id.header_picture);

        if (!submitted)
        {
            lastUpdated.setText("N/A");
            status.setText("Missing");
            statusImage.setImageResource(R.drawable.ic_missing);
        }
        else
        {
            lastUpdated.setText(updateTime.toString("MMM dd, YYYY  HH:MM:SS"));
            status.setText("Submitted");
            statusImage.setImageResource(R.drawable.ic_success);
        }

    }

    public void uploadConfirmation()
    {
        UIDialog.onCreateActionDialog(getActivity(), "Confirmation", "Are you sure you want to submit?",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                DateTime updateTime = checkUpdate();
                if (updateTime == null) {
                    uploadData();
                }
                else {
                    UIDialog.onCreateInvalidDialog(getActivity(), "The attendance has already submitted!");
                }
            }
        });
    }

    private void uploadData()
    {
        List<ParseObject> nexcellObject = ParseOperation.getNexcellData(userNexcell, nextDate, getActivity());
        List<Integer> data = submitDialog.getData();

        if (!nexcellObject.isEmpty())
        {
            ParseOperation.updateData(nexcellObject.get(0).getObjectId(), data.get(0), data.get(1), data.get(2), data.get(3), userName, getActivity());
        }
        else
        {
            ParseOperation.saveData(data.get(0), data.get(1), data.get(2), data.get(3), nextDate, userNexcell, userName, getActivity());
        }

        updateStatus(true, new DateTime());

        addCardtoList(new AttendanceItem(data, nextDate,  new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SubmitDialog submitDialog = SubmitDialog.newInstance(getActivity().getLayoutInflater());
                UIDialog.onCreateCustomDialog(getActivity(), "Edit Record - " + nextDate.toString("MMM dd") , submitDialog.getView(), "Modify", "Delete", editAttendanceListener, deleteAttendanceListener);
            }
        }));

        mAttendanceAdapter.notifyDataSetChanged();

        String toRecipients = ParseOperation.getSubmitDataRecipient(userNexcell, getActivity());
        String ccRecipients = Constants.SYSTEM_GMAIL;

        DateTime currentTime = new DateTime();

        String dateString = String.format("%d-%d-%d", nextDate.getYear(), nextDate.getMonthOfYear(), nextDate.getDayOfMonth());

        String currentDateTimeString = String.format("%d-%d-%d %s:%s:%s:%s", currentTime.getYear(), currentTime.getMonthOfYear(), currentTime.getDayOfMonth(),
                currentTime.hourOfDay().getAsString(), currentTime.minuteOfHour().getAsString(), currentTime.secondOfMinute().getAsString(),
                currentTime.millisOfSecond().getAsString());

        String emailSubject = "Confirmation for " + userNexcell + " Attendance Submission -- " + dateString;

        String emailBody = String.format("Below is the submission for Nexcell %s %s: \n\n"
                        + "Fellowship: %s \nService: %s \nCollege: %s \nNew Comer: %s \n\nTime of Submission: %s \nSubmitted By: %s",
                userNexcell, dateString, data.get(0), data.get(1), data.get(2), data.get(3), currentDateTimeString, userName);

        SendMailAsync sendMail = new SendMailAsync(getActivity());
        //sendMail.execute(emailSubject, emailBody, toRecipients, ccRecipients, "", "");

        Toast.makeText(getActivity(), "Saved Successfully", Toast.LENGTH_LONG).show();
    }



    public DateTime getNextUpdateDate()
    {
        return nextDate;
    }

}
