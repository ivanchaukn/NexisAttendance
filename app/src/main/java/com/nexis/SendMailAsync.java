package com.nexis;

import java.io.File;

import com.github.sendgrid.SendGrid;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class SendMailAsync extends AsyncTask<String, Void, String> {
	
	private SendGrid sender;
	private Context context;
	
	public SendMailAsync(Context context) {
		this.context = context;
	}

		protected void onPreExecute() {

			sender = new SendGrid(Constants.SENDGRID_USER_NAMAE, Constants.SENDGRID_PASSWORD);
		}

		protected String doInBackground(String... info) {
			try
			{
				String[] toRep = info[2].split(",");

				for(String rep: toRep)
					sender.addTo(rep);

				if (info[3] != null)
				{
					String[] ccRep = info[3].split(",");

					for(String rep: ccRep)
						sender.addTo(rep);
				}
			
			sender.setFrom(Constants.SYSTEM_GMAIL);
			sender.setSubject(info[0]);
			sender.setText(info[1]);
			
			String filePath = info[4];
			
			if (filePath != "")
			{
				File f = new File(info[4]);
				sender.addAttachment(f);
			}

			 String response = sender.send();
			 
			 if (response.contains("error"))
			 {
				 return "Send email";
			 }
			
		}
		catch(Exception e)
		{
			return e.toString();
		}
		
		return "";
    }
    
    protected void onPostExecute(String exception) {
    	super.onPostExecute(exception);
    	
		if (!exception.isEmpty())
		{
			UIDialog.onCreateErrorDialog(context, exception);
	    	return;
		}

		Toast.makeText(context, "Email Sent" , Toast.LENGTH_LONG).show();
    }
}