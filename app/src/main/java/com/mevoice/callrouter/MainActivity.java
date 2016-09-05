package com.mevoice.callrouter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import com.mevoice.callrouter.R;
import com.mevoice.callrouter.events.ClickEvent;
import com.mevoice.callrouter.model.AppData;
import com.mevoice.callrouter.model.RuleGroup;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private HashMap option_menu_groups_hashmap = new HashMap();
    private String activeFragment;
    private int[] option_menu_groups_array = {R.id.options_groups, R.id.options_help};

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
            if (rg.getRules().size() > 0) {
                fragmentTitles.put(Constant.FRAGMENT_KEY_RULES, "Rules - " + rg.getName());
                showFragment(Constant.FRAGMENT_KEY_RULES, true);
            } else {
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < option_menu_groups_array.length; i++) {
            Integer groupID = (Integer) option_menu_groups_hashmap.get(activeFragment);
            if (groupID == null || groupID.intValue() != option_menu_groups_array[i]) {
                menu.setGroupVisible(option_menu_groups_array[i], false);
            } else {
                menu.setGroupVisible(option_menu_groups_array[i], true);
            }
        }
        super.onPrepareOptionsMenu(menu);
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
        if (id == R.id.action_send_email) {
            //save setting to file and email the file
            exportSettings();
        } else if (id == R.id.action_import) {
            importSettings();
        } else if (id == R.id.action_reset) {
            resetSettings();
        } else {
            showFragment(fragmentName, false);
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean exportSettings() {
        try {
            File outputFile = File.createTempFile("call_router", ".json", context.getExternalCacheDir());
            String fileName = outputFile.getAbsolutePath();
            Gson gson = new Gson();
            FileOutputStream fOut = new FileOutputStream(fileName);
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static final int OPEN_REQUEST_CODE = 41;

    public void importSettings() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, OPEN_REQUEST_CODE);
    }

    public void resetSettings() {
        try {
            InputStream inputStream = getResources().getAssets().open("preload_sample_groups.json");
            importSettingsFromInputStream(inputStream);
            Toast.makeText(getApplicationContext(), "settings successfully reset, restarting app", Toast.LENGTH_SHORT).show();
            recreate();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "cannot find preload_sample_groups.json file", Toast.LENGTH_SHORT).show();
        }
    }

    public void importSettingsFromInputStream(InputStream inputStream) {
        final Gson gson = new Gson();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        appdata = gson.fromJson(reader, AppData.class);
        appdata.setLoadTimes(1);
        appDataAccess.setAppdata(appdata);
        appDataAccess.saveAppData(appdata);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Uri currentUri;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OPEN_REQUEST_CODE) {
                if (resultData != null) {
                    currentUri = resultData.getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(currentUri);
                        importSettingsFromInputStream(inputStream);
                        Toast.makeText(getApplicationContext(), "settings successfully imported, restarting app", Toast.LENGTH_SHORT).show();
                        recreate();
                    } catch (FileNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "cannot find file: " + currentUri.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private DrawerLayout mDrawerLayout;
    private int backStackCount = 0, lastBackStackCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        appDataAccess = AppDataAccess.getInstance(context);
        appdata = appDataAccess.getAppdata();
        if(appdata.getLoadTimes()==0){
            resetSettings();
        }
        setContentView(R.layout.activity_main);
        // Adding Toolbar to Main screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    // This method will trigger on item Click of navigation menu
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // Set item in checked state
                        menuItem.setChecked(true);
                        // TODO: handle navigation
                        onOptionsItemSelected(menuItem);

                        // Closing drawer on item click
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });

        this.fab = (FloatingActionButton) findViewById(R.id.fab);

        fragments.put(Constant.FRAGMENT_KEY_GROUPS, new GroupsFragment());
        fragmentIDs.put(R.id.action_groups, Constant.FRAGMENT_KEY_GROUPS);
        fragmentTitles.put(Constant.FRAGMENT_KEY_GROUPS, "Rule Groups");
        option_menu_groups_hashmap.put(Constant.FRAGMENT_KEY_GROUPS, new Integer(R.id.options_groups));

        fragments.put(Constant.FRAGMENT_KEY_GROUP_ADD, new GroupAddFragment());
        fragmentTitles.put(Constant.FRAGMENT_KEY_GROUP_ADD, "+ Rule Group");
        option_menu_groups_hashmap.put(Constant.FRAGMENT_KEY_GROUP_ADD, new Integer(R.id.options_groups));

        fragments.put(Constant.FRAGMENT_KEY_RULES, new RulesFragment());
        option_menu_groups_hashmap.put(Constant.FRAGMENT_KEY_RULES, new Integer(R.id.options_groups));

        fragments.put(Constant.FRAGMENT_KEY_RULE_ADD, new RuleAddFragment());
        option_menu_groups_hashmap.put(Constant.FRAGMENT_KEY_RULE_ADD, new Integer(R.id.options_groups));


        fragments.put(Constant.FRAGMENT_KEY_HELP, new HelpFragment());
        fragmentIDs.put(R.id.action_help, Constant.FRAGMENT_KEY_HELP);
        fragmentTitles.put(Constant.FRAGMENT_KEY_HELP, "Help & Feedback");
        //option_menu_groups_hashmap.put(Constant.FRAGMENT_KEY_HELP, new Integer(R.id.options_help));

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        backStackCount = getSupportFragmentManager().getBackStackEntryCount();
                        System.out.println("backStackCount: " + backStackCount);
                        if (lastBackStackCount == backStackCount + 1) {
                            onFragmentBack();
                        }
                        lastBackStackCount = backStackCount;
                        //Toast.makeText(getApplicationContext(), "onBackStackChanged", Toast.LENGTH_SHORT).show();
                    }
                });
        activeFragment = Constant.FRAGMENT_KEY_GROUPS;
        showFragment(Constant.FRAGMENT_KEY_GROUPS, false);
    }

    public void onFragmentBack() {
        //reset title text and fab visibility based on fragment key
        System.out.println("onFragmentBack");
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content);
        String key = "";
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
        activeFragment = key;
        showFab(key);
        getSupportActionBar().setTitle(title);
        System.out.println("onFragmentBack title is:" + title);
    }

    public void clearBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = fm.getBackStackEntryAt(0);
            fm.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        lastBackStackCount = 0;
    }

    public void showFragment(String key, boolean addToStack) {
        activeFragment = key;
        invalidateOptionsMenu();
        String title = (String) fragmentTitles.get(key);
        getSupportActionBar().setTitle(title);
        System.out.println("title is:" + title);
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
                    if (appdata.getRuleGroups().size() >= Constant.MAX_GROUPS) {
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
                    if (rg.getRules().size() >= Constant.MAX_RULES) {
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


    public void updateReceiverStatus(boolean enable) {
        PackageManager pm = context.getPackageManager();
        if (enable) {
            pm.setComponentEnabledSetting(new ComponentName(context, OutgoingCallRewrite.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            pm.setComponentEnabledSetting(new ComponentName(context, OutgoingCallRewrite.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }
}
