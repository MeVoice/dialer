package com.mevoice.callrouter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mevoice.callrouter.events.ClickEvent;
import com.mevoice.callrouter.helper.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import org.greenrobot.eventbus.EventBus;

import static com.mevoice.callrouter.helper.Utils.importSettingsFromInputStream;
import static com.mevoice.callrouter.helper.Utils.resetSettings;

/**
 * Created by Admin on 9/10/2016.
 */
public abstract class GroupCommonFragment extends Fragment {
    private int lastResetTime=0;
    private static final int OPEN_REQUEST_CODE = 41;
    EventBus bus = EventBus.getDefault();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.options_menu_groups, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        switch (itemID){
            case R.id.action_send_email:
                //save setting to file and email the file
                exportSettings();
                return true;
            case R.id.action_import:
                importSettings();
                return true;
            case R.id.action_reset:
                int i = (int) (new Date().getTime()/1000);
                if(i-lastResetTime>5){
                    Toast.makeText(getActivity(), R.string.message_before_reset, Toast.LENGTH_SHORT).show();
                    lastResetTime=i;
                    return true;
                }
                if(resetSettings(getActivity())) {
                    Toast.makeText(getActivity(), R.string.message_reset_settings_success, Toast.LENGTH_SHORT).show();
                    postGroupsImport();
                }else {
                    Toast.makeText(getActivity(), R.string.message_reset_settings_cannot_find_file, Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                break;
        }
        return false;
    }

    void postGroupsImport(){
        ClickEvent ev = new ClickEvent();
        ev.put(Constant.EVENT_TYPE_ACTION, Constant.ACTION_GROUPS_RESET);
        bus.post(ev);
        ClickEvent ev2 = new ClickEvent();
        ev2.put(Constant.EVENT_TYPE_ACTION, Constant.ACTION_ROUTER_ON_OFF);
        bus.post(ev2);
    }
    public void exportSettings() {
        String fileName = Utils.saveStringToTempFile(".json", (new Gson()).toJson(CRApp.appdata));
        if(fileName==null){
            Toast.makeText(CRApp.context, R.string.message_export_settings_error, Toast.LENGTH_SHORT).show();
        }
        if(!Utils.startSendEmailWithAttachment(getActivity(), null,
                getString(R.string.literal_export_settings_email_subject),
                getString(R.string.literal_export_settings_email_body), new String[] {fileName},
                getString(R.string.message_export_settings_chooser_title))){
            Toast.makeText(getActivity(), R.string.message_export_settings_error_noemail, Toast.LENGTH_SHORT).show();
        }
    }

    public void importSettings() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        try {
            startActivityForResult(intent, OPEN_REQUEST_CODE);
        }
        catch (ActivityNotFoundException e){
            Toast.makeText(getActivity(), R.string.message_import_settings_error_no_document_opener, Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Uri currentUri;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OPEN_REQUEST_CODE) {
                if (resultData != null) {
                    currentUri = resultData.getData();
                    try {
                        InputStream inputStream = getActivity().getContentResolver().openInputStream(currentUri);
                        importSettingsFromInputStream(inputStream);
                        Toast.makeText(getActivity(), R.string.message_import_settings_success, Toast.LENGTH_SHORT).show();
                        postGroupsImport();
                    } catch (FileNotFoundException e) {
                        Toast.makeText(getActivity(), String.format(getResources().getString(R.string.message_import_settings_cannot_find_file), currentUri.toString()), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
