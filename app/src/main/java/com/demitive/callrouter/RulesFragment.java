package com.demitive.callrouter;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.demitive.callrouter.model.Rule;
import com.demitive.callrouter.helper.ItemTouchHelperAdapter;
import com.demitive.callrouter.helper.ItemTouchHelperViewHolder;
import com.demitive.callrouter.helper.OnStartDragListener;
import com.demitive.callrouter.helper.SimpleItemTouchHelperCallback;
import com.demitive.callrouter.model.MatchNumber;
import com.demitive.callrouter.model.RuleGroup;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RulesFragment extends GroupCommonFragment  implements OnStartDragListener {
    public EditText test_reroute_number;
    private int removePosition=-1;
    private int lastRemoveTime=0;
    private RuleGroup ruleGroup;
    private ItemTouchHelper mItemTouchHelper;
    private MatchNumber testNumber;
    ContentAdapter adapter;
    private View rootView;
    private EventBus bus = EventBus.getDefault();


    public RulesFragment() {
        // Required empty public constructor
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(CRApp.appdata.getRules_loadTimes()==0){
            Snackbar.make(rootView, getString(R.string.greeting_rules), Snackbar.LENGTH_INDEFINITE).show();
        }
        CRApp.appdata.setRules_loadTimes(CRApp.appdata.getRules_loadTimes()+1);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_rule, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rules_recycler_view);
        ruleGroup = CRApp.appdata.getRuleGroup((int) CRApp.appdataaccess.getRuntimeData(Constant.RUNTIME_DATA_GROUPINEDIT));
        adapter = new ContentAdapter(recyclerView.getContext(), ruleGroup.getRules());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        test_reroute_number = (EditText) rootView.findViewById(R.id.test_reroute_number);
        final ImageButton test_reroute_button = (ImageButton) rootView.findViewById(R.id.test_reroute_button);
        final ImageButton test_call_button = (ImageButton) rootView.findViewById(R.id.test_call_button);

        test_reroute_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                testNumber = new MatchNumber(test_reroute_number.getText().toString());
                String matching_pattern = getString(R.string.message_test_result_none_match);
                String result = testNumber.getNumber();
                if(ruleGroup.transform(testNumber)){
                    matching_pattern = testNumber.getMatchingRule().getPattern();
                    result = testNumber.getResult();
                    test_call_button.setVisibility(View.VISIBLE);
                }
                String message = String.format(getString(R.string.message_test_result_numbers), matching_pattern, result);
                Snackbar.make(v, message, Snackbar.LENGTH_INDEFINITE).show();
            }
        });
        test_call_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String result = testNumber.getResult();
                result =  result.replace("*", Uri.encode("*")).replace("#",Uri.encode("#")).replace("#",Uri.encode("+"));
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + result));
                startActivity(callIntent);
                test_call_button.setVisibility(View.GONE);
            }
        });
        return rootView;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public boolean validateRule(Rule rule, int position){
        if(!rule.validate()){
            Toast.makeText(CRApp.context, String.format(getString(rule.getValidationResult()), getString(rule.getValidationResultErrorField())), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    public void removeRule(int position){
        int i = (int) (new Date().getTime()/1000);
        if(removePosition!=position || i-lastRemoveTime>5){
            Toast.makeText(CRApp.context, R.string.message_delete_rule_clickagain, Toast.LENGTH_SHORT).show();
            removePosition=position;
            lastRemoveTime=i;
            return;
        }
        removePosition=-1;
        ruleGroup.removeRule(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(CRApp.context, R.string.message_delete_rule_deleted, Toast.LENGTH_SHORT).show();
    }

    public class RuleViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        public EditText rule_name;
        public EditText rule_pattern;
        public EditText rule_formula;
        public CheckBox rule_inuse;
        public ImageButton rule_edit_delete_button;
        public ImageButton rule_edit_copy;
        public ImageButton rule_edit_button;
        public ImageButton rule_edit_done;
        public ImageButton rule_edit_cancel;
        public LinearLayout rule_edit_layout;
        public boolean isActive;
        public RuleViewHolder(View itemView) {
            super(itemView);
            rule_name = (EditText) itemView.findViewById(R.id.rule_edit_name);
            rule_inuse = (CheckBox) itemView.findViewById(R.id.rule_edit_inuse);
            rule_pattern = (EditText) itemView.findViewById(R.id.rule_edit_pattern);
            rule_formula = (EditText) itemView.findViewById(R.id.rule_edit_formula);

            rule_edit_delete_button = (ImageButton) itemView.findViewById(R.id.rule_edit_delete_button);
            rule_edit_button = (ImageButton) itemView.findViewById(R.id.rule_edit_button);
            rule_edit_done = (ImageButton) itemView.findViewById(R.id.rule_edit_done);
            rule_edit_copy = (ImageButton) itemView.findViewById(R.id.rule_edit_copy);
            rule_edit_cancel = (ImageButton) itemView.findViewById(R.id.rule_edit_cancel);
            rule_edit_layout = (LinearLayout) itemView.findViewById(R.id.rule_edit_layout);
        }
        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            if(this.isActive) {
                itemView.setBackgroundColor(getResources().getColor(R.color.activeItem));
            }else{
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    public class ContentAdapter extends RecyclerView.Adapter<RuleViewHolder> implements ItemTouchHelperAdapter {
        // Set numbers of List in RecyclerView.
        private final List<Rule> mItems;

        public ContentAdapter(Context context, List<Rule> rules) {
            mItems = rules;
        }

        @Override
        public RuleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rule_edit, parent, false);
            return new RuleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RuleViewHolder holder, final int position) {
            holder.isActive = mItems.get(position).isInUse();
            if(holder.isActive ) {
                holder.rule_edit_layout.setBackgroundColor(getResources().getColor(R.color.activeItem));
            }else{
                holder.rule_edit_layout.setBackgroundColor(Color.TRANSPARENT);
            }
            holder.rule_name.setText(mItems.get(position).getName());
            holder.rule_pattern.setText(mItems.get(position).getPattern());
            holder.rule_formula.setText(mItems.get(position).getFormula());
            holder.rule_inuse.setChecked(mItems.get(position).isInUse());
            holder.rule_edit_delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeRule(holder.getLayoutPosition());
                    CRApp.appdataaccess.saveAppData();
                }
            });
            holder.rule_edit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                    Rule currRule = mItems.get(holder.getLayoutPosition());
                    if(holder.rule_inuse.isChecked()==currRule.isInUse() &&
                            holder.rule_name.getText().toString().equals(currRule.getName()) &&
                            holder.rule_pattern.getText().toString().equals(currRule.getPattern()) &&
                            holder.rule_formula.getText().toString().equals(currRule.getFormula())){
                        Toast.makeText(CRApp.context, R.string.message_after_save_no_change, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Rule rule = new Rule(holder.rule_name.getText().toString(), holder.rule_pattern.getText().toString(), holder.rule_formula.getText().toString());
                    rule.setInUse(holder.rule_inuse.isChecked());
                    if(!validateRule(rule, holder.getLayoutPosition())){
                        return;
                    }
                    ruleGroup.setRule(holder.getLayoutPosition(), rule);
                    CRApp.appdataaccess.saveAppData();
                    Toast.makeText(CRApp.context, R.string.message_edit_rule_saved, Toast.LENGTH_SHORT).show();

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
            holder.rule_edit_copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ruleGroup.getRules().size()>=Constant.MAX_RULES){
                        Toast.makeText(CRApp.context, String.format(getString(R.string.message_add_rule_max_rules), Constant.MAX_RULES), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Rule currRule = mItems.get(holder.getLayoutPosition());
                    int i = (int) (new Date().getTime()/1000);
                    String newRuleName = currRule.getName()+"-"+i;
                    Rule rule = new Rule(newRuleName,
                            currRule.getPattern(),
                            currRule.getFormula());
                    ruleGroup.addRule(rule);
                    CRApp.appdataaccess.saveAppData();
                    Toast.makeText(CRApp.context, String.format(getString(R.string.message_copy_rule_copied_to), newRuleName), Toast.LENGTH_SHORT).show();
                    adapter.notifyItemInserted(ruleGroup.getRules().size()-1);
                }
            });

        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
        @Override
        public void onItemDismiss(int position) {
            mItems.remove(position);
            notifyItemRemoved(position);
            CRApp.appdataaccess.saveAppData();
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            Collections.swap(mItems, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            CRApp.appdataaccess.saveAppData();
            return true;
        }
    }
}
