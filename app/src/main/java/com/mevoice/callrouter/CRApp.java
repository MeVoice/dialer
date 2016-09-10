package com.mevoice.callrouter;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.database.DatabaseUtils;

import com.mevoice.callrouter.model.AppData;

/**
 * Created by Admin on 9/5/2016.
 */
public class CRApp extends Application {
    public static Resources resources;
    public static AppDataAccess appdataaccess;
    public static AppData appdata;
    public static Context context;

    public void onCreate()
    {
        super.onCreate();

        // Initialize the singletons so their instances
        // are bound to the application process.
        initAppData();
    }
    public void initAppData(){
        appdataaccess = AppDataAccess.getInstance(this);
        appdata = appdataaccess.getAppdata();
        resources = getResources();
        context = this;
    }
}
