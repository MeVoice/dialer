package com.mevoice.callrouter;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.xml.sax.Locator;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import org.junit.FixMethodOrder;

/**
 * Created by Admin on 9/6/2016.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HelloWorldEspressoTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);
    //Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.drawerItemNameTextView), ViewMatchers.hasSibling(ViewMatchers.withText(((NavDrawerItem)item).getItemName())))).perform(ViewActions.click());

    void clickDrawerMenu(int itemID){
        //open drawer
        onView(withId(R.id.drawer)).perform(DrawerActions.open());
        //click groups
        onView(withText(itemID)).perform(click());
    }


    void clickOptionsMenu(int itemID) {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(itemID)).perform(click());
    }

    @Test
    public void test005_agreeUELA() {
        //display EULA
        onView(withId(R.id.eula_agree)).check(matches(isDisplayed()));
        //click agree
        onView(withId(R.id.eula_agree)).perform(click());
        //show settings
        onView(withId(R.id.settings_number_rewrite)).check(matches(isDisplayed()));
    }

    @Test
    public void test010_showTopLevelUI() {
        //show groups
        clickDrawerMenu(R.string.menu_item_groups);
        onView(withId(R.id.group_recycler_view)).check(matches(isDisplayed()));

        //show help
        clickDrawerMenu(R.string.menu_item_help);
        onView(withId(R.id.help_webview)).check(matches(isDisplayed()));

        //show settings
        clickDrawerMenu(R.string.menu_item_settings);
        onView(withId(R.id.settings_number_rewrite)).check(matches(isDisplayed()));
    }

    @Test
    public void test015_test_help() {
        //show groups
        clickDrawerMenu(R.string.menu_item_help);
        clickOptionsMenu(R.string.menu_item_about);
    }
}