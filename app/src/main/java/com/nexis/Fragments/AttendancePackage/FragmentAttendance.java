package com.nexis.Fragments.AttendancePackage;

import com.github.clans.fab.FloatingActionButton;
import com.nexis.AttendanceView.AttendanceAdapter;
import com.nexis.AttendanceView.AttendanceItem;
import com.nexis.Activity.MainActivity;
import com.nexis.Constants;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.SendMailAsync;
import com.nexis.UIDialog;
import com.parse.ParseObject;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.util.ArrayList;
import java.util.List;

public class FragmentAttendance extends DialogFragment {

    private View rootView;

    private DateTime nextDate;
    private String userName, userNexcell;
    private List<AttendanceItem> attendanceList;
    private SubmitDialog submitDialog;
    private DateTime dialogDate;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private AttendanceAdapter mAttendanceAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int mScrollOffset = 4;

	public static FragmentAttendance newInstance() {
		FragmentAttendance fragment = new FragmentAttendance();
		return fragment;
	}

	public FragmentAttendance() {
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_attendance, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.cardList);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.swipeRefreshColors));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateCardsAsync popCard = new populateCardsAsync();
                popCard.execute("");
            }
        });

        attendanceList = new ArrayList<>();

        nextDate = new DateTime(DateTimeZone.UTC);
        DateTime friday = nextDate.withDayOfWeek(DateTimeConstants.FRIDAY);

        if (nextDate.isBefore(friday)) nextDate = friday.minusWeeks(1);
        else nextDate = friday;

        nextDate = nextDate.withTimeAtStartOfDay();

        TextView dateText = (TextView) rootView.findViewById(R.id.attendanceDate);
        dateText.setText(nextDate.toString("MMM dd, YYYY"));

        userName = ((MainActivity)getActivity()).getUserName();
        userNexcell = ((MainActivity)getActivity()).getUserNexcell();

        checkAndUpdate(nextDate);

        final FloatingActionButton addButton = (FloatingActionButton)rootView.findViewById(R.id.addButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitDialog = SubmitDialog.newInstance(getActivity().getLayoutInflater(), null);
                UIDialog.onCreateCustomDialog(getActivity(), "New Record - " + nextDate.toString("MMM dd"), submitDialog.getView(), "Submit", "Change Date", submitAttendanceListener, null);
            }
        });

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > mScrollOffset) {
                    if (dy > 0) {
                        addButton.hide(true);
                    } else {
                        addButton.show(true);
                    }
                }
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

        attendanceList.clear();

        for (int i = nexcellObject.size() - 1; i >= 0; i--) {
            final List<Integer> rowData = new ArrayList<>();
            final DateTime rowDt = new DateTime(nexcellObject.get(i).get("Date"), DateTimeZone.UTC);

            for (int j = 0; j < Constants.CATEGORY_LIST.size(); j++) {
                rowData.add((Integer) nexcellObject.get(i).get(Constants.CATEGORY_LIST.get(j)));
            }

            attendanceList.add(new AttendanceItem(rowData, rowDt, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDate = rowDt;

                    submitDialog = SubmitDialog.newInstance(getActivity().getLayoutInflater(), rowData);
                    UIDialog.onCreateCustomDialog(getActivity(), "Edit Record - " + rowDt.toString("MMM dd") , submitDialog.getView(), "Modify", "Delete", editAttendanceListener, deleteAttendanceListener);
                }
            }));
        }
    }


    private DialogInterface.OnClickListener editAttendanceListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int id) {
            uploadConfirmation(true, dialogDate, "Attendance Modified");
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

            checkAndUpdate(nextDate);

            Toast.makeText(getActivity(), "Attendance Deleted", Toast.LENGTH_LONG).show();
        }
    };

    private DialogInterface.OnClickListener submitAttendanceListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int id) {
            uploadConfirmation(false, nextDate, "Attendance Saved");
        }
    };

    private void editCardtoList(AttendanceItem item)
    {
        int pos = existInList(item.getDate());

        if (pos == -1) attendanceList.add(0, item);
        else attendanceList.set(pos, item);
    }

    private int existInList(DateTime date)
    {
        for (int i = 0; i < attendanceList.size(); i++) {
            if (attendanceList.get(i).getDate() == date) return i;
        }
        return -1;
    }

    private void checkAndUpdate(DateTime date)
    {
        List<ParseObject> nexcellObject = checkUpdate(date);

        if (nexcellObject.isEmpty()) {
            updateStatus(false, null);
        }
        else {
            ParseObject obj = nexcellObject.get(0);
            updateStatus(true, (String)obj.get("saveBy"));
        }
    }

    private  List<ParseObject> checkUpdate(DateTime newDate)
    {
        List<ParseObject> nexcellObject = ParseOperation.getNexcellData(userNexcell, newDate, getActivity());
        return nexcellObject;
    }

    private void updateStatus(boolean submitted, String userName)
    {
        TextView lastUpdated = (TextView) rootView.findViewById(R.id.updateTextView);
        TextView status = (TextView) rootView.findViewById(R.id.statusTextView);

        ImageView statusImage = (ImageView) rootView.findViewById(R.id.header_picture);

        if (!submitted)
        {
            lastUpdated.setText("N/A");
            status.setText("Missing");
            statusImage.setImageResource(R.drawable.ic_missing);
        } else {
            lastUpdated.setText(userName);
            status.setText("Submitted");
            statusImage.setImageResource(R.drawable.ic_success);
        }
    }

    public void uploadConfirmation(final boolean override, final DateTime newDate, final String toastMsg) {
        UIDialog.onCreateActionDialog(getActivity(), "Confirmation", "Are you sure you want to submit?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                List<ParseObject> nexcellObject = checkUpdate(newDate);
                if (!override && !nexcellObject.isEmpty()) {
                    UIDialog.onCreateInvalidDialog(getActivity(), "The attendance has already submitted!");
                } else {
                    uploadData(newDate);

                    Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void uploadData(final DateTime newDate) {
        List<ParseObject> nexcellObject = ParseOperation.getNexcellData(userNexcell, newDate, getActivity());
        final List<Integer> data = submitDialog.getData();

        if (!nexcellObject.isEmpty()) {
            ParseOperation.updateData(nexcellObject.get(0).getObjectId(), data.get(0), data.get(1), data.get(2), data.get(3), userName, getActivity());
        } else {
            ParseOperation.saveData(data.get(0), data.get(1), data.get(2), data.get(3), newDate, userNexcell, userName, getActivity());
        }

        if (newDate == nextDate) updateStatus(true, userName);

        editCardtoList(new AttendanceItem(data, newDate, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialogDate = newDate;

                submitDialog = SubmitDialog.newInstance(getActivity().getLayoutInflater(), data);
                UIDialog.onCreateCustomDialog(getActivity(), "Edit Record - " + newDate.toString("MMM dd"), submitDialog.getView(), "Modify", "Delete", editAttendanceListener, deleteAttendanceListener);
            }
        }));

        mAttendanceAdapter.notifyDataSetChanged();

        sendEmail(newDate, data);
    }

    private void sendEmail(DateTime newDate, List<Integer> data) {

        String toRecipients = ParseOperation.getSubmitDataRecipient(userNexcell, getActivity());
        String ccRecipients = Constants.SYSTEM_GMAIL;

        DateTime currentTime = new DateTime();

        String dateString = String.format("%d-%d-%d", newDate.getYear(), newDate.getMonthOfYear(), newDate.getDayOfMonth());

        String currentDateTimeString = currentTime.toString("yyyy-MM-dd HH:mm:ss.SSS");

        String emailSubject = "Confirmation for " + userNexcell + " Attendance Submission -- " + dateString;

        String emailBody = String.format("Below is the submission for Nexcell %s %s: \n\n"
                        + "Fellowship: %s \nService: %s \nCollege: %s \nNew Comer: %s \n\nTime of Submission: %s \nSubmitted By: %s",
                userNexcell, dateString, data.get(0), data.get(1), data.get(2), data.get(3), currentDateTimeString, userName);

        SendMailAsync sendMail = new SendMailAsync(getActivity());
        sendMail.execute(emailSubject, emailBody, toRecipients, ccRecipients, "", "");
    }

    public class populateCardsAsync extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... info) {
            populateCards();

            try {
                Thread.sleep(2000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            return null;
        }

        protected void onPostExecute(Void exception) {
            mAttendanceAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
            checkAndUpdate(nextDate);
        }
    }

}
