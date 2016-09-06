package com.mevoice.callrouter;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mevoice.callrouter.model.Rule;
import com.mevoice.callrouter.R;
import com.mevoice.callrouter.model.RuleGroup;

/**
 * A simple {@link Fragment} subclass.
 */
public class RuleAddFragment extends Fragment {

    public EditText rule_add_name;
    public EditText rule_add_pattern;
    public EditText rule_add_formula;
    public ImageButton rule_add_button;

    public RuleAddFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rule_add, container, false);
        // Inflate the layout for this fragment
        rule_add_name = (EditText) rootView.findViewById(R.id.rule_add_name);
        rule_add_pattern = (EditText) rootView.findViewById(R.id.rule_add_pattern);
        rule_add_formula = (EditText) rootView.findViewById(R.id.rule_add_formula);
        rule_add_button = (ImageButton) rootView.findViewById(R.id.rule_add_button);
        rule_add_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                addRule();
            }
        });
        return rootView;
    }
    public void addRule(){
        RuleGroup ruleGroup = CRApp.appdata.getRuleGroup(CRApp.appdata.getGroupInEdit());
        if(ruleGroup.getRules().size()>=Constant.MAX_RULES){
            Toast.makeText(CRApp.context, String.format(getString(R.string.message_add_rule_max_rules), Constant.MAX_RULES), Toast.LENGTH_SHORT).show();
            return;
        }

        Rule rule = new Rule(rule_add_name.getText().toString(),
                rule_add_pattern.getText().toString(),
                rule_add_formula.getText().toString());
        if(validateRule(rule, -1)){
            ruleGroup.addRule(rule);
            CRApp.appdataaccess.saveAppData();
            rule_add_name.setText("");
            rule_add_pattern.setText("");
            rule_add_formula.setText("");
            Toast.makeText(CRApp.context, R.string.message_add_rule_success, Toast.LENGTH_SHORT).show();
        }
    }
    public boolean validateRule(Rule rule, int position){
        if(!rule.validate()){
            Toast.makeText(CRApp.context, String.format(getString(rule.getValidationResult()), getString(rule.getValidationResultErrorField())), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
