package com.nexis.Activity;

import com.nexis.Data;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.UIDialog;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

        //Initialize global variables
        ParseOperation.saveYearDate(this);
        ParseOperation.refreshAttendanceLocalData(this);
        Data.initializeNexcell(this);

		loadBackgroundData loadData = new loadBackgroundData();
		loadData.execute();
	}

    private DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int id) {
            System.exit(0);
        }
    };

	private void checkCurrentUser()
	{
		final ParseUser currentUser = ParseUser.getCurrentUser();

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                if (currentUser != null) {
                    //Switch to Main Activity
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);

                    endActivity();
                }
                else {
                    //Switch to Login Activity
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(i);

                    endActivity();
                }
            }
        }, 2000);
	}
	
	private void endActivity()
	{
		this.finish();
	}
	
	private class loadBackgroundData extends AsyncTask<Void, Void, String> {
		 
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
 
        @Override
        protected String doInBackground(Void... arg0) {

            try
            {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (wifi.getState() != NetworkInfo.State.CONNECTED)
                {
                    if (mobile != null && mobile.getState() != NetworkInfo.State.CONNECTED)
                    {
                        return "NoConnection";
                    }
                }
            }
            catch(Exception e)
            {
                return e.toString();
            }

            return Data.checkVersionCode(SplashActivity.this);
        }
 
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            
            if (result != "")
            {
                if (result == "Success")
                {
                    checkCurrentUser();
                }
                else if(result == "NoConnection")
                {
                    UIDialog.onCreateSimpleActionDialog(SplashActivity.this, "No Network Connection", "Mobile Data turned off. Connect to Wi-Fi network instead or turn on mobile data and try again.", exitListener);
                }
	            else if (result == "Update")
	            {
                    Data.promptUpdate(getApplicationContext());
	            }
	            else 
	            {
	            	UIDialog.onCreateErrorDialog(SplashActivity.this, result + ". Version Code");
	            }
            }
        }
 
    }
}
