package com.mevoice.callrouter;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import com.mevoice.callrouter.model.RuleGroup;
import com.mevoice.callrouter.events.ClickEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {
    private int removePosition=-1;
    private int lastRemoveTime=0;
    ContentAdapter adapter;
    View rootView;
    private EventBus bus = EventBus.getDefault();

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_group, container, false);

        // Inflate the layout for this fragment
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.group_recycler_view);
        adapter = new ContentAdapter(recyclerView.getContext(), CRApp.appdata.getRuleGroups());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return rootView;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(CRApp.appdata.getGroups_loadTimes()==0){
            Snackbar.make(rootView, getString(R.string.greeting_groups), Snackbar.LENGTH_INDEFINITE).show();
        }
        CRApp.appdata.setGroups_loadTimes(CRApp.appdata.getGroups_loadTimes()+1);
    }
    public boolean validateGroup(RuleGroup rg, int position){
        switch(CRApp.appdata.validateRuleGroup(rg, position)) {
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

    public void removeGroup(int position){
        int i = (int) (new Date().getTime()/1000);
        if(removePosition!=position || i-lastRemoveTime>5){
            Toast.makeText(CRApp.context, R.string.message_delete_group_clickagain, Toast.LENGTH_SHORT).show();
            removePosition=position;
            lastRemoveTime=i;
            return;
        }
        removePosition=-1;
        CRApp.appdata.removeGroup(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(CRApp.context, R.string.message_delete_group_deleted, Toast.LENGTH_SHORT).show();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        public EditText group_name;
        public CheckBox group_inuse;
        public ImageButton group_edit_rules;
        public ImageButton group_edit_delete_button;
        public ImageButton group_edit_button;
        public ImageButton group_edit_copy;
        public ImageButton group_edit_done;
        public ImageButton group_edit_cancel;
        public LinearLayout group_edit_layout;
        public GroupViewHolder(View itemView) {
            super(itemView);
            group_name = (EditText) itemView.findViewById(R.id.group_edit_name);
            group_inuse = (CheckBox) itemView.findViewById(R.id.group_edit_inuse);
            group_edit_rules = (ImageButton) itemView.findViewById(R.id.group_edit_rules);
            group_edit_delete_button = (ImageButton) itemView.findViewById(R.id.group_edit_delete_button);
            group_edit_button = (ImageButton) itemView.findViewById(R.id.group_edit_button);
            group_edit_done = (ImageButton) itemView.findViewById(R.id.group_edit_done);
            group_edit_copy = (ImageButton) itemView.findViewById(R.id.group_edit_copy);
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
            View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.group_edit, parent, false);
            return new GroupViewHolder(view);
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
                    ClickEvent ev = new ClickEvent();
                    CRApp.appdataaccess.setRuntimeData(Constant.RUNTIME_DATA_GROUPINEDIT, holder.getLayoutPosition());
                    ev.put(Constant.EVENT_TYPE_ACTION, Constant.ACTION_EDIT_RULES);
                    bus.post(ev);
                }
            });
            holder.group_edit_delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeGroup(holder.getLayoutPosition());
                    CRApp.appdataaccess.saveAppData();
                }
            });
            holder.group_edit_copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CRApp.appdata.getRuleGroups().size() >= Constant.MAX_GROUPS) {
                        Toast.makeText(CRApp.context, String.format(getResources().getString(R.string.message_add_group_max_groups), Constant.MAX_GROUPS), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String rgName = CRApp.appdata.copyRuleGroup(holder.getLayoutPosition());
                    Toast.makeText(CRApp.context, String.format(getResources().getString(R.string.message_copy_group_copied_to), rgName), Toast.LENGTH_SHORT).show();
                    adapter.notifyItemInserted(CRApp.appdata.getRuleGroups().size()-1);
                }
            });
            holder.group_edit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                    RuleGroup currRG = mItems.get(holder.getLayoutPosition());
                    if(holder.group_inuse.isChecked()==currRG.isInUse() && holder.group_name.getText().toString().equals(currRG.getName())){
                        Toast.makeText(CRApp.context, R.string.message_after_save_no_change, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    RuleGroup rg = new RuleGroup(holder.group_name.getText().toString());
                    rg.setInUse(holder.group_inuse.isChecked());
                    if(!validateGroup(rg, holder.getLayoutPosition())){
                        return;
                    }
                    rg.setRules(currRG.getRules());
                    int inUseIndex = CRApp.appdata.getRuleGroupInUse();
                    if(rg.isInUse() && inUseIndex>=0 && inUseIndex!=holder.getLayoutPosition()){
                        mItems.get(inUseIndex).setInUse(false);
                        notifyItemChanged(inUseIndex);
                    }
                    CRApp.appdata.setGroup(holder.getLayoutPosition(), rg);
                    int newInUseIndex = CRApp.appdata.getRuleGroupInUse();
                    boolean wasInUse = inUseIndex>=0;
                    boolean nowInUse = newInUseIndex>=0;
                    if(wasInUse != nowInUse) {
                        ClickEvent ev = new ClickEvent();
                        ev.put(Constant.EVENT_TYPE_ACTION, Constant.ACTION_ROUTER_ON_OFF);
                        bus.post(ev);
                    }
                    CRApp.appdataaccess.saveAppData();
                    Toast.makeText(CRApp.context, R.string.message_edit_group_saved, Toast.LENGTH_SHORT).show();

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
