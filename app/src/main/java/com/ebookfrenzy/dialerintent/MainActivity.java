package com.ebookfrenzy.dialerintent;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.util.Log;

import android.Manifest;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "DialIntent";
    private static final int PERMISSION_REQUEST_CODE = 101;
    private Boolean enable_edit = false;
    private Boolean enable_reroute = false;
    private String prefix;
    private SharedPreferences sharedPref;
    private CheckBox checkbox_edit, checkbox_reroute;
    private EditText edittext;
    private Button okButton;
    private Context context;
    private String[] requiredPermissions = new String[] {Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.CALL_PHONE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);
        //initialize checkbox and prefix with values from storage
        sharedPref = context.getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE);
        enable_edit = sharedPref.getBoolean(getString(R.string.value_enable_edit), false);
        enable_reroute = sharedPref.getBoolean(getString(R.string.value_enable_reroute), false);
        prefix = sharedPref.getString(getString(R.string.value_prefix), null);

        checkbox_edit = (CheckBox) findViewById(R.id.checkbox_edit);
        checkbox_reroute = (CheckBox) findViewById(R.id.checkbox_reroute);
        edittext = (EditText) findViewById(R.id.editText2);
        okButton = (Button) findViewById(R.id.button2);

        if(enable_edit ==null || !enable_edit.booleanValue()){
            checkbox_edit.setChecked(false);
        }else{
            checkbox_edit.setChecked(true);
        }
        if(enable_reroute ==null || !enable_reroute.booleanValue()){
            checkbox_reroute.setChecked(false);
        }else{
            checkbox_reroute.setChecked(true);
        }
        if(prefix!=null) {
            edittext.setText(prefix);
        }

        okButton.setOnClickListener( new Button.OnClickListener(){
            public void onClick(View v){
                handleButtonClick(v);
            }
        });
        //check and request permission
        checkAndRequestPermissions(requiredPermissions, PERMISSION_REQUEST_CODE);

    }

    protected void showReceivers(Intent intent){
        PackageManager packageManager = getPackageManager();
        List<String> startupApps = new ArrayList<String>();
        List<ResolveInfo> activities = packageManager.queryBroadcastReceivers(intent, 0);
        Log.i(TAG, "list of receivers---");
        for (ResolveInfo resolveInfo : activities) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo != null) {
                startupApps.add(activityInfo.name);
                Log.i(TAG, activityInfo.name);
            }
        }
        Log.i(TAG, "---list of receivers");
    }


    protected void checkAndRequestPermissions(String[] permissions, int requestcode){
        List<String> permList = new ArrayList<String>();
        for(int i=0; i<permissions.length; i++){
            int isGranted = ContextCompat.checkSelfPermission(this, permissions[i]);
            if(isGranted!=PackageManager.PERMISSION_GRANTED){
                Log.i(TAG, permissions[i] + " not granted");
                permList.add(permissions[i]);
            }
        }
        if(permList.size()>0) {
            Log.i(TAG, "ActivityCompat.requestPermissions");
            ActivityCompat.requestPermissions(this, permList.toArray(new String[permList.size()]), requestcode);
        }else{
            checkbox_edit.setEnabled(true);
            checkbox_reroute.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        Log.i(TAG, "onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Debug.waitForDebugger();
        int permission_denied = 0;
        switch(requestCode){
            case PERMISSION_REQUEST_CODE:{
                if (grantResults.length == 0){
                    permission_denied++;
                    Log.i(TAG, "permission for NEW_OUTGOING_CALL has been denied by user");
                } else {
                    for(int i=0; i<grantResults.length; i++){
                        if(grantResults[i] !=PackageManager.PERMISSION_GRANTED){
                            Log.i(TAG, "permission " + permissions[i] + " has been denied by user");
                            permission_denied++;
                        }else{
                            Log.i(TAG, "permission " + permissions[i] + " has been granted by user");
                        }
                    }
                }
            }
        }
        if(permission_denied>0) {
            //disable activities when permission's denied
            PackageManager pm = getPackageManager();
            pm.setComponentEnabledSetting(new ComponentName(this, com.ebookfrenzy.dialerintent.EditDialedNumber.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(new ComponentName(this, OutgoingCallRewrite.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            checkbox_edit.setChecked(false);
            checkbox_reroute.setChecked(false);
            checkbox_edit.setEnabled(false);
            checkbox_reroute.setEnabled(false);
            Toast.makeText(context, "disabled features due to lack of permissions", Toast.LENGTH_SHORT).show();
        }else{
            checkbox_edit.setEnabled(true);
            checkbox_reroute.setEnabled(true);
        }
    }

    private void handleButtonClick(View v){
        //enable/disable activity based on preference
        PackageManager pm = getPackageManager();
        if(checkbox_edit.isChecked()){
            pm.setComponentEnabledSetting(new ComponentName(this, com.ebookfrenzy.dialerintent.EditDialedNumber.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }else{
            pm.setComponentEnabledSetting(new ComponentName(this, com.ebookfrenzy.dialerintent.EditDialedNumber.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        }
        if(checkbox_reroute.isChecked()){
            pm.setComponentEnabledSetting(new ComponentName(this, OutgoingCallRewrite.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }else{
            pm.setComponentEnabledSetting(new ComponentName(this, OutgoingCallRewrite.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        //save preference
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.value_enable_edit), checkbox_edit.isChecked());
        editor.putBoolean(getString(R.string.value_enable_reroute), checkbox_reroute.isChecked());
        editor.putString(getString(R.string.value_prefix), edittext.getText().toString());
        editor.commit();
        finish();
    }
}

