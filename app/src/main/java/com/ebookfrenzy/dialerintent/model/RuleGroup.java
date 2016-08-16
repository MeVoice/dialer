package com.ebookfrenzy.dialerintent.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 8/10/2016.
 */
public class RuleGroup {
    private String name;
    private boolean inUse=false;
    List<Rule> rules=new ArrayList<>();

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public RuleGroup(String name) {
        this.name = name;
    }

    public boolean addRule(Rule rule){
        if(!rule.validate()){
            return false;
        }
        this.rules.add(rule);
        setRuleSequence();
        return true;
    }

    public boolean updateRule(Rule rule, int position){
        if(!rule.validate()){
            return false;
        }
        this.rules.set(position, rule);
        setRuleSequence();
        return true;
    }

    public void removeRule(int position){
        this.rules.remove(position);
        setRuleSequence();
    }
    public Rule getRule(int position){
        return this.rules.get(position);
    }
    public void setRule (int position, Rule rule){
        rules.set(position, rule);
    }
    private void setRuleSequence(){
        for(int i=0;i<rules.size();i++){
            rules.get(i).setSequence(i);
        }
    }
}
