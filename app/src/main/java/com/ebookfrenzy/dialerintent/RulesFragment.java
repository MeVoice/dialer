package com.ebookfrenzy.dialerintent;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ebookfrenzy.dialerintent.model.AppData;
import com.ebookfrenzy.dialerintent.model.Rule;
import com.ebookfrenzy.dialerintent.model.RuleGroup;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RulesFragment extends Fragment {
    public Context context;
    public AppDataAccess appdataaccess;
    public AppData appdata;
    public EditText rule_add_name;
    public EditText rule_add_pattern;
    public EditText rule_add_formula;
    public ImageButton rule_add_button;
    public static int MAX_RULES=10;
    private int removePosition=-1;
    private RuleGroup ruleGroup;
    private int inEditCount=0;

    ContentAdapter adapter;
    private EventBus bus = EventBus.getDefault();


    public RulesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = getActivity();
        appdataaccess = AppDataAccess.getInstance(context);
        appdata = appdataaccess.getAppdata();
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rule, container, false);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);
        ruleGroup = appdata.getRuleGroup(appdata.getGroupInEdit());
        adapter = new ContentAdapter(recyclerView.getContext(), ruleGroup.getRules());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rule_add_name = (EditText) v.findViewById(R.id.rule_add_name);
        rule_add_pattern = (EditText) v.findViewById(R.id.rule_add_pattern);
        rule_add_formula = (EditText) v.findViewById(R.id.rule_add_formula);
        rule_add_button = (ImageButton) v.findViewById(R.id.rule_add_button);
        rule_add_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                addRule();
            }
        });
        return v;
    }
    public boolean validateRule(Rule rule, int position){
        if(!rule.validate()){
            Toast.makeText(context, rule.getValidationResult(), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    public void addRule(){
        if(ruleGroup.getRules().size()==MAX_RULES){
            Toast.makeText(context, "can have no more than "+MAX_RULES+" rules", Toast.LENGTH_SHORT).show();
            return;
        }
        Rule rule = new Rule(rule_add_name.getText().toString(),
                rule_add_pattern.getText().toString(),
                rule_add_formula.getText().toString());
        if(validateRule(rule, -1)){
            ruleGroup.addRule(rule);
            adapter.notifyItemInserted(ruleGroup.getRules().size()-1);
            appdataaccess.saveAppData(appdata);
            rule_add_name.setText("");
            rule_add_pattern.setText("");
            rule_add_formula.setText("");
            Toast.makeText(context, "rule added", Toast.LENGTH_SHORT).show();
        }
    }
    public void removeRule(int position){
        if(removePosition!=position){
            Toast.makeText(context, "click again to delete", Toast.LENGTH_SHORT).show();
            removePosition=position;
            return;
        }
        removePosition=-1;
        ruleGroup.removeRule(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(context, "rule removed", Toast.LENGTH_SHORT).show();
    }

    public class RuleViewHolder extends RecyclerView.ViewHolder {
        public EditText rule_name;
        public EditText rule_pattern;
        public EditText rule_formula;
        public CheckBox rule_inuse;
        public ImageButton rule_edit_delete_button;
        public ImageButton rule_edit_button;
        public ImageButton rule_edit_done;
        public ImageButton rule_edit_cancel;
        public RuleViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.rule_edit, parent, false));
            //itemView is from Parent class, represent any view within the holder
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();

                }
            });
            rule_name = (EditText) itemView.findViewById(R.id.rule_edit_name);
            rule_inuse = (CheckBox) itemView.findViewById(R.id.rule_edit_inuse);
            rule_pattern = (EditText) itemView.findViewById(R.id.rule_edit_pattern);
            rule_formula = (EditText) itemView.findViewById(R.id.rule_edit_formula);

            rule_edit_delete_button = (ImageButton) itemView.findViewById(R.id.rule_edit_delete_button);
            rule_edit_button = (ImageButton) itemView.findViewById(R.id.rule_edit_button);
            rule_edit_done = (ImageButton) itemView.findViewById(R.id.rule_edit_done);
            rule_edit_cancel = (ImageButton) itemView.findViewById(R.id.rule_edit_cancel);
        }
    }

    public class ContentAdapter extends RecyclerView.Adapter<RuleViewHolder> {
        // Set numbers of List in RecyclerView.
        private final List<Rule> mItems;
        private final Context mContext;

        public ContentAdapter(Context context, List<Rule> rules) {
            mContext = context;
            mItems = rules;
            System.out.println("rule count: " + rules.size());
        }

        @Override
        public RuleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RuleViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(final RuleViewHolder holder, final int position) {
            holder.rule_name.setText(mItems.get(position).getName());
            holder.rule_pattern.setText(mItems.get(position).getPattern());
            holder.rule_formula.setText(mItems.get(position).getFormula());
            holder.rule_inuse.setChecked(mItems.get(position).isInUse());
            holder.rule_edit_delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeRule(holder.getLayoutPosition());
                    appdataaccess.saveAppData(appdata);
                }
            });
            holder.rule_edit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePosition=-1;
                    holder.rule_name.setEnabled(true);
                    holder.rule_pattern.setEnabled(true);
                    holder.rule_formula.setEnabled(true);
                    holder.rule_inuse.setEnabled(true);
                    holder.rule_edit_cancel.setVisibility(View.VISIBLE);
                    holder.rule_edit_done.setVisibility(View.VISIBLE);
                    holder.rule_edit_button.setVisibility(View.INVISIBLE);
                    holder.rule_edit_delete_button.setVisibility(View.INVISIBLE);
                }
            });
            holder.rule_edit_done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePosition=-1;
                    Rule currRule = mItems.get(holder.getLayoutPosition());
                    if(holder.rule_inuse.isChecked()==currRule.isInUse() &&
                            holder.rule_name.getText().toString().equals(currRule.getName()) &&
                            holder.rule_pattern.getText().toString().equals(currRule.getPattern()) &&
                            holder.rule_formula.getText().toString().equals(currRule.getFormula())){
                        Toast.makeText(context, "no change detected", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Rule rule = new Rule(holder.rule_name.getText().toString(), holder.rule_pattern.getText().toString(), holder.rule_formula.getText().toString());
                    rule.setInUse(holder.rule_inuse.isChecked());
                    if(!validateRule(rule, holder.getLayoutPosition())){
                        return;
                    }
                    ruleGroup.setRule(holder.getLayoutPosition(), rule);
                    appdataaccess.saveAppData(appdata);
                    Toast.makeText(context, "group changed", Toast.LENGTH_SHORT).show();

                    holder.rule_name.setEnabled(false);
                    holder.rule_pattern.setEnabled(false);
                    holder.rule_formula.setEnabled(false);
                    holder.rule_inuse.setEnabled(false);
                    holder.rule_edit_cancel.setVisibility(View.INVISIBLE);
                    holder.rule_edit_done.setVisibility(View.INVISIBLE);
                    holder.rule_edit_button.setVisibility(View.VISIBLE);
                    holder.rule_edit_delete_button.setVisibility(View.VISIBLE);
                    adapter.notifyItemChanged(holder.getLayoutPosition());
                }
            });
            holder.rule_edit_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePosition=-1;
                    holder.rule_name.setText(mItems.get(holder.getLayoutPosition()).getName());
                    holder.rule_inuse.setChecked(mItems.get(holder.getLayoutPosition()).isInUse());
                    holder.rule_name.setEnabled(false);
                    holder.rule_inuse.setEnabled(false);
                    holder.rule_pattern.setEnabled(false);
                    holder.rule_formula.setEnabled(false);
                    holder.rule_edit_cancel.setVisibility(View.INVISIBLE);
                    holder.rule_edit_done.setVisibility(View.INVISIBLE);
                    holder.rule_edit_button.setVisibility(View.VISIBLE);
                    holder.rule_edit_delete_button.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }
}
