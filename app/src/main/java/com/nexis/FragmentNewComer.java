package com.nexis;

import java.util.Arrays;
import java.util.List;

import com.nexis.R;

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
	
	private void submitInfo(View view)
	{
		//RADIO BUTTONS
		int genderRadioId = ((RadioGroup) view.findViewById(R.id.radioGender)).getCheckedRadioButtonId();
		int christianRadioId = ((RadioGroup) view.findViewById(R.id.radioChristian)).getCheckedRadioButtonId();
		int baptizedRadioId = ((RadioGroup) view.findViewById(R.id.radioBaptized)).getCheckedRadioButtonId();
		
		if (genderRadioId == -1 || christianRadioId == -1 || baptizedRadioId == -1 || newComerBirthday == defaultDate)
		{
			missingFieldDialog();
			return;
		}
		
		String gender = ((RadioButton) view.findViewById(genderRadioId)).getText().toString();
		String christian = ((RadioButton) view.findViewById(christianRadioId)).getText().toString();
		String baptized = ((RadioButton) view.findViewById(baptizedRadioId)).getText().toString();
		
		//REQUIRED EDITTEXT FIELDS
		String firstName = ((EditText) view.findViewById(R.id.newComerFirstName)).getText().toString();
		String lastName = ((EditText) view.findViewById(R.id.newComerLastName)).getText().toString();
		String cellPhone = ((EditText) view.findViewById(R.id.newComerCellPhone)).getText().toString();
		String email = ((EditText) view.findViewById(R.id.newComerEmail)).getText().toString();
		String school = ((EditText) view.findViewById(R.id.newComerSchool)).getText().toString();
		String gradeYear = ((EditText) view.findViewById(R.id.newComerGradeYear)).getText().toString();
		
		List<String> requiredFields = Arrays.asList(firstName, lastName, cellPhone, email, school, gradeYear);
		
		for(String x: requiredFields)
		{
			if (x.equals("")) 
			{
				missingFieldDialog();
				return;
			}
		}

		//NON-REQUIRED EDITTEXT FIELDS
		String homePhone = ((EditText) view.findViewById(R.id.newComerHomePhone)).getText().toString();
		String address = ((EditText) view.findViewById(R.id.newComerAddress)).getText().toString();
		String postalCode = ((EditText) view.findViewById(R.id.newComerPostalCode)).getText().toString();
		String city = ((EditText) view.findViewById(R.id.newComerCity)).getText().toString();
		String christianYear = ((EditText) view.findViewById(R.id.newComerYearChristian)).getText().toString();
		String baptizedYear = ((EditText) view.findViewById(R.id.newComerYearBaptized)).getText().toString();

		
		newComerInfo info = new newComerInfo(firstName, lastName, gender, newComerBirthday.toString("dd/MM/yyyy"), homePhone, cellPhone, email, address, postalCode, city,
											 school, gradeYear, christian, baptized, christianYear, baptizedYear);
		
		sendEmailInfo(view, info);
	}
	
	private void sendEmailInfo(View view, newComerInfo info)
	{
		nexcell = ((MainActivity)getActivity()).getUserNexcell();
		
		boolean excel = Excel.createNewComerForm(info, filePath, getActivity());
		if (!excel) return;
		
		//TODO Email should send to committee and ESMs
		String toRecipients = ParseOperation.getUserEmail(Constants.USER_LEVEL, nexcell, getActivity());
		String ccRecipients = ParseOperation.getUserEmail(Constants.MASTER_LEVEL, null, getActivity());
		
		sendMailAsync sendMail = new sendMailAsync(getActivity());
		sendMail.execute(nexcell + " New Comer Detail", "Please refer to the attachment for details" , toRecipients, ccRecipients, filePath);
		
		Toast.makeText(getActivity(), "Saved Successfully" , Toast.LENGTH_LONG).show();
	}
	
	private void clearInfo(View view)
	{
		ViewGroup group = (ViewGroup)view;
		
		for (int i = 0, count = group.getChildCount(); i < count; ++i) {
		    View v = group.getChildAt(i);
		    if (v instanceof EditText) {
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

	private void underlineTitles(View view)
	{
		TextView generalTitle = (TextView) view.findViewById(R.id.generalTitle);
		generalTitle.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
		
		TextView academicTitle = (TextView) view.findViewById(R.id.academicTitle);
		academicTitle.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
		
		TextView spiritualTitle = (TextView) view.findViewById(R.id.spiritualTitle);
		spiritualTitle.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
	}
	
	private void missingFieldDialog()
    {
    	UIDialog.onCreateInvalidDialog(getActivity(), "Please make sure all required fields are entered!");
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
			submitInfo(getFragmentView());
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
