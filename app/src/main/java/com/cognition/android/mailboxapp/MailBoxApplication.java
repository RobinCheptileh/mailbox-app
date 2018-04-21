package com.cognition.android.mailboxapp;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.cognition.android.mailboxapp.models.AppDatabase;
import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

public class MailBoxApplication extends Application {

    private static Resources mResources;

    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();

        mResources = getResources();

        // DBFlow init
        FlowManager.init(new FlowConfig.Builder(this)
                .addDatabaseConfig(
                        new DatabaseConfig.Builder(AppDatabase.class)
                                .build())
                .build());
    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public static Resources getAppResources() {
        return mResources;
    }

}