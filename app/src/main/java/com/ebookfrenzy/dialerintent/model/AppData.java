package com.ebookfrenzy.dialerintent.model;

import java.util.List;

/**
 * Created by Admin on 8/10/2016.
 */
public class AppData {
    private boolean editNumber=false;
    private boolean rerouteCall=false;
    private boolean confirmBeforeReroute=false;
    private boolean permissionGranted=false;

    public String getProfix() {
        return profix;
    }

    public void setProfix(String profix) {
        this.profix = profix;
    }

    private String profix="";
    private List<RuleGroup> ruleGroups=null;

    public AppData(){
    }

    public boolean isConfirmBeforeReroute() {
        return confirmBeforeReroute;
    }

    public void setConfirmBeforeReroute(boolean confirmBeforeReroute) {
        this.confirmBeforeReroute = confirmBeforeReroute;
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
}
