package com.demitive.callrouter;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.demitive.callrouter.events.ClickEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {
    private EventBus bus = EventBus.getDefault();
    View rootView;
    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        Switch settings_more_call_log = (Switch) rootView.findViewById(R.id.settings_more_call_log);
        settings_more_call_log.setChecked(CRApp.appdata.isMoreLog());
        settings_more_call_log.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CRApp.appdata.setMoreLog(isChecked);
                CRApp.appdataaccess.saveAppData();
            }
        });
        Switch settings_number_rewrite = (Switch) rootView.findViewById(R.id.settings_number_rewrite);
        settings_number_rewrite.setChecked(CRApp.appdata.isNumberRewrite());
        settings_number_rewrite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CRApp.appdata.setNumberRewrite(isChecked);
                CRApp.appdataaccess.saveAppData();
                ClickEvent ev = new ClickEvent();
                ev.put(Constant.EVENT_TYPE_ACTION, Constant.ACTION_ROUTER_ON_OFF);
                bus.post(ev);
            }
        });
        Switch settings_show_number_change = (Switch) rootView.findViewById(R.id.settings_show_number_change);
        settings_show_number_change.setChecked(CRApp.appdata.isShowRewrite());
        settings_show_number_change.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CRApp.appdata.setShowRewrite(isChecked);
                CRApp.appdataaccess.saveAppData();
            }
        });
        return rootView;
    }
}
