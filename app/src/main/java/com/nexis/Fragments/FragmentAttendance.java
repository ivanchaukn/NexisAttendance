package com.nexis.Fragments;

import com.nexis.AttendanceView.AttendanceAdapter;
import com.nexis.AttendanceView.AttendanceItem;
import com.nexis.Activity.MainActivity;
import com.nexis.R;

import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class FragmentAttendance extends DialogFragment {

    private List<AttendanceItem> attendanceList;

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
        View rootView = inflater.inflate(R.layout.fragment_attendance, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.cardList);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity)getActivity()).setToolbarElevation(0);

        attendanceList = new ArrayList<AttendanceItem>();
        attendanceList.add(new AttendanceItem(1,1,1,1));
        attendanceList.add(new AttendanceItem(1,1,1,1));
        attendanceList.add(new AttendanceItem(1,1,1,1));
        attendanceList.add(new AttendanceItem(1,1,1,1));
        attendanceList.add(new AttendanceItem(1,1,1,1));
        attendanceList.add(new AttendanceItem(1,1,1,1));
        attendanceList.add(new AttendanceItem(1,1,1,1));

        mAttendanceAdapter = new AttendanceAdapter(attendanceList);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setAdapter(mAttendanceAdapter);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

}
