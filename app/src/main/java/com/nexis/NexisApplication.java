package com.nexis;

import com.parse.Parse;

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

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "n4gVHiC7PcB6fAvCkfMKSsAEyUODifl7JL33w3xT", "sag1RtZfVIVqJFhDbLTiHACtDPPCmBQbmdmOv670");
    }
}
