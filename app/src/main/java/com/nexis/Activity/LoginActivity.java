package com.nexis.Activity;

import com.nexis.R;
import com.nexis.UIDialog;
import com.parse.LogInCallback;
import com.parse.ParseUser;
import com.parse.ParseException;
import com.parse.RequestPasswordResetCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
 
public class LoginActivity extends Activity {
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        setContentView(R.layout.login);        
        
        //Hide the keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        
        Button loginButton = (Button) findViewById(R.id.btnLogin);
        
        loginButton.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View v) {
            	//turnOnProgressDialog("Login","Wait while I log you in");
            	EditText username = (EditText) findViewById(R.id.EditTextUsername);
            	EditText password = (EditText) findViewById(R.id.EditTextPassword);
            	
                ParseUser.logInInBackground(username.getText().toString(), password.getText().toString(), new LogInCallback() {
                	
             	   @Override
             	   public void done(ParseUser user, ParseException e) {
             		     if (e == null && user != null) {
             		    	//Switch to Main Activity
             		    	Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                            endCurrentActivity();
             		     } else if (user == null) {
             		    	UIDialog.onCreateInvalidDialog(LoginActivity.this, "Invalid Username or password, please try again!");
             		     } else {
             		    	UIDialog.onCreateErrorDialog(LoginActivity.this, "Login Error, please contact administrator!");
             		     }
             	   }
             });
            }
        });
 
//        TextView registerScreen = (TextView) findViewById(R.id.link_to_signup);
// 
//        // Listening to register new account link
//        registerScreen.setOnClickListener(new View.OnClickListener() {
// 
//            public void onClick(View v) {
//                // Switching to Register screen
//                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
//                startActivity(i);
//            }
//        });
        
        TextView resetPasswordText = (TextView) findViewById(R.id.resetPwdText);
        
        // Listening to register new account link
        resetPasswordText.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View v) {
            	pwdResetDialog();
            }
        });
    }
 
    protected void pwdResetDialog() {
    	Dialog d = passwordResetDialog();
    	d.show();
	}
    
    private AlertDialog passwordResetDialog()
	{
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    
	    LayoutInflater inflater = this.getLayoutInflater();

	    // Inflate and set the text for the dialog
	    // Pass null as the parent view because its going in the dialog text
	    final View Viewlayout = inflater.inflate(R.layout.pwdresetdialog, null);
	    builder.setView(Viewlayout);
	    
	    builder.setTitle("Reset Password");
	    
	    builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                  
	            	   EditText userEmail = (EditText) Viewlayout.findViewById(R.id.resetPwdEmail);
	            	   
	            	   ParseUser.requestPasswordResetInBackground(userEmail.getText().toString(), new RequestPasswordResetCallback() {
							public void done(ParseException e) {
								if (e == null) {
									Toast.makeText(LoginActivity.this, "Email Sent", Toast.LENGTH_LONG).show();
								} else {
									Toast.makeText(LoginActivity.this, "Invalid Email, Please check again", Toast.LENGTH_LONG).show();
								}
							}
	            	   });
	               }
	           })
	           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {		          
	               }
	    });
	    
	    return builder.create();		
	}
    
    private void endCurrentActivity()
    {
    	this.finish();
    }
}
