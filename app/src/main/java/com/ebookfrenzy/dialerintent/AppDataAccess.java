package com.ebookfrenzy.dialerintent;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.ebookfrenzy.dialerintent.model.AppData;
import com.ebookfrenzy.dialerintent.model.RuleGroup;
import com.google.gson.Gson;

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
    static private final String preferenceName="e.164 dialer AppData";
    static private final String appDataName="gson_AppData";
    protected AppDataAccess(Context context){
        appContext = context;
        sharedPref = appContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        prefEditor = sharedPref.edit();
    }
    public static AppDataAccess getInstance(Context context){
        if(context==null){
            System.out.println("context is null");
        }
        if(instance == null){
            instance = new AppDataAccess(context);
        }
        return instance;
    }
    public void saveAppData(AppData appdata){
        Gson gson = new Gson();
        String json = gson.toJson(appdata);
        prefEditor.putString(appDataName, json);
        prefEditor.commit();
    }
    public AppData loadAppData(){
        Gson gson = new Gson();
        String json = sharedPref.getString(appDataName, "");
        appdata = gson.fromJson(json, AppData.class);
        if(appdata==null){
            appdata = new AppData();
            appdata.addRuleGroup(new RuleGroup("group1"));
        }
        if(appdata.getRuleGroups().size()==0) {
            appdata.addRuleGroup(new RuleGroup("group1"));
            appdata.addRuleGroup(new RuleGroup("group2"));
        }
        return appdata;
    }
}
