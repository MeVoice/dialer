package com.mevoice.callrouter.model;

import com.mevoice.callrouter.Constant;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Admin on 8/10/2016.
 */
public class AppData {
    private int groupInEdit=-1;
    private int ruleInEdit=-1;

    public int getLoadTimes() {
        return loadTimes;
    }

    public void setLoadTimes(int loadTimes) {
        this.loadTimes = loadTimes;
    }

    private int loadTimes=0;

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

    public List<RuleGroup> getRuleGroups() {
        return ruleGroups;
    }

    public void setRuleGroups(List<RuleGroup> ruleGroups) {
        this.ruleGroups = ruleGroups;
    }

    public void addRuleGroup(RuleGroup rg){
        this.ruleGroups.add(rg);
    }

    public String copyRuleGroup(int position){
        RuleGroup sourceRG = ruleGroups.get(position);
        List<Rule> sourceRules = sourceRG.getRules();
        int i = (int) (new Date().getTime()/1000);
        String rgName = sourceRG.getName()+"-" + i;
        RuleGroup newRG = new RuleGroup(rgName);
        List<Rule> rules=new ArrayList<>();
        for(i=0;i<sourceRules.size();i++){
            Rule r=new Rule(sourceRules.get(i).getName(), sourceRules.get(i).getPattern(), sourceRules.get(i).getFormula());
            r.setInUse(sourceRules.get(i).isInUse());
            rules.add(r);
        }
        newRG.setRules(rules);
        ruleGroups.add(newRG);
        return rgName;
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
    public int getRuleGroupInUse(){
        for(int i=0;i<ruleGroups.size();i++){
            if(ruleGroups.get(i).isInUse()){
                return i;
            }
        }
        return -1;
    }

}
