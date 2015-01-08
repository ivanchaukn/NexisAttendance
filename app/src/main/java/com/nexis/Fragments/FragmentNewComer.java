package com.nexis.Fragments;

import java.util.ArrayList;
import java.util.List;

import com.nexis.Constants;
import com.nexis.Activity.MainActivity;
import com.nexis.ExcelReports.genNewComerForm;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.UIDialog;
import com.nexis.SendMailAsync;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentNewComer extends DialogFragment {

	private String nexcell, filePath; 
	private DateTime newComerBirthday, defaultDate;
	private Button dateButton;

    private List<String> labelList = new ArrayList<String>();
    private List<String> infoList = new ArrayList<String>();
	
	public static FragmentNewComer newInstance() {
		FragmentNewComer fragment = new FragmentNewComer();
		Bundle args = new Bundle();
		args.putInt("NewComer", 3);
		fragment.setArguments(args);
		return fragment;
	}

	public FragmentNewComer() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_newcomer, container, false);
		
		filePath = getActivity().getFilesDir().getPath().toString() +  "/NewComer.xls";
		
		defaultDate = new DateTime(1990, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
		newComerBirthday = defaultDate;
		
		dateButton = (Button) rootView.findViewById(R.id.newComerBirthdayButton);
		
		dateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialog d = new DatePickerDialog(getActivity(), mDateSetListener, newComerBirthday.getYear(), newComerBirthday.getMonthOfYear()-1, newComerBirthday.getDayOfMonth());
				d.show();
			}
		});

		//Submit button
		Button submitButton = (Button) rootView.findViewById(R.id.newComerSubmitButton);
		
		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UIDialog.onCreateActionDialog(getActivity(), "Submit", "Are you sure you want to submit?", submitInfoListener);
			}
		});
		
		Button clearButton = (Button) rootView.findViewById(R.id.newComerClearButton);
		
		clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UIDialog.onCreateActionDialog(getActivity(), "Clear Data", "Are you sure you want to clear all data?", clearDataListener);
			}
		});
		
		underlineTitles(rootView);
		
		return rootView;
	}

	private void sendEmailInfo(View view)
    {
        nexcell = ((MainActivity)getActivity()).getUserNexcell();

        genNewComerForm report = new genNewComerForm(getActivity(), filePath, labelList, infoList);
        report.genReport();

        //TODO Email should send to committee and ESMs
        String toRecipients = ParseOperation.getNewComerFormRecipient(nexcell, getActivity());
        String ccRecipients = Constants.SYSTEM_GMAIL;

        SendMailAsync sendMail = new SendMailAsync(getActivity());
        sendMail.execute(nexcell + " New Comer Detail **TESTING", "Please refer to the attachment for details" , toRecipients, ccRecipients, filePath);

        Toast.makeText(getActivity(), "Saved Successfully" , Toast.LENGTH_LONG).show();
    }
	
	private void clearInfo(View view)
    {
        ViewGroup group = (ViewGroup)view;

        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View v = group.getChildAt(i);

            if (v instanceof Button && group.getChildCount() != 1) {
                ((Button)v).setText("Please select date");
                newComerBirthday = defaultDate;
            }
            else if (v instanceof EditText) {
                ((EditText)v).setText("");
            }
            else if (v instanceof RadioGroup) {
                ((RadioGroup)v).clearCheck();
            }
            else if(v instanceof ViewGroup && (((ViewGroup)v).getChildCount() > 0)) {
                clearInfo((ViewGroup)v);
            }
        }
    }

    private void retrieiveInfo(View view, List<String> lbList, List<String> ioList)
    {
        ViewGroup group = (ViewGroup)view;

        if (group.getChildCount() == 1 && group.getChildAt(0) instanceof TextView) return;

        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View v = group.getChildAt(i);

            if (v instanceof Button) {
                if (((Button)v).getText().equals("Please select date")) ioList.add("");
                else ioList.add(newComerBirthday.toString("YYYY/MM/dd"));
            }
            else if (v instanceof EditText) {
                ioList.add(((EditText)v).getText().toString());
            }
            else if (v instanceof TextView) {
                lbList.add(((TextView)v).getText().toString());
            }
            else if (v instanceof RadioGroup) {
                int checkedId = ((RadioGroup)v).getCheckedRadioButtonId();

                if (checkedId == -1) ioList.add("");
                else
                {
                    String radioButtonString = ((RadioButton) this.getView().findViewById(checkedId)).getText().toString();
                    ioList.add(radioButtonString);
                }
            }
            else if(v instanceof ViewGroup && (((ViewGroup)v).getChildCount() > 0)) {
                retrieiveInfo(v, lbList, ioList);
            }
        }
    }

    private boolean checkMissingField()
    {
        for (int i = 0; i < labelList.size(); i++)
        {
            if (labelList.get(i).contains("*") && infoList.get(i).equals(""))
            {
                UIDialog.onCreateInvalidDialog(getActivity(), "Please make sure all required fields are entered!");
                return true;
            }
        }
        return false;
    }

	private void underlineTitles(View view)
	{
		TextView generalTitle = (TextView) view.findViewById(R.id.generalTitle);
		generalTitle.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
		
		TextView academicTitle = (TextView) view.findViewById(R.id.academicTitle);
		academicTitle.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
		
		TextView spiritualTitle = (TextView) view.findViewById(R.id.spiritualTitle);
		spiritualTitle.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
	}
	
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			
			newComerBirthday = new DateTime(year, monthOfYear+1, dayOfMonth, 0, 0, 0, 0, DateTimeZone.UTC);

			dateButton.setText(newComerBirthday.toString("MMM dd, YYYY"));
		}
	};
	
	private DialogInterface.OnClickListener submitInfoListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int id) {

            labelList.clear();
            infoList.clear();

            retrieiveInfo(getFragmentView(), labelList, infoList);

            if (checkMissingField()) return;

            sendEmailInfo(getFragmentView());
		}
	};
	
	private DialogInterface.OnClickListener clearDataListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int id) {
			clearInfo(getFragmentView());
		}
	}; 
	
	public View getFragmentView()
	{
		return this.getView();
	}
}
