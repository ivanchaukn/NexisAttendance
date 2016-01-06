package com.nexis.Fragments;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.nexis.Constants;
import com.nexis.Activity.MainActivity;
import com.nexis.Data;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.UIDialog;
import com.nexis.SendMailAsync;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.util.ArrayMap;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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

public class FragmentRegistration extends DialogFragment {

	private String nexcell, nexcellLabel, filePath;
	private DateTime newComerBirthday, defaultDate, nextDate;
	private Button dateButton;

    private Spinner s, s2, s3;
    private ArrayAdapter<String> adapter, adapter2, adapter3;

    ProgressDialog pDialog;

    private List<String> labelList = new ArrayList<>();
    private List<String> infoList = new ArrayList<>();

    private List<String> schoolSpinner = new ArrayList<>();
    private List<String> yearSpinner = new ArrayList<>();
    private List<String> regisSpinner = Arrays.asList("New Comer", "Existing member");

	public static FragmentRegistration newInstance() {
		FragmentRegistration fragment = new FragmentRegistration();
		Bundle args = new Bundle();
		args.putInt("Registration", 3);
		fragment.setArguments(args);
		return fragment;
	}

	public FragmentRegistration() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_newcomer, container, false);

        setupDate();

        filePath = getActivity().getFilesDir().getPath().toString() + "/Registration.xls";

        nexcell = ((MainActivity)getActivity()).getUserNexcell();
        nexcellLabel = ((MainActivity)getActivity()).getUserNexcellLabel();
		
		defaultDate = new DateTime(1990, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
		newComerBirthday = defaultDate;

        EditText cPhoneText = (EditText) rootView.findViewById(R.id.newComerCellPhone);
        cPhoneText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        setupSpinners(rootView);
		
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
                UIDialog.onCreateActionDialog(getActivity(), "Reset Form", "Are you sure you want to start a new form?", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        clearInfo(getFragmentView());
                    }
                });
            }
        });
		
		return rootView;
	}

    private void setupDate()
    {
        nextDate = new DateTime(DateTimeZone.UTC);
        DateTime friday = nextDate.withDayOfWeek(DateTimeConstants.FRIDAY);
        if (nextDate.isBefore(friday)) nextDate = friday.minusWeeks(1);
        else nextDate = friday;

        nextDate = nextDate.withTimeAtStartOfDay();
    }

    private void setupSpinners(View rootView)
    {
        setupSpinnerValues();

        s = (Spinner) rootView.findViewById(R.id.RegisTypespinner);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, regisSpinner);
        s.setAdapter(adapter);

        s2 = (Spinner) rootView.findViewById(R.id.schoolSpinner);
        adapter2 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, schoolSpinner);
        s2.setAdapter(adapter2);

        s3 = (Spinner) rootView.findViewById(R.id.schoolYearSpinner);
        adapter3 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, yearSpinner);
        s3.setAdapter(adapter3);

        s2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String items = s2.getSelectedItem().toString();
                if (items.equals("*Working"))
                {
                    s3.setEnabled(false);
                    adapter3.add("N/A");
                    adapter3.notifyDataSetChanged();
                    s3.setSelection(adapter3.getPosition("N/A"), true);
                }
                else
                {
                    s3.setEnabled(true);
                    adapter3.remove("N/A");
                    adapter3.notifyDataSetChanged();
                    if (items.equals("Others...")) schoolDialog();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    protected void schoolDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View Viewlayout = inflater.inflate(R.layout.textviewdialog, null);

        UIDialog.onCreateTextDialog(getActivity(), Viewlayout, "New School", "Please enter your school:", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                EditText school = (EditText) Viewlayout.findViewById(R.id.inputText);
                adapter2.add(school.getText().toString());
                adapter2.notifyDataSetChanged();
                s2.setSelection(adapter2.getPosition(school.getText().toString()), true);
            }
        });
    }

    private void setupSpinnerValues()
    {
        yearSpinner.clear();
        schoolSpinner.clear();

        List<String> years = Arrays.asList("Grade 8", "Grade 9", "Grade 10", "Grade 11",
                                           "Grade 12", "Year 1", "Year 2", "Year 3", "Year 4");
        yearSpinner.addAll(years);

        schoolSpinner.addAll(Data.SCHOOL_LIST);
        schoolSpinner.add("Others...");
    }

    private void hideKeyboard(View view) {
        // TODO Auto-generated method stub
        FloatingActionMenu fabMenu = (FloatingActionMenu) view.findViewById(R.id.fab);
        if (fabMenu.isOpened()) fabMenu.close(true);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
            else if (v instanceof Spinner) {
                ((Spinner)v).setSelection(0);
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
            else if (v instanceof Spinner) {
                ioList.add(((Spinner) v).getSelectedItem().toString());
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
            if (labelList.get(i).contains(field)) return infoList.get(i).trim();
        }
        return "";
    }

    private String formatName(String name)
    {
        String fLetter = name.substring(0, 1);
        String rWord = name.substring(1, name.length());

        return fLetter.toUpperCase() + rWord.toLowerCase();
    }

    private void signupUser(View view)
    {
        final String firstname = formatName(findValue("First Name"));
        final String lastname = formatName(findValue("Last Name"));
        final String initial = findValue("Initial");
        final String email = findValue("Email");
        final String gender = findValue("Gender");
        final String phone = findValue("Cell Phone");

        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy/MM/dd");
        DateTime birthdayDate = f.parseDateTime(findValue("Birthday"));
        birthdayDate = birthdayDate.withZone(DateTimeZone.UTC).withTimeAtStartOfDay();
        final Date birthday = birthdayDate.toDate();

        final String school = findValue("School");
        final String schoolYear = findValue("Grade/Year");
        final String christian = findValue("Are you a Christian");
        final String baptized = findValue("Are you Baptized");
        final String yearAccepted = findValue("Year Accepted");
        final String yearBaptized = findValue("Year Baptized");

        final EditText init = (EditText)view.findViewById(R.id.newComerInitial);

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
                    params.put("gender", gender);
                    params.put("phone", phone);

                    final String tempPwd = passwordGenerator();
                    params.put("password", tempPwd);

                    params.put("birthday", birthday);
                    params.put("school", school);
                    params.put("schoolYear", schoolYear);
                    params.put("christian", christian);
                    params.put("yearAccepted", yearAccepted);
                    params.put("baptized", baptized);
                    params.put("yearBaptized", yearBaptized);
                    params.put("levelname", Constants.USER_LEVEL_LIST);

                    ParseCloud.callFunctionInBackground("signupUser", params, new FunctionCallback<String>() {
                        public void done(String newUsername, ParseException e) {
                            if (e == null) {
                                pDialog.dismiss();
                                Toast.makeText(getActivity(), "User Created", Toast.LENGTH_LONG).show();

                                String fullname;
                                if (initial.equals("")) fullname = firstname + " " + lastname;
                                else fullname = firstname + " " + initial + " " + lastname;

                                sendNewUserEmail(fullname, newUsername, tempPwd, email);

                                Spinner spn = (Spinner) getFragmentView().findViewById(R.id.RegisTypespinner);
                                String type = (String)spn.getSelectedItem();

                                if (type == "New Comer") {
                                    sendWelcomeEmail();

                                    String uName = ((MainActivity)getActivity()).getUserName();
                                    ParseOperation.saveNewComer(nextDate, nexcell, newUsername, uName);
                                }

                                ArrayMap<String, String> currMap = Data.getNexcellMemberNameMap(nexcell, getActivity());
                                currMap.put(newUsername, fullname);
                                ((MainActivity)getActivity()).setNexcellUserMap(currMap);

                            } else if (e.toString().contains("invalid email address")) {
                                pDialog.dismiss();
                                UIDialog.onCreateMsgDialog(getActivity(), "Invalid Email Address", "Please enter a valid email address to create new user");
                            } else if (e.toString().contains("name exist")) {
                                pDialog.dismiss();
                                UIDialog.onCreateMsgDialog(getActivity(), "Name already exist!", "Your name already exist in the system. Please enter an initial or middle name");
                                init.setEnabled(true);
                                init.setHint("Tap to enter...");
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

    private void sendWelcomeEmail()
    {
        String toRecipients = Data.getNewComerFormRecipient(nexcell, getActivity());
        String ccRecipients = Constants.SYSTEM_GMAIL;

        SendMailAsync sendMail = new SendMailAsync(getActivity());
        sendMail.execute("New Comer for Nexcell " + nexcellLabel, "Let's welcome " + findValue("First Name") + " to Nexis. For more information, " +
                "you can export member's information from the Nexis mobile app!", toRecipients, ccRecipients, "");
    }

    private void sendNewUserEmail(String fullname, String username, String pwd, String useremail)
    {
        String toRecipients = useremail;
        String ccRecipients = Constants.SYSTEM_GMAIL;

        DateTime currentTime = new DateTime();
        String currentDateTimeString = currentTime.toString("yyyy-MM-dd HH:mm:ss.SSS");

        String emailSubject = "Nexis user account for nexcell " + nexcell;

        String emailBody = String.format("Below is the details of the new account for nexcell %s\n\n"
                        + "Full Name: %s \nUsername: %s \n" + "Temporary Password: %s \nEmail: %s \n\nTime of Creation: %s",
                nexcellLabel, fullname, username, pwd, useremail, currentDateTimeString);

        SendMailAsync sendMail = new SendMailAsync(getActivity());
        sendMail.execute(emailSubject, emailBody, toRecipients, ccRecipients, "", "");
    }

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			
			newComerBirthday = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0, 0, 0, DateTimeZone.UTC);

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

            signupUser(getFragmentView());
		}
	};

    public String passwordGenerator()
    {
        SecureRandom random = new SecureRandom();
        return new BigInteger(50, random).toString(32);
    }
	
	public View getFragmentView()
	{
		return this.getView();
	}
}
