package com.ebookfrenzy.dialerintent;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.Toast;

import com.ebookfrenzy.dialerintent.helper.ItemTouchHelperAdapter;
import com.ebookfrenzy.dialerintent.helper.ItemTouchHelperViewHolder;
import com.ebookfrenzy.dialerintent.helper.OnStartDragListener;
import com.ebookfrenzy.dialerintent.helper.SimpleItemTouchHelperCallback;
import com.ebookfrenzy.dialerintent.model.AppData;
import com.ebookfrenzy.dialerintent.model.MatchNumber;
import com.ebookfrenzy.dialerintent.model.Rule;
import com.ebookfrenzy.dialerintent.model.RuleGroup;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RulesFragment extends CommonFragment  implements OnStartDragListener {
    public EditText test_reroute_number;
    private int removePosition=-1;
    private RuleGroup ruleGroup;
    private ItemTouchHelper mItemTouchHelper;

    ContentAdapter adapter;
    private EventBus bus = EventBus.getDefault();


    public RulesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initAppData();
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rule, container, false);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.rules_recycler_view);
        ruleGroup = appdata.getRuleGroup(appdata.getGroupInEdit());
        adapter = new ContentAdapter(recyclerView.getContext(), ruleGroup.getRules());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        test_reroute_number = (EditText) v.findViewById(R.id.test_reroute_number);
        ImageButton test_reroute_button = (ImageButton) v.findViewById(R.id.test_reroute_button);
        test_reroute_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                MatchNumber number = new MatchNumber(test_reroute_number.getText().toString());
                String matching_pattern = "None";
                String result = number.getNumber();
                if(ruleGroup.transform(number)){
                    matching_pattern = number.getMatchingRule().getPattern();
                    result = number.getResult();
                }
                Toast.makeText(context, "Matching pattern: " + matching_pattern + "\nresult: " + result, Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public boolean validateRule(Rule rule, int position){
        if(!rule.validate()){
            Toast.makeText(context, rule.getValidationResult(), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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

    public class RuleViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
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
        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

    public class ContentAdapter extends RecyclerView.Adapter<RuleViewHolder> implements ItemTouchHelperAdapter {
        // Set numbers of List in RecyclerView.
        private final List<Rule> mItems;

        public ContentAdapter(Context context, List<Rule> rules) {
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
                    Toast.makeText(context, "rule changed", Toast.LENGTH_SHORT).show();

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
        @Override
        public void onItemDismiss(int position) {
            mItems.remove(position);
            notifyItemRemoved(position);
            appdataaccess.saveAppData(appdata);
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            Collections.swap(mItems, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            appdataaccess.saveAppData(appdata);
            return true;
        }
    }
}
