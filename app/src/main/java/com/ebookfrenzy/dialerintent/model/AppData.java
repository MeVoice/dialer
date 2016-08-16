package com.ebookfrenzy.dialerintent.model;

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
    public static final int ERROR_GROUPNAME_TOOSHORT=1;
    public static final int ERROR_GROUPNAME_DUP=2;

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

    public int validateRuleGroup(RuleGroup rg, int position){
        //name is not blank
        if(rg.getName().length()<5){
            return ERROR_GROUPNAME_TOOSHORT;
        }
        //no name duplicate
        for(int i=0;i<this.ruleGroups.size();i++){
            if(i==position){
                continue;
            }
            if(ruleGroups.get(i).getName().equalsIgnoreCase(rg.getName())){
                return ERROR_GROUPNAME_DUP;
            }
        }
        return 0;
    }
}
