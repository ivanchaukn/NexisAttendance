package com.nexis;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Application;
import android.content.Context;

import java.util.List;

public class NexisApplication extends Application {

    private static boolean dev_ = false;
    private static boolean commi_ = false;
    private static boolean couns_ = false;
    private static boolean ESM_ = false;


    private static NexisApplication instance = new NexisApplication();

    public NexisApplication() {
        instance = this;
    }

    public static void setDev(boolean devVal){
        dev_ = devVal;
    }

    public static boolean getDev(){
        return dev_;
    }

    public static void setCommi(boolean commiVal){
        commi_ = commiVal;
    }

    public static boolean getCommi(){
        return commi_;
    }

    public static void setCouns(boolean counsVal){
        couns_ = counsVal;
    }

    public static boolean getCouns(){
        return couns_;
    }

    public static void setESM(boolean ESMVal){
        ESM_ = ESMVal;
    }

    public static boolean getESM(){
        return ESM_;
    }

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "n4gVHiC7PcB6fAvCkfMKSsAEyUODifl7JL33w3xT", "sag1RtZfVIVqJFhDbLTiHACtDPPCmBQbmdmOv670");
        /*
        ParseUser user = ParseUser.getCurrentUser();
        if(user != null) {
            try {
                ParseQuery<ParseObject> userQuery = ParseQuery.getQuery("_User");
                userQuery.whereEqualTo("username", user.getUsername());
                List<ParseObject> obj = userQuery.find();

                NexisApplication.setDev(obj.get(0).getBoolean("developer"));
                NexisApplication.setCommi(obj.get(0).getBoolean("committee"));
                NexisApplication.setCouns(obj.get(0).getBoolean("counsellor"));
                NexisApplication.setESM(obj.get(0).getBoolean("esm"));

            } catch (ParseException e) {
            }
        }
        */

    }
}
