package com.demitive.callrouter;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.demitive.callrouter.R;
import com.demitive.callrouter.model.RuleGroup;

import java.util.Formatter;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupAddFragment extends GroupCommonFragment {
    public EditText group_add_name;
    public ImageButton group_add_button;

    public GroupAddFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_group_add, container, false);
        group_add_name = (EditText) rootView.findViewById(R.id.group_add_name);
        group_add_button = (ImageButton) rootView.findViewById(R.id.group_add_button);
        group_add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addGroup();
            }
        });
        return rootView;
    }

    public void addGroup() {
        if (CRApp.appdata.getRuleGroups().size() == Constant.MAX_GROUPS) {
            Toast.makeText(CRApp.context, String.format(getResources().getString(R.string.message_add_group_max_groups), Constant.MAX_GROUPS), Toast.LENGTH_SHORT).show();
            return;
        }
        RuleGroup rg = new RuleGroup(group_add_name.getText().toString());
        if (validateGroup(rg, -1)) {
            CRApp.appdata.addRuleGroup(rg);
            //ft.adapter.notifyItemInserted(appdata.getRuleGroups().size() - 1);
            CRApp.appdataaccess.saveAppData();
            group_add_name.setText("");
            Toast.makeText(CRApp.context, R.string.message_after_add_group, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean validateGroup(RuleGroup rg, int position) {
        switch (CRApp.appdata.validateRuleGroup(rg, position)) {
            case Constant.ERROR_GROUPNAME_TOOSHORT:
                Toast.makeText(CRApp.context, R.string.message_validation_group_name_too_short, Toast.LENGTH_SHORT).show();
                return false;
            case Constant.ERROR_GROUPNAME_DUP:
                Toast.makeText(CRApp.context, R.string.message_validation_group_name_in_use, Toast.LENGTH_SHORT).show();
                return false;
            default:
                return true;
        }
    }
}
