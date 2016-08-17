package com.ebookfrenzy.dialerintent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.ebookfrenzy.dialerintent.events.ClickEvent;
import com.ebookfrenzy.dialerintent.model.AppData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    public AppData getAppdata() {
        return appdata;
    }

    private static final int PERMISSION_REQUEST_CODE = 101;

    private AppData appdata;

    public AppDataAccess getAppDataAccess() {
        return appDataAccess;
    }

    private AppDataAccess appDataAccess;
    private EventBus bus = EventBus.getDefault();
    private HashMap fragments = new HashMap();
    private HashMap fragmentIDs = new HashMap();
    private String[] requiredPermissions = new String[] {Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.CALL_PHONE};


    @Override
    public void onStart() {
        super.onStart();
        bus.register(this); // registering the bus
    }

    @Override
    public void onStop() {
        bus.unregister(this); // un-registering the bus
        super.onStop();
    }

    @Subscribe
    public void onClickEvent(ClickEvent event) {
        if (event.get("action") == Constant.ACTION_EDIT_RULES) {
            showFragment("rules", true, "Rules - " + appdata.getRuleGroups().get(appdata.getGroupInEdit()).getName());
            //Toast.makeText(getApplicationContext(), "show rules for group " + appdata.getGroupInEdit(), Toast.LENGTH_SHORT).show();
        }
        if (event.get("action") == Constant.ACTION_REQUEST_PERMISSION) {
            checkAndRequestPermissions(requiredPermissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        String fragmentName = (String) fragmentIDs.get(id);
        //user should use either menu OR back button, not both
        clearBackStack();
        String title = "";
        if (fragmentName == "features") {
            title = "Choose Features";
        }
        if (fragmentName == "groups") {
            title = "Rule Groups";
        }

        showFragment(fragmentName, false, title);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDataAccess = AppDataAccess.getInstance(getApplicationContext());
        appdata = appDataAccess.getAppdata();

        setContentView(R.layout.activity_main2);
        // Adding Toolbar to Main screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Setting ViewPager for each Tabs
        fragments.put("features", new FeaturesFragment());
        fragmentIDs.put(R.id.action_features, "features");
        fragments.put("groups", new GroupsFragment());
        fragmentIDs.put(R.id.action_groups, "groups");
        fragments.put("rules", new RulesFragment());

        showFragment("features", false, "Choose Features");
    }

    public void clearBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = fm.getBackStackEntryAt(0);
            fm.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void showFragment(String key, boolean addToStack, String title) {
        getSupportActionBar().setTitle(title);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content, (Fragment) fragments.get(key));
        if (addToStack) {
            ft.addToBackStack(null);
        }
        ft.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("called onRequestPermissionsResult");
        int permission_denied = 0;
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length == 0) {
                    permission_denied++;
                } else {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            permission_denied++;
                        }
                    }
                }
            }
        }
        System.out.println(permission_denied + " permissions denied");
        ClickEvent ev = new ClickEvent();
        if (permission_denied > 0) {
            ev.put("action", Constant.ACTION_PERMISSION_DENIED);
        } else {
            ev.put("action", Constant.ACTION_PERMISSION_GRANTED);
        }
        bus.post(ev);
    }
    protected void checkAndRequestPermissions(String[] permissions, int requestcode){
        List<String> permList = new ArrayList<String>();
        for(int i=0; i<permissions.length; i++){
            int isGranted = ContextCompat.checkSelfPermission(getApplicationContext(), permissions[i]);
            if(isGranted!= PackageManager.PERMISSION_GRANTED){
                permList.add(permissions[i]);
            }
        }
        if(permList.size()>0) {
            ActivityCompat.requestPermissions(this, permList.toArray(new String[permList.size()]), requestcode);
            System.out.println("calling requestPermissions");
        }else{
            ClickEvent ev = new ClickEvent();
            ev.put("action", Constant.ACTION_PERMISSION_GRANTED);
            bus.post(ev);
        }
    }
}
