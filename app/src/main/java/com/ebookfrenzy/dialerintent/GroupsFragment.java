package com.ebookfrenzy.dialerintent;


import android.content.Context;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ebookfrenzy.dialerintent.events.ClickEvent;
import com.ebookfrenzy.dialerintent.model.AppData;
import com.ebookfrenzy.dialerintent.model.RuleGroup;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends CommonFragment {
    private int removePosition=-1;
    ContentAdapter adapter;
    private EventBus bus = EventBus.getDefault();

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initAppData();
        View v = inflater.inflate(R.layout.fragment_group, container, false);

        // Inflate the layout for this fragment
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);
        adapter = new ContentAdapter(recyclerView.getContext(), appdata.getRuleGroups());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return v;
    }

    public boolean validateGroup(RuleGroup rg, int position){
        switch(appdata.validateRuleGroup(rg, position)) {
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

    public void removeGroup(int position){
        if(removePosition!=position){
            Toast.makeText(context, "click again to delete", Toast.LENGTH_SHORT).show();
            removePosition=position;
            return;
        }
        removePosition=-1;
        appdata.removeGroup(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(context, "group removed", Toast.LENGTH_SHORT).show();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        public EditText group_name;
        public CheckBox group_inuse;
        public ImageButton group_edit_rules;
        public ImageButton group_edit_delete_button;
        public ImageButton group_edit_button;
        public ImageButton group_edit_done;
        public ImageButton group_edit_cancel;
        public LinearLayout group_edit_layout;
        public GroupViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.group_edit, parent, false));
            //itemView is from Parent class, represent any view within the holder
            group_name = (EditText) itemView.findViewById(R.id.group_edit_name);
            group_inuse = (CheckBox) itemView.findViewById(R.id.group_edit_inuse);
            group_edit_rules = (ImageButton) itemView.findViewById(R.id.group_edit_rules);
            group_edit_delete_button = (ImageButton) itemView.findViewById(R.id.group_edit_delete_button);
            group_edit_button = (ImageButton) itemView.findViewById(R.id.group_edit_button);
            group_edit_done = (ImageButton) itemView.findViewById(R.id.group_edit_done);
            group_edit_cancel = (ImageButton) itemView.findViewById(R.id.group_edit_cancel);
            group_edit_layout = (LinearLayout) itemView.findViewById(R.id.group_edit_layout);
        }
    }
    public class ContentAdapter extends RecyclerView.Adapter<GroupViewHolder> {
        // Set numbers of List in RecyclerView.
        private final List<RuleGroup> mItems;
        private final Context mContext;

        public ContentAdapter(Context context, List<RuleGroup> groups) {
            mContext = context;
            mItems = groups;
        }

        @Override
        public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new GroupViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(final GroupViewHolder holder, final int position) {
            holder.group_name.setText(mItems.get(position).getName());
            holder.group_inuse.setChecked(mItems.get(position).isInUse());
            if(mItems.get(position).isInUse()){
                holder.group_edit_layout.setBackgroundColor(getResources().getColor(R.color.activeItem));
            }else{
                holder.group_edit_layout.setBackgroundColor(Color.TRANSPARENT);
            }
            holder.group_edit_rules.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePosition=-1;
                    ClickEvent ev = new ClickEvent();
                    appdata.setGroupInEdit(position);
                    ev.put("action", Constant.ACTION_EDIT_RULES);
                    bus.postSticky(ev);
                }
            });
            holder.group_edit_delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeGroup(holder.getLayoutPosition());
                    appdataaccess.saveAppData(appdata);
                }
            });
            holder.group_edit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePosition=-1;
                    holder.group_name.setEnabled(true);
                    holder.group_inuse.setEnabled(true);
                    holder.group_edit_cancel.setVisibility(View.VISIBLE);
                    holder.group_edit_done.setVisibility(View.VISIBLE);
                    holder.group_edit_button.setVisibility(View.INVISIBLE);
                    holder.group_edit_delete_button.setVisibility(View.INVISIBLE);
                }
            });
            holder.group_edit_done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePosition=-1;
                    RuleGroup currRG = mItems.get(holder.getLayoutPosition());
                    if(holder.group_inuse.isChecked()==currRG.isInUse() && holder.group_name.getText().toString().equals(currRG.getName())){
                        Toast.makeText(context, "no change detected", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    RuleGroup rg = new RuleGroup(holder.group_name.getText().toString());
                    rg.setInUse(holder.group_inuse.isChecked());
                    if(!validateGroup(rg, holder.getLayoutPosition())){
                        return;
                    }
                    rg.setRules(currRG.getRules());
                    int inUseIndex = appdata.getRuleGroupInUse();
                    if(rg.isInUse() && inUseIndex>=0 && inUseIndex!=holder.getLayoutPosition()){
                        mItems.get(inUseIndex).setInUse(false);
                        notifyItemChanged(inUseIndex);
                    }
                    appdata.setGroup(holder.getLayoutPosition(), rg);
                    int newInUseIndex = appdata.getRuleGroupInUse();
                    boolean wasInUse = inUseIndex>=0;
                    boolean nowInUse = newInUseIndex>=0;
                    if(wasInUse != nowInUse) {
                        ClickEvent ev = new ClickEvent();
                        ev.put("action", Constant.ACTION_ROUTER_ON_OFF);
                        if (nowInUse) {
                            ev.put("ON_OFF", new Boolean(true));
                        }else{
                            ev.put("ON_OFF", new Boolean(false));
                        }
                        bus.post(ev);
                    }
                    appdataaccess.saveAppData(appdata);
                    Toast.makeText(context, "group changed", Toast.LENGTH_SHORT).show();

                    holder.group_name.setEnabled(false);
                    holder.group_inuse.setEnabled(false);
                    holder.group_edit_cancel.setVisibility(View.INVISIBLE);
                    holder.group_edit_done.setVisibility(View.INVISIBLE);
                    holder.group_edit_button.setVisibility(View.VISIBLE);
                    holder.group_edit_delete_button.setVisibility(View.VISIBLE);
                    adapter.notifyItemChanged(holder.getLayoutPosition());
                }
            });
            holder.group_edit_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePosition=-1;
                    holder.group_name.setText(mItems.get(holder.getLayoutPosition()).getName());
                    holder.group_inuse.setChecked(mItems.get(holder.getLayoutPosition()).isInUse());
                    holder.group_name.setEnabled(false);
                    holder.group_inuse.setEnabled(false);
                    holder.group_edit_cancel.setVisibility(View.INVISIBLE);
                    holder.group_edit_done.setVisibility(View.INVISIBLE);
                    holder.group_edit_button.setVisibility(View.VISIBLE);
                    holder.group_edit_delete_button.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }
}
