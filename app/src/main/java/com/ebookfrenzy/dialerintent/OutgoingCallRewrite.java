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

import com.ebookfrenzy.dialerintent.model.AppData;
import com.ebookfrenzy.dialerintent.model.MatchNumber;
import com.ebookfrenzy.dialerintent.model.RuleGroup;

/**
 * Created by Admin on 7/30/2016.
 */
public class OutgoingCallRewrite extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AppData appdata = AppDataAccess.getInstance(context).getAppdata();
        RuleGroup rg = appdata.getRuleGroupInUse();
        String phoneNumber;
        String new_phoneNumber;
        phoneNumber = getResultData();
        if(rg!=null){
            MatchNumber number = new MatchNumber(phoneNumber);
            if(rg.transform(number)){
                new_phoneNumber = number.getResult();
                Toast.makeText(context, "Dialed: " + phoneNumber + "\nCalled: " + new_phoneNumber, Toast.LENGTH_LONG).show();
                setResultData(new_phoneNumber);
                return;
            }
        }
        setResultData(phoneNumber);
        return;
    }
}