package com.nexis.Activity;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.nexis.NavigationDrawer.NavigationDrawerAdapter;
import com.nexis.R;
import com.nexis.UIDialog;
import com.parse.LogInCallback;
import com.parse.ParseUser;
import com.parse.ParseException;
import com.parse.RequestPasswordResetCallback;
import com.gc.materialdesign.views.ButtonRectangle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
 
public class LoginActivity extends Activity {

    ProgressBarCircularIndeterminate progressCircle;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        setContentView(R.layout.activity_login);

        //Hide the keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        final ButtonRectangle loginButton = (ButtonRectangle) findViewById(R.id.loginButton);

        progressCircle = (ProgressBarCircularIndeterminate) findViewById(R.id.progressCircular);

        loginButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                final EditText username = (EditText) findViewById(R.id.userNameLogIn);
                final EditText password = (EditText) findViewById(R.id.userPwLogIn);

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(password.getWindowToken(), 0);

                fadeOutButton(loginButton);

                ParseUser.logInInBackground(username.getText().toString(), password.getText().toString(), new LogInCallback() {

                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e == null && user != null) {
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                            endCurrentActivity();
                            return;
                        } else if (user == null) {
                            UIDialog.onCreateInvalidDialog(thisActivity(), "Invalid Username or password, please try again!");
                        } else {
                            UIDialog.onCreateErrorDialog(thisActivity(), "Login Error, please contact administrator!");
                        }
                        showButton(loginButton);
                    }
                });
            }
        });

        TextView resetPasswordText = (TextView) findViewById(R.id.resetPwdText);
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

    private void fadeOutButton(ButtonRectangle button)
    {
        Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
        button.startAnimation(fadeout);
        fadeout.setDuration(500);
        fadeout.setFillAfter(true);

        progressCircle.setVisibility(View.VISIBLE);
    }

    private void showButton(ButtonRectangle button)
    {
        Animation fadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        button.startAnimation(fadein);

        progressCircle.setVisibility(View.INVISIBLE);
    }
    
    private void endCurrentActivity()
    {
    	this.finish();
    }

    private LoginActivity thisActivity()
    {
        return this;
    }
}
