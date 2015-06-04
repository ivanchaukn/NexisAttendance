package com.nexis;

import com.parse.Parse;

import android.app.Application;
import android.content.Context;

public class NexisApplication extends Application {

    private static boolean dev_;
    private static boolean commi_;
    private static boolean couns_;
    private static boolean ESM_;


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
    }
}
