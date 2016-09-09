package com.mevoice.callrouter;

import android.app.Activity;
import android.content.ComponentName;
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
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    private FloatingActionButton fab;

    private EventBus bus = EventBus.getDefault();
    private HashMap fragments = new HashMap();
    private HashMap fragmentTitles = new HashMap();
    private HashMap fragmentIDs = new HashMap();
    private HashMap option_menu_groups_hashmap = new HashMap();
    int selectedMenuItemID;
    private String activeFragment;
    private int lastResetTime = 0;
    private int[] option_menu_groups_array = {R.id.options_groups};

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
        if (event.get(Constant.EVENT_TYPE_ACTION).equals(Constant.ACTION_EDIT_RULES)) {
            RuleGroup rg = CRApp.appdata.getRuleGroups().get(CRApp.appdata.getGroupInEdit());
            if (rg.getRules().size() > 0) {
                fragmentTitles.put(Constant.FRAGMENT_KEY_RULES, String.format(getString(R.string.title_rules), rg.getName()));
                showFragment(Constant.FRAGMENT_KEY_RULES, true);
            } else {
                fragmentTitles.put(Constant.FRAGMENT_KEY_RULE_ADD, String.format(getString(R.string.title_rule_add), rg.getName()));
                showFragment(Constant.FRAGMENT_KEY_RULE_ADD, true);
            }
        }
        if (event.get(Constant.EVENT_TYPE_ACTION).equals(Constant.ACTION_ROUTER_ON_OFF)) {
            updateReceiverStatus(CRApp.appdata.getRuleGroupInUse()>=0 && CRApp.appdata.isNumberRewrite());
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
        selectedMenuItemID = item.getItemId();
        //noinspection SimplifiableIfStatement
        String fragmentName = (String) fragmentIDs.get(selectedMenuItemID);
        //user should use either menu OR back button, not both
        clearBackStack();
        if (selectedMenuItemID == R.id.action_send_email) {
            //save setting to file and email the file
            exportSettings();
        } else if (selectedMenuItemID == R.id.action_import) {
            importSettings();
        } else if (selectedMenuItemID == R.id.action_reset) {
            int i = (int) (new Date().getTime()/1000);
            if(i-lastResetTime>5){
                Toast.makeText(this, R.string.message_before_reset, Toast.LENGTH_SHORT).show();
                lastResetTime=i;
                return false;
            }
            resetSettings();
        } else {
            showFragment(fragmentName, false);
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean exportSettings() {
        try {
            File outputFile = File.createTempFile("call_router", ".json", CRApp.context.getExternalCacheDir());
            String fileName = outputFile.getAbsolutePath();
            Gson gson = new Gson();
            FileOutputStream fOut = new FileOutputStream(fileName);
            String str = gson.toJson(CRApp.appdata);
            fOut.write(str.getBytes());
            fOut.close();
            //invoke email with attachment intent
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.literal_export_settings_email_subject));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.literal_export_settings_email_body));
            File file = new File(fileName);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file));
            this.startActivity(Intent.createChooser(intent, getString(R.string.message_export_settings_chooser_title)));
            return true;
        }
        catch (IOException e){
            Toast.makeText(CRApp.context, R.string.message_export_settings_error, Toast.LENGTH_SHORT).show();
        }
        return false;
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
            InputStream inputStream = getResources().getAssets().open(getString(R.string.literal_preload_settings_filename));
            importSettingsFromInputStream(inputStream);
            Toast.makeText(getApplicationContext(), R.string.message_reset_settings_success, Toast.LENGTH_SHORT).show();
            recreate();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), R.string.message_reset_settings_cannot_find_file, Toast.LENGTH_SHORT).show();
        }
    }

    public void importSettingsFromInputStream(InputStream inputStream) {
        final Gson gson = new Gson();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        AppData appDataFromFile = gson.fromJson(reader, AppData.class);
        CRApp.appdata.setRuleGroups(appDataFromFile.getRuleGroups());
        CRApp.appdataaccess.saveAppData();
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
                        Toast.makeText(getApplicationContext(), R.string.message_import_settings_success, Toast.LENGTH_SHORT).show();
                        recreate();
                    } catch (FileNotFoundException e) {
                        Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.message_import_settings_cannot_find_file), currentUri.toString()), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void checkEULA(){
        if(!CRApp.appdata.isEULA()) {
            FragmentManager fm = getSupportFragmentManager();
            EULAFragment dialogFragment = new EULAFragment();
            dialogFragment.setCancelable(false);
            dialogFragment.show(fm, "Sample Fragment");
        }
    }
    private DrawerLayout mDrawerLayout;
    private int backStackCount = 0, lastBackStackCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(CRApp.appdata.getLoadTimes()==0){
            resetSettings();
        }else{
            checkEULA();
        }
        CRApp.appdata.setLoadTimes(CRApp.appdata.getLoadTimes()+1);
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
        fragmentTitles.put(Constant.FRAGMENT_KEY_GROUPS, getString(R.string.title_groups));
        option_menu_groups_hashmap.put(Constant.FRAGMENT_KEY_GROUPS, new Integer(R.id.options_groups));

        fragments.put(Constant.FRAGMENT_KEY_SETTINGS, new SettingsFragment());
        fragmentIDs.put(R.id.action_settings, Constant.FRAGMENT_KEY_SETTINGS);
        fragmentTitles.put(Constant.FRAGMENT_KEY_SETTINGS, getString(R.string.title_settings));


        fragments.put(Constant.FRAGMENT_KEY_GROUP_ADD, new GroupAddFragment());
        fragmentTitles.put(Constant.FRAGMENT_KEY_GROUP_ADD, getString(R.string.title_group_add));
        option_menu_groups_hashmap.put(Constant.FRAGMENT_KEY_GROUP_ADD, new Integer(R.id.options_groups));

        fragments.put(Constant.FRAGMENT_KEY_RULES, new RulesFragment());
        option_menu_groups_hashmap.put(Constant.FRAGMENT_KEY_RULES, new Integer(R.id.options_groups));

        fragments.put(Constant.FRAGMENT_KEY_RULE_ADD, new RuleAddFragment());
        option_menu_groups_hashmap.put(Constant.FRAGMENT_KEY_RULE_ADD, new Integer(R.id.options_groups));


        fragments.put(Constant.FRAGMENT_KEY_HELP, new HelpFragment());
        fragmentIDs.put(R.id.action_help, Constant.FRAGMENT_KEY_HELP);
        fragmentTitles.put(Constant.FRAGMENT_KEY_HELP, getString(R.string.title_help));

        fragmentIDs.put(R.id.action_about, Constant.FRAGMENT_KEY_HELP);

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        backStackCount = getSupportFragmentManager().getBackStackEntryCount();
                        if (lastBackStackCount == backStackCount + 1) {
                            onFragmentBack();
                        }
                        lastBackStackCount = backStackCount;
                    }
                });
        activeFragment = Constant.FRAGMENT_KEY_SETTINGS;
        showFragment(Constant.FRAGMENT_KEY_SETTINGS, false);
    }

    public void onFragmentBack() {
        //reset title text and fab visibility based on fragment key
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
        String title = (String) fragmentTitles.get(key);
        activeFragment = key;
        showFab(key);
        getSupportActionBar().setTitle(title);
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
                    if (CRApp.appdata.getRuleGroups().size() >= Constant.MAX_GROUPS) {
                        Toast.makeText(CRApp.context, String.format(getResources().getString(R.string.message_add_group_max_groups), Constant.MAX_GROUPS), Toast.LENGTH_SHORT).show();
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
                    RuleGroup rg = CRApp.appdata.getRuleGroups().get(CRApp.appdata.getGroupInEdit());
                    if (rg.getRules().size() >= Constant.MAX_RULES) {
                        Toast.makeText(CRApp.context, String.format(getString(R.string.message_add_rule_max_rules), Constant.MAX_RULES), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    fragmentTitles.put(Constant.FRAGMENT_KEY_RULE_ADD, String.format(getString(R.string.title_rule_add), rg.getName()));
                    showFragment(Constant.FRAGMENT_KEY_RULE_ADD, true);
                }
            });
            return;
        }
        fab.setVisibility(View.INVISIBLE);
        return;
    }


    public void updateReceiverStatus(boolean enable) {
        PackageManager pm = CRApp.context.getPackageManager();
        ComponentName cn = new ComponentName(CRApp.context, OutgoingCallRewrite.class);
        int currentComponentEnabledSetting = pm.getComponentEnabledSetting(cn);
        if (enable && currentComponentEnabledSetting==PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            pm.setComponentEnabledSetting(cn,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
        if (!enable && (currentComponentEnabledSetting==PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            || currentComponentEnabledSetting==PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)){
            pm.setComponentEnabledSetting(cn,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }
}
