/*
package com.nexis.Fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.nexis.Activity.MainActivity;
import com.nexis.Constants;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.SendMailAsync;
import com.nexis.UIDialog;
import com.parse.ParseObject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.text.DateFormatSymbols;
import java.util.List;
import java.util.Locale;


public class FragmentUpload_old extends DialogFragment {

	static final int numberPickerMax = 50;
	static final int numberPickerMin = 0;

	static DateTime date = new DateTime(DateTimeZone.UTC);

	private String userName, nexcell;
	private int authLevel;
	private int fellNum, collNum, servNum, newcNum = 0;

	OnFragmentUploadSelectedListener mCallback;

	public interface OnFragmentUploadSelectedListener {
        public void onUploadAttendance(DateTime date, int f, int s, int c);
    }

	public static FragmentUpload_old newInstance() {
		FragmentUpload_old fragment = new FragmentUpload_old();
		Bundle args = new Bundle();
		args.putInt("Upload", 1);
		fragment.setArguments(args);
		return fragment;
	}

	public FragmentUpload_old() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_upload, container, false);

		//NumberPickers Initialization
		NumberPicker npF = (NumberPicker) rootView.findViewById(R.id.numberPickerFellowship);
		NumberPicker npC = (NumberPicker) rootView.findViewById(R.id.numberPickerCollege);
		NumberPicker npS = (NumberPicker) rootView.findViewById(R.id.numberPickerService);
		NumberPicker npN = (NumberPicker) rootView.findViewById(R.id.numberPickerNewComer);

		initializeNumberPickers(npF);
		initializeNumberPickers(npC);
		initializeNumberPickers(npS);
		initializeNumberPickers(npN);

		//Date Select Button
		Button dateSelectButton = (Button) rootView.findViewById(R.id.dateSelectbutton);

		date = date.dayOfWeek().setCopy(DateTimeConstants.FRIDAY);
		date = date.withTimeAtStartOfDay();

		setDate(rootView);
		setNexcell(rootView, ((MainActivity)getActivity()).getUserNexcell());
		checkLastUpdated(rootView);

		userName = ((MainActivity)getActivity()).getUserName();
		authLevel = ((MainActivity)getActivity()).getUserAuthlevel();

		dateSelectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialog d = new DatePickerDialog(getActivity(), mDateSetListener, date.getYear(), date.getMonthOfYear()-1, date.getDayOfMonth());
				d.show();
			}
		});

		//Submit button
		Button submitButton = (Button) rootView.findViewById(R.id.submitButton);

		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UIDialog.onCreateActionDialog(getActivity(), "Confirmation", "Are you sure you want to submit?", uploadDataListener);
			}
		});

		Button nexcellButton = (Button) rootView.findViewById(R.id.nexcellSelectButton);

		nexcellButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (authLevel >= Constants.ADMIN_LEVEL)
				{
					UIDialog.onCreateListDialog(getActivity(), "Nexcell list",  Constants.NEXCELL_LIST, updateNexcellListener);
				}
				else
				{
					UIDialog.onCreateInvalidDialog(getActivity(), "You are not authorized to change nexcell!");
				}
			}
		});

		return rootView;
	}

	private void initializeNumberPickers(NumberPicker np)
	{
		np.setMaxValue(numberPickerMax);
		np.setMinValue(numberPickerMin);
		np.setOnValueChangedListener(valueChangeListener);
		np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
	}

	private OnValueChangeListener valueChangeListener = new OnValueChangeListener() {
		
	    @Override
	    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
	    	if (picker.getId() == R.id.numberPickerFellowship) fellNum = picker.getValue();
	    	if (picker.getId() == R.id.numberPickerCollege) collNum = picker.getValue();
	    	if (picker.getId() == R.id.numberPickerService) servNum = picker.getValue();
	    	if (picker.getId() == R.id.numberPickerNewComer) newcNum = picker.getValue();
	    }
	};
	
	private DialogInterface.OnClickListener updateNexcellListener = new DialogInterface.OnClickListener() {
        
		@Override
		public void onClick(DialogInterface dialog, int which) {
     	   setNexcell(getFragmentView(), Constants.NEXCELL_CHARLIST[which].toString());
     	   checkLastUpdated(getFragmentView());
        }
	};
	
	private DialogInterface.OnClickListener uploadDataListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int id) {
			uploadData();
		}
	}; 
	
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			
			if (view.isShown())
			{
				DateTime oldDate = date;
				
				date = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0, 0, 0, DateTimeZone.UTC);
	
				if(date.getDayOfWeek() != DateTimeConstants.FRIDAY)
				{
					date = oldDate;
					Dialog d = new DatePickerDialog(getActivity(), mDateSetListener, date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
					d.show();
					
					UIDialog.onCreateInvalidDialog(getActivity(), "You are only permitted to select Friday");
					
					return;
				}
				
				Toast.makeText(getActivity(), "Selected Date: " + date.getDayOfMonth() + " / " + date.getMonthOfYear() + " / " + date.getYear(), Toast.LENGTH_LONG).show();
	
				setDate(getFragmentView());
				checkLastUpdated(getFragmentView());
			}
		}
	}; 
	
	private void setDate(View view)
	{
		EditText dateText = (EditText) view.findViewById(R.id.EditTextDate);
		
		dateText.setText(getMonth(date.getMonthOfYear()) + " " + date.getDayOfMonth() + ", " + date.getYear() + " (" + date.toString("EEEE", Locale.US) + ")");
	}
	
	private void setNexcell(View view, String newNexcell)
	{
		EditText nexcellText = (EditText) view.findViewById(R.id.EditTextNexcell);
		
		nexcellText.setText(newNexcell);
		
		nexcell = newNexcell;
	}
	
	private void checkLastUpdated(View view)
	{
		List<ParseObject> nexcellObject = ParseOperation.getNexcellData(nexcell, date, getActivity());
		
		TextView lastUpdated = (TextView) view.findViewById(R.id.lastUpdatedText);
		
		if (!nexcellObject.isEmpty())
		{
			String lastUserName = (String) nexcellObject.get(0).get("saveBy");
			int fNum = (Integer) nexcellObject.get(0).get("Fellowship");
			int sNum = (Integer) nexcellObject.get(0).get("Service");
			int cNum = (Integer) nexcellObject.get(0).get("College");
			int nNum = (Integer) nexcellObject.get(0).get("NewComer");
			
			lastUpdated.setText("Last updated by " + lastUserName + " -- " + "F:" + fNum + " S:" + sNum + " C:" + cNum + " N:" + nNum);
		}
		else
		{
			lastUpdated.setText("");
		}
	}
	
	private void uploadData()
	{
		List<ParseObject> nexcellObject = ParseOperation.getNexcellData(nexcell, date, getActivity());
		if (!nexcellObject.isEmpty())
		{
			ParseOperation.updateData(nexcellObject.get(0).getObjectId(), fellNum, servNum, collNum, newcNum, userName, getActivity());
		}
		else
		{
			ParseOperation.saveData(fellNum, servNum, collNum, newcNum, date, nexcell, userName, getActivity());
		}
		
		checkLastUpdated(getFragmentView());
		
		String toRecipients = ParseOperation.getSubmitDataRecipient(nexcell, getActivity());
        String ccRecipients = Constants.SYSTEM_GMAIL;

		DateTime currentTime = new DateTime();
		
		String dateString = String.format("%d-%d-%d", date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
		
		String currentDateTimeString = String.format("%d-%d-%d %s:%s:%s:%s", currentTime.getYear(), currentTime.getMonthOfYear(), currentTime.getDayOfMonth(), 
				currentTime.hourOfDay().getAsString(), currentTime.minuteOfHour().getAsString(), currentTime.secondOfMinute().getAsString(), 
				currentTime.millisOfSecond().getAsString());
		
		String emailSubject = "Confirmation for " + nexcell + " Attendance Submission -- " + dateString;
		
		String emailBody = String.format("Below is the submission for Nexcell %s %s: \n\n"
				+ "Fellowship: %s \nService: %s \nCollege: %s \nNew Comer: %s \n\nTime of Submission: %s \nSubmitted By: %s", 
				nexcell, dateString, fellNum, servNum, collNum, newcNum, currentDateTimeString, userName);
		
		SendMailAsync sendMail = new SendMailAsync(getActivity());
		sendMail.execute(emailSubject, emailBody, toRecipients, ccRecipients, "", "");

		Toast.makeText(getActivity(), "Saved Successfully" , Toast.LENGTH_LONG).show();
	}
	
	public View getFragmentView()
	{
		return this.getView();
	}
	
	public String getMonth(int month) 
	{
	    return new DateFormatSymbols().getMonths()[month-1];
	}
	
	public DateTime getDateTime()
	{
		return date;
	}
}
*/
