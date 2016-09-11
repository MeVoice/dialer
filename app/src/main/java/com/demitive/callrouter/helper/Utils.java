package com.demitive.callrouter.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.google.gson.Gson;
import com.demitive.callrouter.CRApp;
import com.demitive.callrouter.R;
import com.demitive.callrouter.model.AppData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Admin on 9/9/2016.
 */
public class Utils {
    public static String saveStringToTempFile(String extension, String content){
        try {
            File outputFile = File.createTempFile("temp", extension, CRApp.context.getExternalCacheDir());
            String fileName = outputFile.getAbsolutePath();
            FileOutputStream fOut = new FileOutputStream(fileName);
            fOut.write(content.getBytes());
            fOut.close();
            return fileName;
        }
        catch (IOException e){
            return null;
        }
    }

    public static boolean startSendEmailWithAttachment(Context context,
                               String[] recipients, String subject, String body,
                               String[] attachmentFilenames, String dialogTitle){
        ArrayList<Uri> uris = new ArrayList<>();
        if(attachmentFilenames!=null) {
            for(String filename : attachmentFilenames) {
                uris.add(Uri.parse("file://" + filename));
            }
        }
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", context.getString(R.string.literal_dummy_email_recipient), null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(emailIntent, 0);
        if(resolveInfos==null || resolveInfos.size()==0){
            return false;
        }
        List<LabeledIntent> intents = new ArrayList<>();
        for (int i = resolveInfos.size()-1; i >= 0; i--) {
            ResolveInfo info = resolveInfos.get(i);
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
            System.out.println(info.activityInfo.packageName);
            if(recipients!=null) {
                intent.putExtra(Intent.EXTRA_EMAIL, recipients);
            }
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            if(attachmentFilenames!=null) {
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris); //ArrayList<Uri> of attachment Uri's
            }
            intents.add(new LabeledIntent(intent, info.activityInfo.packageName, info.loadLabel(context.getPackageManager()), info.icon));
        }
        Intent chooser = Intent.createChooser(intents.remove(0), dialogTitle);
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new LabeledIntent[intents.size()]));
        context.startActivity(chooser);
        return true;
    }

    static public int getBuildNumber(){
        try{
            Properties prop = new Properties();
            InputStream inputStream = CRApp.context.getResources().getAssets().open(CRApp.context.getString(R.string.literal_version_properties_filename));
            prop.load(inputStream);
            inputStream.close();
            return Integer.valueOf(prop.getProperty("buildNumber"));
        }
        catch (IOException e){
        }
        return -1;
    }
    static public void importSettingsFromInputStream(InputStream inputStream) {
        final Gson gson = new Gson();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        AppData appDataFromFile = gson.fromJson(reader, AppData.class);
        CRApp.appdata.setRuleGroups(appDataFromFile.getRuleGroups());
        CRApp.appdataaccess.saveAppData();
    }

    static public boolean resetSettings(Context context) {
        try {
            InputStream inputStream = context.getResources().getAssets().open(context.getString(R.string.literal_preload_settings_filename));
            importSettingsFromInputStream(inputStream);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
