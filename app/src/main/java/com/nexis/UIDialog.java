package com.nexis;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;


public class UIDialog {

    static public void onCreateErrorDialog(Context actv, String errorType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(actv);
        builder.setMessage(String.format("%s Error, please contact administrator!", errorType));
        builder.setTitle("Error");
        builder.setIcon(R.drawable.ic_action_error);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
            }
        });

        AlertDialog d = builder.create();
        d.show();
    }

    static public void onCreateInvalidDialog(Context actv, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(actv);
        builder.setMessage(String.format("%s", msg));
        builder.setTitle("Invalid");
        builder.setIcon(R.drawable.ic_action_warning);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
            }
        });

        AlertDialog d = builder.create();
        d.show();
    }

    static public void onCreateMsgDialog(Context actv, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(actv);
        builder.setMessage(String.format("%s", msg));
        builder.setTitle(title);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) 
             {
             }
        });
        
        AlertDialog d = builder.create();
        d.show();
	}
	
	static public void onCreateSimpleActionDialog(Context actv, String title, String msg, DialogInterface.OnClickListener buttonListener)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(actv);
        builder.setTitle(title);
        
        builder.setMessage(msg)
	   		  .setPositiveButton("Ok", buttonListener);

        AlertDialog d = builder.create();
        d.show();
	}
	
	static public void onCreateActionDialog(Context actv, String title, String msg, DialogInterface.OnClickListener positiveButtonListener)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(actv);
        builder.setTitle(title);
        
        builder.setMessage(msg)
	   		  .setPositiveButton("Yes", positiveButtonListener)
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	               }
	    });

        AlertDialog d = builder.create();
        d.show();
	}
	
	static public void onCreateListDialog(Context actv, String title, List<String> list, DialogInterface.OnClickListener clickListener)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(actv);
        builder.setTitle(title);
	    
	    if (list != null)
	    {
	    	final CharSequence[] nList = list.toArray(new CharSequence[list.size()]);
	    	builder.setItems(nList, clickListener);
	    }
	    
	    AlertDialog d = builder.create();
        d.show();
	}
}
