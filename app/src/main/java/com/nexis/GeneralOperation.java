package com.nexis;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

public class GeneralOperation {

    static public boolean checkNetworkConnection(Context actv)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) actv.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifi.getState() == NetworkInfo.State.CONNECTED ||
                mobile.getState() == NetworkInfo.State.CONNECTED)
        {
            return true;
        } else {
            return false;
        }
    }

    static public String checkVersionCode(Context actv)
    {
        try
        {
            int mostRecentCode = ParseOperation.getMostRecentVersionCode(actv);

            PackageInfo pinfo = actv.getPackageManager().getPackageInfo(actv.getPackageName(), 0);
            int versionNumber = pinfo.versionCode;

            if (mostRecentCode > versionNumber) return "Update";
            else return "Success";
        }
        catch(Exception e)
        {
            return e.toString();
        }
    }

    static public void promptUpdate(final Context actv)
    {
        UIDialog.onCreateSimpleActionDialog(actv, "Update app", "A new version is available. Please update through google play!", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                actv.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.nexis")));
                ((Activity)actv).finish();
            }
        });
    }

    static public boolean getCacheLevel(Context actv, String lev)
    {
        SharedPreferences cache = actv.getSharedPreferences("levels", Context.MODE_PRIVATE);
        return cache.getBoolean(lev, false);
    }

    static public boolean hasRole(Context actv)
    {
        for(String x : Constants.USER_LEVEL_LIST)
        {
            if (getCacheLevel(actv,x)) return true;
        }
        return false;
    }
}
