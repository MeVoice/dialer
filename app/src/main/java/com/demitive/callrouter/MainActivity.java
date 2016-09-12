package com.demitive.callrouter;

import android.content.ComponentName;
import android.content.pm.PackageManager;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import com.demitive.callrouter.events.ClickEvent;
import com.demitive.callrouter.helper.CustomExceptionHandler;
import com.demitive.callrouter.model.RuleGroup;

import java.util.HashMap;

import static com.demitive.callrouter.helper.Utils.resetSettings;

public class MainActivity extends AppCompatActivity {


    private FloatingActionButton fab;

    private EventBus bus = EventBus.getDefault();
    private HashMap fragments = new HashMap();
    private HashMap fragmentTitles = new HashMap();
    private HashMap fragmentIDs = new HashMap();
    private HashMap option_menu_groups_hashmap = new HashMap();
    int selectedMenuItemID;

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
            RuleGroup rg = CRApp.appdata.getRuleGroups().get((int) CRApp.appdataaccess.getRuntimeData(Constant.RUNTIME_DATA_GROUPINEDIT));
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
        if (event.get(Constant.EVENT_TYPE_ACTION).equals(Constant.ACTION_GROUPS_RESET)) {
            showFragment(Constant.FRAGMENT_KEY_SETTINGS, false);
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
        Thread.currentThread().setUncaughtExceptionHandler(new CustomExceptionHandler(this));
        super.onCreate(savedInstanceState);
        System.out.println(CRApp.appdata.getLoadTimes() + " times");
        if(CRApp.appdata.getLoadTimes()==0){
            if(!resetSettings(this)){
                Toast.makeText(this, R.string.message_reset_settings_cannot_find_file, Toast.LENGTH_SHORT).show();
            }
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
                        mDrawerLayout.closeDrawers();
                        // Set item in checked state
                        menuItem.setChecked(true);
                        // TODO: handle navigation
                        selectedMenuItemID = menuItem.getItemId();
                        //noinspection SimplifiableIfStatement
                        String fragmentName = (String) fragmentIDs.get(selectedMenuItemID);
                        //user should use either menu OR back button, not both
                        clearBackStack();
                        showFragment(fragmentName, false);
                        // Closing drawer on item click
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
                    RuleGroup rg = CRApp.appdata.getRuleGroups().get((int) CRApp.appdataaccess.getRuntimeData(Constant.RUNTIME_DATA_GROUPINEDIT));
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
