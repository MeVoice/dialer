package com.demitive.callrouter.model;

import com.demitive.callrouter.Constant;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Admin on 8/10/2016.
 */
public class AppData {
    private int loadTimes=0;
    private List<RuleGroup> ruleGroups=new ArrayList();;
    private boolean moreLog=false;
    private boolean numberRewrite=false;
    private int groups_loadTimes=0;
    private int rules_loadTimes=0;

    public boolean isEULA() {
        return EULA;
    }

    public void setEULA(boolean EULA) {
        this.EULA = EULA;
    }

    private boolean EULA=false;

    public int getGroups_loadTimes() {
        return groups_loadTimes;
    }

    public void setGroups_loadTimes(int groups_loadTimes) {
        this.groups_loadTimes = groups_loadTimes;
    }

    public int getRules_loadTimes() {
        return rules_loadTimes;
    }

    public void setRules_loadTimes(int rules_loadTimes) {
        this.rules_loadTimes = rules_loadTimes;
    }

    public boolean isNumberRewrite() {
        return numberRewrite;
    }

    public void setNumberRewrite(boolean numberRewrite) {
        this.numberRewrite = numberRewrite;
    }


    public boolean isMoreLog() {
        return moreLog;
    }

    public void setMoreLog(boolean moreLog) {
        this.moreLog = moreLog;
    }


    public int getLoadTimes() {
        return loadTimes;
    }

    public void setLoadTimes(int loadTimes) {
        this.loadTimes = loadTimes;
    }


    public List<TransformLog> getTransformLog() {
        return transformLog;
    }

    private List<TransformLog> transformLog=new ArrayList<>() ;
    public void addTransformLog(TransformLog log){
        transformLog.add(log);
    }

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
