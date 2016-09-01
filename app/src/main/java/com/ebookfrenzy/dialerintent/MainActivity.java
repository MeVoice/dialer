package com.ebookfrenzy.dialerintent;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import com.ebookfrenzy.dialerintent.events.ClickEvent;
import com.ebookfrenzy.dialerintent.model.AppData;
import com.ebookfrenzy.dialerintent.model.RuleGroup;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private AppData appdata;

    private FloatingActionButton fab;

    private AppDataAccess appDataAccess;
    private EventBus bus = EventBus.getDefault();
    private HashMap fragments = new HashMap();
    private HashMap fragmentTitles = new HashMap();
    private HashMap fragmentIDs = new HashMap();

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
        if (event.get("action").equals(Constant.ACTION_EDIT_RULES)) {
            RuleGroup rg = appdata.getRuleGroups().get(appdata.getGroupInEdit());
            if(rg.getRules().size()>0) {
                fragmentTitles.put(Constant.FRAGMENT_KEY_RULES, "Rules - " + rg.getName());
                showFragment(Constant.FRAGMENT_KEY_RULES, true);
            }else{
                fragmentTitles.put(Constant.FRAGMENT_KEY_RULE_ADD, "+ Rule - " + rg.getName());
                showFragment(Constant.FRAGMENT_KEY_RULE_ADD, true);
            }
            //Toast.makeText(getApplicationContext(), "show rules for group " + appdata.getGroupInEdit(), Toast.LENGTH_SHORT).show();
        }
        if (event.get("action").equals(Constant.ACTION_ROUTER_ON_OFF)) {
            Boolean on_off = (Boolean) event.get("ON_OFF");
            updateReceiverStatus(on_off.booleanValue());
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
        if(id==R.id.action_send_email){
            //save setting to file and email the file
            exportSettings();
        }else if(id==R.id.action_import) {
            importSettings();
        }else{
            showFragment(fragmentName, false);
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean exportSettings(){
        try {
            File outputFile = File.createTempFile("call_router", ".json", context.getExternalCacheDir() );
            String fileName = outputFile.getAbsolutePath();
            Gson gson = new Gson();
            FileOutputStream fOut =  new FileOutputStream(fileName);
            String str = gson.toJson(appdata);
            fOut.write(str.getBytes());
            fOut.close();
            //invoke email with attachment intent
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "setting export");
            intent.putExtra(Intent.EXTRA_TEXT, "setting export");
            File file = new File(fileName);
            if (!file.exists() || !file.canRead()) {
                Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
            } else {
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file));
                this.startActivity(Intent.createChooser(intent,
                        "Sending email..."));
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    private static final int OPEN_REQUEST_CODE = 41;

    public void importSettings(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, OPEN_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData){
        Uri currentUri;
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==OPEN_REQUEST_CODE){
                if(resultData!=null){
                    currentUri=resultData.getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(currentUri);
                        final Gson gson = new Gson();
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        appdata = gson.fromJson(reader, AppData.class);
                        appDataAccess.setAppdata(appdata);
                        appDataAccess.saveAppData(appdata);
                        Toast.makeText(getApplicationContext(), "settings successfully imported, restarting app", Toast.LENGTH_SHORT).show();
                        recreate();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        appDataAccess = AppDataAccess.getInstance(context);
        appdata = appDataAccess.getAppdata();

        setContentView(R.layout.activity_main);
        // Adding Toolbar to Main screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.fab = (FloatingActionButton) findViewById(R.id.fab);

        fragments.put(Constant.FRAGMENT_KEY_GROUPS, new GroupsFragment());
        fragmentIDs.put(R.id.action_groups, Constant.FRAGMENT_KEY_GROUPS);
        fragmentTitles.put(Constant.FRAGMENT_KEY_GROUPS, "Rule Groups");

        fragments.put(Constant.FRAGMENT_KEY_GROUP_ADD, new GroupAddFragment());
        fragmentTitles.put(Constant.FRAGMENT_KEY_GROUP_ADD, "+ Rule Group");

        fragments.put(Constant.FRAGMENT_KEY_RULES, new RulesFragment());
        fragments.put(Constant.FRAGMENT_KEY_RULE_ADD, new RuleAddFragment());


        fragments.put(Constant.FRAGMENT_KEY_HELP, new HelpFragment());
        fragmentIDs.put(R.id.action_help, Constant.FRAGMENT_KEY_HELP);
        fragmentTitles.put(Constant.FRAGMENT_KEY_HELP, "Help & Feedback");

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        onFragmentBack();
                        //Toast.makeText(getApplicationContext(), "onBackStackChanged", Toast.LENGTH_SHORT).show();
                    }
                });

        showFragment(Constant.FRAGMENT_KEY_GROUPS, false);
    }

    public void onFragmentBack(){
        //reset title text and fab visibility based on fragment key
        System.out.println("onFragmentBack");
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content);
        String key="";
        if (f instanceof HelpFragment) {
            key = Constant.FRAGMENT_KEY_HELP;
        }
        if (f instanceof GroupsFragment) {
            key = Constant.FRAGMENT_KEY_GROUPS;
        }
        if (f instanceof GroupAddFragment) {
            key = Constant.FRAGMENT_KEY_GROUP_ADD;
        }
        if (f instanceof RulesFragment) {
            key = Constant.FRAGMENT_KEY_RULES;
        }
        if (f instanceof RuleAddFragment) {
            key = Constant.FRAGMENT_KEY_RULE_ADD;
        }
        System.out.println(key);
        String title = (String) fragmentTitles.get(key);
        System.out.println("found title:" + title);
        showFab(key);
        getSupportActionBar().setTitle(title);
    }

    public void clearBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = fm.getBackStackEntryAt(0);
            fm.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void showFragment(String key, boolean addToStack) {
        String title = (String) fragmentTitles.get(key);
        getSupportActionBar().setTitle(title);
        System.out.println(title);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content, (Fragment) fragments.get(key));
        if (addToStack) {
            ft.addToBackStack(null);
        }
        showFab(key);
        ft.commit();
    }

    public void showFab(String key) {
        if (key.equals(Constant.FRAGMENT_KEY_GROUPS)) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(appdata.getRuleGroups().size()>=Constant.MAX_GROUPS){
                        Toast.makeText(context, "maximum " + Constant.MAX_GROUPS + " groups", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showFragment(Constant.FRAGMENT_KEY_GROUP_ADD, true);
                }
            });
            return;
        }
        if (key.equals(Constant.FRAGMENT_KEY_RULES)) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RuleGroup rg = appdata.getRuleGroups().get(appdata.getGroupInEdit());
                    if(rg.getRules().size()>=Constant.MAX_RULES){
                        Toast.makeText(context, "maximum " + Constant.MAX_RULES + " rules per group", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    fragmentTitles.put(Constant.FRAGMENT_KEY_RULE_ADD, "+ Rule - " + rg.getName());
                    showFragment(Constant.FRAGMENT_KEY_RULE_ADD, true);
                }
            });
            return;
        }
        fab.setVisibility(View.INVISIBLE);
        return;
    }


    public void updateReceiverStatus(boolean enable){
        PackageManager pm = context.getPackageManager();
        if(enable){
            pm.setComponentEnabledSetting(new ComponentName(context, OutgoingCallRewrite.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }else{
            pm.setComponentEnabledSetting(new ComponentName(context, OutgoingCallRewrite.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }
}
