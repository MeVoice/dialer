package com.demitive.callrouter;

import android.content.Context;
import android.content.SharedPreferences;

import com.demitive.callrouter.model.AppData;
import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by Admin on 8/11/2016.
 */
public class AppDataAccess {
    public AppData getAppdata(){
        if(appdata==null){
            loadAppData();
        }
        return appdata;
    }

    public void setAppdata(AppData appdata) {
        this.appdata = appdata;
    }

    private static AppDataAccess instance = null;
    private AppData appdata=null;
    private SharedPreferences sharedPref;
    private Context appContext;
    private SharedPreferences.Editor prefEditor;

    public Object getRuntimeData(String key) {
        return runtimeData.get(key);
    }
    public void setRuntimeData(String key, Object data) {
        runtimeData.put(key, data);
    }

    private HashMap runtimeData=new HashMap();

    static private final String preferenceName="e.164 dialer AppData";
    static private final String appDataName="gson_AppData";
    protected AppDataAccess(Context context){
        appContext = context;
        sharedPref = appContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        prefEditor = sharedPref.edit();
    }
    public static AppDataAccess getInstance(Context context){
        if(instance == null){
            instance = new AppDataAccess(context);
        }
        return instance;
    }
    public void saveAppData(){
        Gson gson = new Gson();
        String json = gson.toJson(this.appdata);
        prefEditor.putString(appDataName, json);
        prefEditor.commit();
    }
    public AppData loadAppData(){
        Gson gson = new Gson();
        String json = sharedPref.getString(appDataName, "");
        appdata = gson.fromJson(json, AppData.class);
        if(appdata==null){
            appdata = new AppData();
        }
        return appdata;
    }
}
