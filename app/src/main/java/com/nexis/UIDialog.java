package com.nexis;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.nexis.DescriptionList.DescListAdapter;
import com.nexis.DescriptionList.DescListItem;

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

    static public void onCreateTextDialog(Context actv, View Viewlayout, String titleText, String subtitleText, DialogInterface.OnClickListener posLstn){
        AlertDialog.Builder builder = new AlertDialog.Builder(actv);

        TextView title = (TextView) Viewlayout.findViewById(R.id.tvDialogTitle);
        title.setText(subtitleText);

        builder.setView(Viewlayout);
        builder.setTitle(titleText);

        builder.setPositiveButton("Submit", posLstn)
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });

    AlertDialog d = builder.create();
    d.show();
}

    static public AlertDialog onCreateListViewDialog(Context actv, String titleText, ListView lv, boolean cc){
        AlertDialog.Builder builder = new AlertDialog.Builder(actv);

        ArrayList<DescListItem> objects = new ArrayList<>();

        objects.add(new DescListItem("Only Me", "Only you will receive the report", R.drawable.ic_person_black_24dp));

        if (cc) objects.add(new DescListItem("Committee and Counsellor", "All committees and counsellors in Nexis", R.drawable.ic_group_black_24dp));
        else objects.add(new DescListItem("Nexcell Leaders", "All leaders in the nexcell", R.drawable.ic_group_black_24dp));

        DescListAdapter adapter = new DescListAdapter(actv, objects);
        lv.setAdapter(adapter);

        builder.setView((View) lv.getParent());
        builder.setTitle(titleText);

        return builder.create();
    }
}
