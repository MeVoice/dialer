package com.ebookfrenzy.dialerintent;

import android.support.v4.app.Fragment;
import android.content.Context;

import com.ebookfrenzy.dialerintent.model.AppData;

/**
 * Created by Admin on 8/19/2016.
 */
public class CommonFragment extends Fragment {
    public Context context;
    public AppDataAccess appdataaccess;
    public AppData appdata;

    public CommonFragment() {
    }
    public void initAppData(){
        context = getActivity();
        appdataaccess = AppDataAccess.getInstance(context);
        appdata = appdataaccess.getAppdata();

    }
}
