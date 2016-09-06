package com.mevoice.callrouter;

import android.support.design.widget.FloatingActionButton;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mTestActivity;
    private EditText mEditText;

    private FloatingActionButton mFab;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Starts the activity under test using
        // the default Intent with:
        // action = {@link Intent#ACTION_MAIN}
        // flags = {@link Intent#FLAG_ACTIVITY_NEW_TASK}
        // All other fields are null or empty.
        mTestActivity = getActivity();
        mEditText = (EditText)  mTestActivity.findViewById(R.id.group_add_name);
        mFab = (FloatingActionButton) mTestActivity.findViewById(R.id.fab);
    }
    public void testPreconditions() {
        // Try to add a message to add context to your assertions.
        // These messages will be shown if
        // a tests fails and make it easy to
        // understand why a test failed
        assertNotNull("mTestActivity is null", mTestActivity);
        assertNull("mEditText is not null", mEditText);
        assertNotNull("mFab is null", mFab);
    }
}