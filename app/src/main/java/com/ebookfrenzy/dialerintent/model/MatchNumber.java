package com.ebookfrenzy.dialerintent.model;

/**
 * Created by Admin on 8/10/2016.
 */
public class MatchNumber {
    public MatchNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Rule getMatchingRule() {
        return matchingRule;
    }

    public void setMatchingRule(Rule rule) {
        this.matchingRule = rule;
    }

    private String number;
    private Rule matchingRule;
    private String result=null;
}
