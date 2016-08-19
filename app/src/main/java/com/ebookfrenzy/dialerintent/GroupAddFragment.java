package com.ebookfrenzy.dialerintent;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ebookfrenzy.dialerintent.model.AppData;
import com.ebookfrenzy.dialerintent.model.RuleGroup;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupAddFragment extends CommonFragment {
    public EditText group_add_name;
    public ImageButton group_add_button;

    public GroupAddFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        initAppData();

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
        if (appdata.getRuleGroups().size() == Constant.MAX_GROUPS) {
            Toast.makeText(context, "can have no more than " + Constant.MAX_GROUPS + " rule groups", Toast.LENGTH_SHORT).show();
            return;
        }
        RuleGroup rg = new RuleGroup(group_add_name.getText().toString());
        if (validateGroup(rg, -1)) {
            appdata.addRuleGroup(rg);
            //ft.adapter.notifyItemInserted(appdata.getRuleGroups().size() - 1);
            appdataaccess.saveAppData(appdata);
            group_add_name.setText("");
            Toast.makeText(context, "group added", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean validateGroup(RuleGroup rg, int position) {
        switch (appdata.validateRuleGroup(rg, position)) {
            case Constant.ERROR_GROUPNAME_TOOSHORT:
                Toast.makeText(context, "group name at least 5 characters long", Toast.LENGTH_SHORT).show();
                return false;
            case Constant.ERROR_GROUPNAME_DUP:
                Toast.makeText(context, "group name already in use", Toast.LENGTH_SHORT).show();
                return false;
            default:
                return true;
        }
    }
}
