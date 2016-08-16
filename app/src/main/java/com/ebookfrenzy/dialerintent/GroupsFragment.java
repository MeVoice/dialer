package com.ebookfrenzy.dialerintent;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ebookfrenzy.dialerintent.R;
import com.ebookfrenzy.dialerintent.events.ClickEvent;
import com.ebookfrenzy.dialerintent.model.AppData;
import com.ebookfrenzy.dialerintent.model.Rule;
import com.ebookfrenzy.dialerintent.model.RuleGroup;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {
    public Context context;
    public AppDataAccess appdataaccess;
    public AppData appdata;
    public EditText group_add_name;
    public ImageButton group_add_button;
    private int removePosition=-1;
    ContentAdapter adapter;
    private EventBus bus = EventBus.getDefault();

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        appdataaccess = AppDataAccess.getInstance(context);
        appdata = appdataaccess.getAppdata();

        View v = inflater.inflate(R.layout.fragment_group, container, false);

        // Inflate the layout for this fragment
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);
        adapter = new ContentAdapter(recyclerView.getContext(), appdata.getRuleGroups());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //return recyclerView;
        group_add_name = (EditText) v.findViewById(R.id.group_add_name);
        group_add_button = (ImageButton) v.findViewById(R.id.group_add_button);
        group_add_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                addGroup();
            }
        });
        return v;
    }

    public boolean validateGroup(RuleGroup rg, int position){
        switch(appdata.validateRuleGroup(rg, position)) {
            case AppData.ERROR_GROUPNAME_TOOSHORT:
                Toast.makeText(context, "group name at least 5 characters long", Toast.LENGTH_SHORT).show();
                return false;
            case AppData.ERROR_GROUPNAME_DUP:
                Toast.makeText(context, "group name already in use", Toast.LENGTH_SHORT).show();
                return false;
            default:
                return true;
        }
    }
    public void addGroup(){
        RuleGroup rg = new RuleGroup(group_add_name.getText().toString());
        if(validateGroup(rg, -1)){
            appdata.addRuleGroup(rg);
            adapter.notifyItemInserted(appdata.getRuleGroups().size()-1);
            appdataaccess.saveAppData(appdata);
            group_add_name.setText("");
            Toast.makeText(context, "group added", Toast.LENGTH_SHORT).show();
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public EditText group_name;
        public CheckBox group_inuse;
        public ImageButton group_edit_rules;
        public ImageButton group_edit_delete_button;
        public ImageButton group_edit_button;
        public ImageButton group_edit_done;
        public ImageButton group_edit_cancel;
        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.group_edit, parent, false));
            //itemView is from Parent class, represent any view within the holder
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();

                }
            });
            group_name = (EditText) itemView.findViewById(R.id.group_edit_name);
            group_inuse = (CheckBox) itemView.findViewById(R.id.group_edit_inuse);

            group_edit_rules = (ImageButton) itemView.findViewById(R.id.group_edit_rules);
            group_edit_delete_button = (ImageButton) itemView.findViewById(R.id.group_edit_delete_button);
            group_edit_button = (ImageButton) itemView.findViewById(R.id.group_edit_button);
            group_edit_done = (ImageButton) itemView.findViewById(R.id.group_edit_done);
            group_edit_cancel = (ImageButton) itemView.findViewById(R.id.group_edit_cancel);
        }
    }
    public class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        // Set numbers of List in RecyclerView.
        private final List<RuleGroup> mItems;
        private final Context mContext;

        public ContentAdapter(Context context, List<RuleGroup> groups) {
            mContext = context;
            mItems = groups;
            System.out.println("group count: " + groups.size());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.group_name.setText(mItems.get(position).getName());
            holder.group_inuse.setChecked(mItems.get(position).isInUse());
            holder.group_edit_rules.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePosition=-1;
                    HashMap p=new HashMap();
                    p.put("username", "nice");
                    ClickEvent ev = new ClickEvent(p);
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
                    appdata.setGroup(holder.getLayoutPosition(), rg);
                    for(int i=0;i<mItems.size();i++){
                        if(holder.getLayoutPosition()==i){
                            continue;
                        }
                        if(mItems.get(i).isInUse()){
                            mItems.get(i).setInUse(false);
                            notifyItemChanged(i);
                        }
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
