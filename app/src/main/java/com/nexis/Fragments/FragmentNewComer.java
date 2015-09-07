package com.nexis.Fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.nexis.Constants;
import com.nexis.Activity.MainActivity;
import com.nexis.ExcelReports.NewComerForm;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.UIDialog;
import com.nexis.SendMailAsync;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentNewComer extends DialogFragment {

	private String nexcell, nexcellLabel, filePath, newusername;
	private DateTime newComerBirthday, defaultDate;
	private Button dateButton;

    ProgressDialog pDialog;

    private List<String> labelList = new ArrayList<>();
    private List<String> infoList = new ArrayList<>();
	
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
		final View rootView = inflater.inflate(R.layout.fragment_newcomer, container, false);

        nexcell = ((MainActivity)getActivity()).getUserNexcell();
        nexcellLabel = ((MainActivity)getActivity()).getUserNexcellLabel();
		
		defaultDate = new DateTime(1990, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
		newComerBirthday = defaultDate;

        String[] arraySpinner = new String[] {"New Comer", "Existing member"};

        Spinner s = (Spinner) rootView.findViewById(R.id.RegisTypespinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, arraySpinner);
        s.setAdapter(adapter);
		
		dateButton = (Button) rootView.findViewById(R.id.newComerBirthdayButton);
		
		dateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialog d = new DatePickerDialog(getActivity(), mDateSetListener, newComerBirthday.getYear(), newComerBirthday.getMonthOfYear()-1, newComerBirthday.getDayOfMonth());
				d.show();
			}
		});

        ScrollView sView = (ScrollView) rootView.findViewById(R.id.newComerScroll);
        sView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(rootView);
                return false;
            }
        });

        FloatingActionButton submitButton = (FloatingActionButton) rootView.findViewById(R.id.menu_item_1);
		
		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                hideKeyboard(rootView);
				UIDialog.onCreateActionDialog(getActivity(), "Submit", "Are you sure you want to submit?", submitInfoListener);
			}
		});

        FloatingActionButton clearButton = (FloatingActionButton) rootView.findViewById(R.id.menu_item_2);
		
		clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(rootView);
                UIDialog.onCreateActionDialog(getActivity(), "New Form", "Are you sure you want to start a new form?", clearDataListener);
            }
        });
		
		return rootView;
	}

    private void hideKeyboard(View view) {
        // TODO Auto-generated method stub
        FloatingActionMenu fabMenu = (FloatingActionMenu) view.findViewById(R.id.fab);
        if (fabMenu.isOpened()) fabMenu.close(true);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

	private void sendEmailInfo()
    {
        NewComerForm report = new NewComerForm(getActivity(), filePath, labelList, infoList);
        report.genReport();

        String toRecipients = ParseOperation.getNewComerFormRecipient(nexcell, getActivity());
        String ccRecipients = Constants.SYSTEM_GMAIL;

        SendMailAsync sendMail = new SendMailAsync(getActivity());
        sendMail.execute("New Comer for Nexcell " + nexcellLabel, "Let's welcome " + findValue("First Name") + "to Nexis. For more information, " +
                        "you can export member's information from Nexis android app!" , toRecipients, ccRecipients, "");
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
                clearInfo(v);
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
                ioList.add(((EditText) v).getText().toString());
            }
            else if (v instanceof TextView) {
                lbList.add(((TextView) v).getText().toString());
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
            else if(!(v instanceof FloatingActionMenu) && v instanceof ViewGroup && (((ViewGroup)v).getChildCount() > 0)) {
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

    private String findValue(String field)
    {
        for (int i = 0; i < labelList.size(); i++)
        {
            if (labelList.get(i).contains(field)) return infoList.get(i);
        }
        return "";
    }

    private void signupUser()
    {
        final String firstname = findValue("First Name");
        final String lastname = findValue("Last Name");
        final String initial = findValue("Initial");
        final String email = findValue("Email");

        Toast.makeText(getActivity(), "Creating New User...", Toast.LENGTH_LONG).show();

        pDialog = ProgressDialog.show(getActivity(), "Please wait ...", "Signing up new account ...", true);
        pDialog.setCancelable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("firstname", firstname);
                    params.put("lastname", lastname);
                    params.put("initial", initial);
                    params.put("nexcell", nexcell);
                    params.put("email", email);
                    params.put("levelname", Constants.USER_LEVEL_LIST);

                    ParseCloud.callFunctionInBackground("signupUser", params, new FunctionCallback<String>() {
                        public void done(String newUsername, ParseException e) {
                            if (e == null) {
                                pDialog.dismiss();
                                Toast.makeText(getActivity(), "User Created", Toast.LENGTH_LONG).show();
                                sendNewUserEmail(firstname + " " + lastname, newusername, email);
                            } else if (e.toString().contains("name exist")) {
                                pDialog.dismiss();
                                UIDialog.onCreateMsgDialog(getActivity(), "Name already exist!", "Your name already exist in the system. Please enter an initial or middle name");
                            } else if (e.toString().contains("email taken")) {
                                pDialog.dismiss();
                                UIDialog.onCreateMsgDialog(getActivity(), "Email Taken!", "This email address " + email + " already has an account");
                            }  else {
                                pDialog.dismiss();
                                UIDialog.onCreateErrorDialog(getActivity(), e.toString());
                            }
                        }
                    });
                } catch (Exception e) {
                    UIDialog.onCreateErrorDialog(getActivity(), e.toString());
                }
            }
        }).start();
    }

    private void sendNewUserEmail(String fullname, String username, String useremail)
    {
        String toRecipients = ParseOperation.getNexcellLeadersRecipient(nexcell, getActivity()) + "," + useremail;
        String ccRecipients = Constants.SYSTEM_GMAIL;

        DateTime currentTime = new DateTime();
        String currentDateTimeString = currentTime.toString("yyyy-MM-dd HH:mm:ss.SSS");

        String emailSubject = "Nexis user account for nexcell " + nexcell;

        String emailBody = String.format("Below is the details of the new account for nexcell %s\n\n"
                        + "Full Name: %s \nUsername: %s \nEmail: %s \n\nTime of Creation: %s",
                nexcellLabel, fullname, username, useremail, currentDateTimeString);

        SendMailAsync sendMail = new SendMailAsync(getActivity());
        sendMail.execute(emailSubject, emailBody, toRecipients, ccRecipients, "", "");
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

            signupUser();

            Spinner spn = (Spinner) getFragmentView().findViewById(R.id.RegisTypespinner);
            String type = (String)spn.getSelectedItem();

            if (type == "New Comer") sendEmailInfo();
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
