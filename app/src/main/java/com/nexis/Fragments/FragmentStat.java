package com.nexis.Fragments;

import com.nexis.R;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FragmentStat extends DialogFragment {

	public static FragmentStat newInstance() {
		FragmentStat fragment = new FragmentStat();
<<<<<<< HEAD
        //Bundle args = new Bundle();
        //args.putInt("HData", 2);
        //fragment.setArguments(args);
        frag = fragment;
		
=======
		Bundle args = new Bundle();
		args.putInt("HData", 2);
		fragment.setArguments(args);
>>>>>>> 287d3bf8dfe9b7e6aa524367943554b82243ba77
		return fragment;
	}

	public FragmentStat() {
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_stat, container, false);
	}
}
