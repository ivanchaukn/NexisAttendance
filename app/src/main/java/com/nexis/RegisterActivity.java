package com.nexis;

import java.util.ArrayList;
import java.util.List;

import com.nexis.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
 
public class RegisterActivity extends Activity {
	
	static String USER = "User";
	List<String> nexcellList = new ArrayList<String>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set View to register.xml
        setContentView(R.layout.register);
        
        //Hide the keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        
        final Spinner nexcellSpin = (Spinner) findViewById(R.id.nexcellSpinner);
        
        List<ParseObject> nexcellObject = ParseOperation.getNexcellList(true, this);        
        for(ParseObject x: nexcellObject) nexcellList.add(x.get("Name").toString());

        populateSpinner(nexcellList, nexcellSpin);
 
        TextView loginScreen = (TextView) findViewById(R.id.link_to_login);
 
        // Listening to Login Screen link
        loginScreen.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View arg0) {
                // Closing registration screen
                // Switching to Login Screen/closing register screen
                finish();
            }
        });
        
        //Register new user
//        Button register = (Button) findViewById(R.id.btnRegister);
//        
//        register.setOnClickListener(new View.OnClickListener() {
// 
//            public void onClick(View arg0) {
//               String[] info = preCheck();
//               
//               String selectedNexcell = nexcellSpin.getSelectedItem().toString();
//               if (info.length != 0) addNewUser(info, selectedNexcell);
//            }
//        });
    }
    
    private void populateSpinner(List<String> nexcellList, Spinner nexcellSpin)
    {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter <CharSequence> (this, R.drawable.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        for(String x : nexcellList) adapter.add(x);
        
        nexcellSpin.setAdapter(adapter);
    }
    
    private String[] preCheck()
    {
    	EditText username = (EditText) findViewById(R.id.reg_username);
    	EditText password = (EditText) findViewById(R.id.reg_password);
    	EditText repassword = (EditText) findViewById(R.id.reg_repassword);
    	EditText email = (EditText) findViewById(R.id.reg_email);
    	
    	String[] info = new String[]{}; 
    	
    	if (username.length() == 0 || password.length() == 0  || email.length() == 0)
    	{
    		UIDialog.onCreateInvalidDialog(RegisterActivity.this, "Please enter all fields to register an account!");
    	}
    	else if (!repassword.getText().toString().equals(password.getText().toString()))
    	{
    		UIDialog.onCreateInvalidDialog(RegisterActivity.this, "Password does not match! Please try again");
    	}
    	else
    	{
    		info = new String []{username.getText().toString(), password.getText().toString(), email.getText().toString()}; 
    	}
    	
    	return info;
    }
    
    private void addNewUser(final String[] info, String nexcell)
    {
    	ParseUser user = new ParseUser();
    	user.setUsername(info[0]);
    	user.setPassword(info[1]);
    	user.setEmail(info[2]);
    	
    	user.put("Nexcell", nexcell);

    	user.signUpInBackground(new SignUpCallback() {
    		  public void done(ParseException e) {
    		    if (e == null) {
    		    	
    		    	ParseObject userLevel = new ParseObject("UserLevelMap");
    		    	userLevel.put("username", info[0]);
    		    	userLevel.put("level", 1);
    		    	userLevel.saveInBackground();
    		    	
    		    	UIDialog.onCreateMsgDialog(RegisterActivity.this, "Congratulation!", "You have successfully registered to the Nexis Android App! Please LOGIN to access to the Nexis App");
    		    	
    		    	//Switch back to Login
                    endCurrentActivity();
    		    } else {    		   
    		    	UIDialog.onCreateErrorDialog(RegisterActivity.this, e.toString());
    		    }
    		  }
		});
    }
    
    private void endCurrentActivity()
    {
    	this.finish();
    }
}