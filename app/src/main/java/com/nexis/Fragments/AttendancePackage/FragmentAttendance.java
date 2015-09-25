package com.nexis.Fragments.AttendancePackage;

import com.github.clans.fab.FloatingActionButton;
import com.nexis.AttendanceView.AttendanceAdapter;
import com.nexis.Activity.MainActivity;
import com.nexis.AttendanceView.AttendanceItem;
import com.nexis.Constants;
import com.nexis.Data;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.SendMailAsync;
import com.nexis.UIDialog;
import com.parse.ParseObject;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
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

    private boolean override;
    private DateTime nextDate;
    private String userName, userNexcell, userNexcellLabel;
    private List<AttendanceItem> attendanceList;
    private DateTime dialogDate;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private AttendanceAdapter mAttendanceAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayMap<String, List<String>> tempMap;
    private ArrayMap<String, String> usernameMap;
    private List<String> usernameList;
    private List<String> userFullnameList;

    private int mScrollOffset = 4;

	public static FragmentAttendance newInstance() {
		FragmentAttendance fragment = new FragmentAttendance();
		return fragment;
	}

	public FragmentAttendance(){
    }

    @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragment_attendance, container, false);

        tempMap = new ArrayMap<>();
        usernameList = new ArrayList<>();
        userFullnameList = new ArrayList<>();
        attendanceList = new ArrayList<>();
        tempMap = genNewMap();

        setupSwipeLayout();

        setupDate();
        setupUserInfo();
        updateStatus(nextDate);

        setAddButtonAnimation();
        populateCards();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).setToolbarElevation(0);

        boolean devVal = Data.getCacheLevel(getActivity(), "developer");
        boolean commiVal = Data.getCacheLevel(getActivity(), "committee");

        mAttendanceAdapter = new AttendanceAdapter(attendanceList, devVal, commiVal);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setAdapter(mAttendanceAdapter);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void setAddButtonAnimation()
    {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.cardList);

        final FloatingActionButton addButton = (FloatingActionButton)rootView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDate = nextDate;
                tempMap = genNewMap();
                override = false;
                fellowshipDialog();
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
    }

    private void setupUserInfo()
    {
        userName = ((MainActivity)getActivity()).getUserName();
        userNexcell = ((MainActivity)getActivity()).getUserNexcell();
        userNexcellLabel = ((MainActivity)getActivity()).getUserNexcellLabel();
        usernameMap = ((MainActivity)getActivity()).getNexcellUserMap();
        setupNexcellUsers(usernameMap);
    }

    private void setupNexcellUsers(ArrayMap<String, String> map)
    {
        usernameList.clear();
        userFullnameList.clear();
        usernameList.addAll(map.keySet());
        userFullnameList.addAll(map.values());
    }

    private void setupDate()
    {
        nextDate = new DateTime(DateTimeZone.UTC);
        DateTime friday = nextDate.withDayOfWeek(DateTimeConstants.FRIDAY);
        if (nextDate.isBefore(friday)) nextDate = friday.minusWeeks(1);
        else nextDate = friday;

        nextDate = nextDate.withTimeAtStartOfDay();

        TextView dateText = (TextView) rootView.findViewById(R.id.attendanceDate);
        dateText.setText(nextDate.toString("MMM dd, YYYY"));
    }

    private void setupSwipeLayout()
    {
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.swipeRefreshColors));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateCardsAsync popCard = new populateCardsAsync();
                popCard.execute();
            }
        });
    }

    private void populateCards()
    {
        ArrayMap<String, List<String>> rowMap;
        List<ParseObject> nexcellObject = ParseOperation.getNexcellData(userNexcell, null, null, true, getActivity());

        if (nexcellObject.isEmpty()) return;

        attendanceList.clear();
        rowMap = genNewMap();

        DateTime curtDate = null;

        for (int i = 0; i < nexcellObject.size(); i++) {

            final DateTime rowDt = new DateTime(nexcellObject.get(i).get("date"), DateTimeZone.UTC);

            if (curtDate == null) curtDate = rowDt;
            else if (!rowDt.equals(curtDate))
            {
                addToAtdList(rowMap, curtDate);
                curtDate = rowDt;
                rowMap = genNewMap();
            }

            String cat = (String) nexcellObject.get(i).get("category");
            String name = (String) nexcellObject.get(i).get("userName");

            addToMap(rowMap, cat, name);
        }

        addToAtdList(rowMap, curtDate);
    }

    public class populateCardsAsync extends AsyncTask<String, Void, Void>
    {

        protected Void doInBackground(String... info) {
            ParseOperation.refreshAttendanceLocalData(getActivity());
            populateCards();

            ((MainActivity)getActivity()).refreshUserMap();
            usernameMap = ((MainActivity)getActivity()).getNexcellUserMap();
            setupNexcellUsers(usernameMap);

            return null;
        }

        protected void onPostExecute(Void exception) {
            mAttendanceAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
            updateStatus(nextDate);
        }
    }

    private ArrayMap<String, List<String>> genNewMap()
    {
        ArrayMap<String, List<String>> newMap = new ArrayMap<>();
        for(int i = 0; i < Constants.CATEGORY_LIST.size(); i++)
        {
            newMap.put(Constants.CATEGORY_LIST.get(i), new ArrayList<String>());
        }
        return newMap;
    }

    private void addToAtdList(final ArrayMap<String, List<String>> map, final DateTime date)
    {
        AttendanceItem additem =  new AttendanceItem(map, date, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDate = date;
                tempMap = map;
                override = true;
                fellowshipDialog();
            }
        });

        int pos = existInList(additem.getDate());

        if (pos == -1) attendanceList.add(0, additem);
        else attendanceList.set(pos, additem);
    }

    private void addToMap(ArrayMap<String, List<String>> map, String cat, String name)
    {
        List<String> nList = map.get(cat);
        nList.add(name);
        map.put(cat, nList);
    }

    private void removeFromMapList(ArrayMap<String, List<String>> map, String cat, String name)
    {
        List<String> nList = map.get(cat);
        nList.remove(name);
        map.put(cat, nList);
    }

    private int existInList(DateTime date)
    {
        for (int i = 0; i < attendanceList.size(); i++) {
            if (attendanceList.get(i).getDate().equals(date)) return i;
        }
        return -1;
    }

    private void updateStatus(DateTime date)
    {
        List<ParseObject> nexcellObject = getRecentSubmit(date);

        if (nexcellObject.isEmpty()) {
            changeStatus(false, null);
        }
        else {
            ParseObject obj = nexcellObject.get(0);
            changeStatus(true, (String) obj.get("saveBy"));
        }
    }

    private  List<ParseObject> getRecentSubmit(DateTime newDate)
    {
        List<ParseObject> nexcellObject = ParseOperation.getNexcellData(userNexcell, null, newDate, false, getActivity());
        return nexcellObject;
    }

    private void changeStatus(boolean submitted, String userName)
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

    public void uploadConfirmation(final boolean flag, final DateTime newDate)
    {
        UIDialog.onCreateActionDialog(getActivity(), "Confirmation", "Are you sure you want to submit?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                List<ParseObject> nexcellObject = getRecentSubmit(newDate);
                if (!flag && !nexcellObject.isEmpty()) {
                    UIDialog.onCreateInvalidDialog(getActivity(), "The attendance has already submitted!");
                } else {
                    Toast.makeText(getActivity(), "Saving...", Toast.LENGTH_LONG).show();
                    uploadData(newDate);
                    Toast.makeText(getActivity(), "Attendance Saved", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void uploadData(final DateTime newDate)
    {
        ParseOperation.saveUserAttendance(tempMap, newDate, userNexcell, userName, getActivity());

        if (newDate == nextDate) changeStatus(true, userName);

        addToAtdList(tempMap, newDate);
        mAttendanceAdapter.notifyDataSetChanged();

        sendEmail(newDate, tempMap);
    }

    private void sendEmail(DateTime newDate, ArrayMap<String, List<String>> data)
    {
        String toRecipients = ParseOperation.getSubmitDataRecipient(userNexcell, getActivity());
        String ccRecipients = Constants.SYSTEM_GMAIL;

        DateTime currentTime = new DateTime();

        String dateString = String.format("%d-%d-%d", newDate.getYear(), newDate.getMonthOfYear(), newDate.getDayOfMonth());

        String currentDateTimeString = currentTime.toString("yyyy-MM-dd HH:mm:ss.SSS");

        String emailSubject = "Confirmation for " + userNexcellLabel + " Attendance Submission -- " + dateString;

        String emailBody = String.format("Below is the submission for Nexcell %s %s: \n\n"
                        + "Fellowship: %s \nService: %s \nCollege: %s \nNew Comer: %s \n\nTime of Submission: %s \nSubmitted By: %s",
                userNexcellLabel, dateString, data.get(Constants.CATEGORY_LIST.get(0)).size(),
                                              data.get(Constants.CATEGORY_LIST.get(1)).size(),
                                              data.get(Constants.CATEGORY_LIST.get(2)).size(),
                                              data.get(Constants.CATEGORY_LIST.get(3)).size(),
                                              currentDateTimeString, userName);

        SendMailAsync sendMail = new SendMailAsync(getActivity());
        sendMail.execute(emailSubject, emailBody, toRecipients, ccRecipients, "", "");
    }

    private boolean[] genUserBoolean(List<String> attendanceList)
    {
        boolean[] bl = new boolean[usernameList.size()];

        for (int i = 0; i < usernameList.size(); i++)
        {
            if (attendanceList.contains(usernameList.get(i))) bl[i] = true;
        }

        return bl;
    }

    private void showSubmitDialog(List<String> lst, final String title, String pBtn, String nBtn,
                                  DialogInterface.OnClickListener pLstn, DialogInterface.OnClickListener nLstn)
    {
        boolean[] bl = genUserBoolean(lst);
        UIDialog.onCreateMultiChoiceListDialog(getActivity(), title + " - " + dialogDate.toString("MMM dd"), userFullnameList, bl, new DialogInterface.OnMultiChoiceClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id, boolean arg) {
                List<String> usernameList = new ArrayList<>();
                usernameList.addAll(usernameMap.keySet());

                String rowString = usernameList.get(id);

                if (arg) addToMap(tempMap, title, rowString);
                else removeFromMapList(tempMap, title, rowString);
            }
        }, pBtn, nBtn, pLstn, nLstn);
    }

    private void fellowshipDialog()
    {
                showSubmitDialog(tempMap.get(Constants.FELLOWSHIP_STRING), Constants.FELLOWSHIP_STRING, "Next", null, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                serviceDialog();
            }
        }, null);
    }

    private void serviceDialog()
    {
        showSubmitDialog(tempMap.get(Constants.SERVICE_STRING), Constants.SERVICE_STRING, "Next", "Back", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                collegeDialog();
            }
        }, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                fellowshipDialog();
            }
        });
    }

    private void collegeDialog()
    {
        showSubmitDialog(tempMap.get(Constants.COLLEGE_STRING), Constants.COLLEGE_STRING, "Submit", "Back",  new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                uploadConfirmation(override, nextDate);
            }
        }, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                serviceDialog();
            }
        });
    }

    private DialogInterface.OnClickListener deleteAttendanceListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int id) {
            UIDialog.onCreateActionDialog(getActivity(), "Delete Record ?", "Are you sure you want to delete the record?", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    int pos = mAttendanceAdapter.getmTouchedPosition();
                    DateTime date = attendanceList.get(pos).getDate();

                    ParseOperation.deleteData(userNexcell, date, getActivity());

                    attendanceList.remove(pos);
                    mAttendanceAdapter.notifyItemRangeRemoved(pos, 1);

                    updateStatus(nextDate);

                    Toast.makeText(getActivity(), "Attendance Deleted", Toast.LENGTH_LONG).show();
                }
            });
        }
    };
}
