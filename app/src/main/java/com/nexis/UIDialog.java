package com.nexis;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;


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
            public void onClick(DialogInterface dialog, int id) {
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
	
	static public void onCreateMultiChoiceListDialog(Context actv, String title, List<String> list, boolean[] blList, DialogInterface.OnMultiChoiceClickListener clickListener,
                                                      String posButton, String negButton, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(actv);
        builder.setTitle(title);

        final CharSequence[] nList = list.toArray(new CharSequence[list.size()]);
        boolean bl[] = blList;
        builder.setMultiChoiceItems(nList, bl, clickListener);

        if (posButton != null) builder.setPositiveButton(posButton, positiveListener);
        if (negButton != null) builder.setNegativeButton(negButton, negativeListener);

        AlertDialog d = builder.create();
        d.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        d.show();
    }

    static public void onCreateCustomDialog(Context actv, String title, View Viewlayout, String posButton, String negaButton,
                                            DialogInterface.OnClickListener positiveButtonListener, DialogInterface.OnClickListener negativeButtonListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(actv);

        //LayoutInflater inflater = ((Activity)actv).getLayoutInflater();

        // Inflate and set the text for the dialog
        // Pass null as the parent view because its going in the dialog text
        builder.setView(Viewlayout);

        builder.setTitle(title);

        builder.setPositiveButton(posButton, positiveButtonListener);

        if (negaButton != "") {
            builder.setNegativeButton(negaButton, negativeButtonListener);
        }

        AlertDialog d = builder.create();
        d.show();
    }
}
