package com.nexis.Activity;

import com.nexis.Data;
import com.nexis.GeneralOperation;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.UIDialog;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity{

    private boolean networkConnection;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

        networkConnection = false;

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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (currentUser != null) {

                    String nexcell = (String) currentUser.get("nexcell");
                    Data.setupApp(nexcell, networkConnection, SplashActivity.this);

                    //Switch to Main Activity
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);

                    endActivity();
                } else {
                    //Switch to Login Activity
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(i);

                    endActivity();
                }
            }
        }, 0);
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
                boolean conn = GeneralOperation.checkNetworkConnection(SplashActivity.this);

                if (conn)
                {
                    networkConnection = true;
                    return GeneralOperation.checkVersionCode(SplashActivity.this);
                }
            }
            catch(Exception e)
            {
                return e.toString();
            }

            return "Success";
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
	            else if (result == "Update")
                {
                    GeneralOperation.promptUpdate(SplashActivity.this);
	            }
	            else 
	            {
	            	UIDialog.onCreateErrorDialog(SplashActivity.this, result + ". Version Code");
	            }
            }
        }
 
    }
}
