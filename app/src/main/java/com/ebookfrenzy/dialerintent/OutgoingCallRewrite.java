package com.ebookfrenzy.dialerintent;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.CallLog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.net.URL;
import java.util.Date;

import android.net.Uri;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.ebookfrenzy.dialerintent.model.AppData;
import com.ebookfrenzy.dialerintent.model.MatchNumber;
import com.ebookfrenzy.dialerintent.model.RuleGroup;
import com.ebookfrenzy.dialerintent.model.TransformLog;

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
                String rule_comment = number.getMatchingRule().getName();
                setResultData(new_phoneNumber);
                Toast.makeText(context, "Dialed: " + phoneNumber + "\nCalled: " + new_phoneNumber, Toast.LENGTH_LONG).show();
                if(!new_phoneNumber.equals(phoneNumber)){
                    //TransformLog log = new TransformLog(phoneNumber, new_phoneNumber, new Date());
                    //appdata.addTransformLog(log);
                    if(new_phoneNumber.indexOf(",")>=0 || new_phoneNumber.indexOf(";")>=0) {
                        AddNumToCallLog(context.getContentResolver(), new_phoneNumber, CallLog.Calls.OUTGOING_TYPE, System.currentTimeMillis(), rule_comment);
                    }
                    AddNumToCallLog(context.getContentResolver(),phoneNumber, CallLog.Calls.OUTGOING_TYPE, System.currentTimeMillis(), rule_comment);
                }
                return;
            }
        }
        setResultData(phoneNumber);
        return;
    }
    public  void  AddNumToCallLog(ContentResolver resolver , String strNum, int type, long timeInMiliSecond, String rule_comment)
    {
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, strNum);
        values.put(CallLog.Calls.DATE, timeInMiliSecond);
        values.put(CallLog.Calls.DURATION, 0);
        values.put(CallLog.Calls.TYPE, type);
        values.put(CallLog.Calls.NEW, 1);
        values.put(CallLog.Calls.CACHED_NAME, rule_comment);
        values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
        values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "");
        values.put(CallLog.Calls.DURATION, 0);
        if(null != resolver)
        {
            resolver.insert(CallLog.Calls.CONTENT_URI, values);
        }
    }

}