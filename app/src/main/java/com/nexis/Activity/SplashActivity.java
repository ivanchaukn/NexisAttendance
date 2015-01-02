package com.nexis.Activity;

import com.nexis.ParseOperation;
import com.nexis.R;
import com.nexis.UIDialog;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

public class SplashActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		
		loadBackgroundData loadData = new loadBackgroundData();
		loadData.execute();
		
	}
	
	private DialogInterface.OnClickListener googlePlayListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int id) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+ "com.nexis")));
			endActivity();
		}
	}; 
	
	private void checkCurrentUser()
	{
		ParseUser currentUser = ParseUser.getCurrentUser();
		
    	if (currentUser != null) {
    		//Switch to Main Activity
		    Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            
            endActivity();
    	}
    	else
    	{
    		//Switch to Login Activity
		    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
            
            endActivity();
    	}
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
    			int mostRecentCode = ParseOperation.getMostRecentVersionCode(SplashActivity.this);
    			
    			PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
    			int versionNumber = pinfo.versionCode;
    			
    			if (mostRecentCode != versionNumber) return "Update";
    			else checkCurrentUser();
    		}
    		catch(Exception e)
    		{
    			return e.toString();
    		}
        	
        	return "";
        }
 
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            
            if (result != "")
            {
	            if (result == "Update")
	            {
	            	UIDialog.onCreateSimpleActionDialog(SplashActivity.this, "Update app", "A new version is available. Please update through google play!", googlePlayListener);
	            }
	            else 
	            {
	            	UIDialog.onCreateErrorDialog(SplashActivity.this, result + ". Version Code");
	            }
            }
        }
 
    }
	
}
