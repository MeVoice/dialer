package com.ebookfrenzy.dialerintent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.net.URL;
import android.net.Uri;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Admin on 7/30/2016.
 */
public class OutgoingCallRewrite extends BroadcastReceiver {
    private static final String TAG = "DialIntent";
    private SharedPreferences sharedPref;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");
        // Extract phone number reformatted by previous receivers
        sharedPref = context.getSharedPreferences(context.getString(R.string.shared_pref), Context.MODE_PRIVATE);
        String prefix = sharedPref.getString(context.getString(R.string.value_prefix), null);

        String phoneNumber;
        String new_phoneNumber;
        //phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        phoneNumber = getResultData();
        Log.i(TAG, "result data is: " + phoneNumber);
        if(phoneNumber.startsWith(prefix)){
            new_phoneNumber = phoneNumber.substring(prefix.length());
            Toast.makeText(context, "Dialed: " + phoneNumber + "\nCalled: " + new_phoneNumber, Toast.LENGTH_LONG).show();
            setResultData(new_phoneNumber);
        }
    }
}