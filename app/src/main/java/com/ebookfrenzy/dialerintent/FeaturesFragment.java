package com.ebookfrenzy.dialerintent;


import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.ebookfrenzy.dialerintent.model.AppData;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeaturesFragment extends Fragment {

    private AppData appdata;
    private CheckBox view_features_edit_number, view_features_reroute_call, view_features_confirm, view_features_permission;
    private Button view_features_save;
    private View rootView;
    private Context context;

    public FeaturesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_features, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity();
        appdata = ((Main2Activity) context).getAppdata();
        rootView = getView();
        view_features_edit_number = (CheckBox) rootView.findViewById(R.id.features_edit_number);
        view_features_edit_number.setChecked(appdata.isEditNumber());
        view_features_reroute_call = (CheckBox) rootView.findViewById(R.id.features_reroute_call);
        view_features_reroute_call.setChecked(appdata.isRerouteCall());
        view_features_confirm = (CheckBox) rootView.findViewById(R.id.features_confirm);
        view_features_confirm.setChecked(appdata.isConfirmReroute());
        view_features_permission = (CheckBox) rootView.findViewById(R.id.features_permission);
        view_features_save = (Button) rootView.findViewById(R.id.features_save);
        view_features_save.setOnClickListener( new Button.OnClickListener(){
            public void onClick(View v){
                handleSaveButtonClick(v);
            }
        });
    }

    private void handleSaveButtonClick(View v){
        PackageManager pm = context.getPackageManager();
        if(view_features_edit_number.isChecked()){
            pm.setComponentEnabledSetting(new ComponentName(context, com.ebookfrenzy.dialerintent.EditDialedNumber.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }else{
            pm.setComponentEnabledSetting(new ComponentName(context, com.ebookfrenzy.dialerintent.EditDialedNumber.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        }
        if(view_features_reroute_call.isChecked()){
            pm.setComponentEnabledSetting(new ComponentName(context, OutgoingCallRewrite.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }else{
            pm.setComponentEnabledSetting(new ComponentName(context, OutgoingCallRewrite.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        //save preference
        appdata.setEditNumber(view_features_edit_number.isChecked());
        appdata.setRerouteCall(view_features_reroute_call.isChecked());
        ((Main2Activity) context).getAppDataAccess().saveAppData(appdata);
        Toast.makeText(context, "changes saved", Toast.LENGTH_SHORT).show();
    }
}
