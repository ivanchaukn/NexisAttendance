package com.nexis;

import com.parse.Parse;
import com.parse.ParseCrashReporting;

import android.app.Application;
import android.content.Context;

public class NexisApplication extends Application {

    private static NexisApplication instance = new NexisApplication();

    public NexisApplication() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ParseCrashReporting.enable(this);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, Constants.PARSE_APPLICATION_ID, Constants.PARSE_CLEINT_KEY);
    }
}
