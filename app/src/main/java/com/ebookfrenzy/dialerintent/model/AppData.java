package com.ebookfrenzy.dialerintent.model;

import com.ebookfrenzy.dialerintent.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 8/10/2016.
 */
public class AppData {
    private boolean editNumber=false;
    private boolean rerouteCall=false;
    private boolean confirmReroute =false;
    private boolean permissionGranted=false;
    private int groupInEdit=-1;
    private int ruleInEdit=-1;

    public List<TransformLog> getTransformLog() {
        return transformLog;
    }

    private List<TransformLog> transformLog=new ArrayList<>() ;
    public void addTransformLog(TransformLog log){
        transformLog.add(log);
    }

    public int getGroupInEdit() {
        return groupInEdit;
    }

    public void setGroupInEdit(int groupInEdit) {
        this.groupInEdit = groupInEdit;
    }

    public int getRuleInEdit() {
        return ruleInEdit;
    }

    public void setRuleInEdit(int ruleInEdit) {
        this.ruleInEdit = ruleInEdit;
    }

    private List<RuleGroup> ruleGroups=new ArrayList();;

    public AppData(){
    }

    public boolean isConfirmReroute() {
        return confirmReroute;
    }

    public void setConfirmReroute(boolean confirmReroute) {
        this.confirmReroute = confirmReroute;
    }

    public boolean isEditNumber() {
        return editNumber;
    }

    public void setEditNumber(boolean editNumber) {
        this.editNumber = editNumber;
    }

    public boolean isPermissionGranted() {
        return permissionGranted;
    }

    public void setPermissionGranted(boolean permissionGranted) {
        this.permissionGranted = permissionGranted;
    }

    public boolean isRerouteCall() {
        return rerouteCall;
    }

    public void setRerouteCall(boolean rerouteCall) {
        this.rerouteCall = rerouteCall;
    }

    public List<RuleGroup> getRuleGroups() {
        return ruleGroups;
    }

    public void setRuleGroups(List<RuleGroup> ruleGroups) {
        this.ruleGroups = ruleGroups;
    }

    public void addRuleGroup(RuleGroup rg){
        this.ruleGroups.add(rg);
    }
    public void removeGroup(int position){
        this.ruleGroups.remove(position);
    }
    public void setGroup(int position, RuleGroup rg){
        this.ruleGroups.set(position, rg);
    }
    public RuleGroup getRuleGroup(int position){
        return this.ruleGroups.get(position);
    }

    public int validateRuleGroup(RuleGroup rg, int position){
        //name is not blank
        if(rg.getName().length()<5){
            return Constant.ERROR_GROUPNAME_TOOSHORT;
        }
        //no name duplicate
        for(int i=0;i<this.ruleGroups.size();i++){
            if(i==position){
                continue;
            }
            if(ruleGroups.get(i).getName().equalsIgnoreCase(rg.getName())){
                return Constant.ERROR_GROUPNAME_DUP;
            }
        }
        return 0;
    }
    public RuleGroup getRuleGroupInUse(){
        for(int i=0;i<ruleGroups.size();i++){
            if(ruleGroups.get(i).isInUse()){
                return ruleGroups.get(i);
            }
        }
        return null;
    }
}
