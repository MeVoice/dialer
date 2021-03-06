package com.demitive.callrouter.model;

import com.demitive.callrouter.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Admin on 8/10/2016.
 */
public class Rule {
    private String name;
    private String pattern;
    private String formula;
    private boolean inUse;

    public boolean isValidated() {
        return validated;
    }

    private boolean validated=false;
    private int sequence;
    private int validationResult;

    public int getValidationResultErrorField() {
        return validationResultErrorField;
    }

    private int validationResultErrorField;


    public List<String> getFormulaParts() {
        return formulaParts;
    }

    private List<String> formulaParts;

    public int getValidationResult() {
        return validationResult;
    }


    public boolean setInUse(boolean inUse) {
        if(inUse && !validated) {
            validate();
            if (!validated) {
                return false;
            }
        }
        this.inUse = inUse;
        return true;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getFormula() {
        return formula;
    }

    public boolean isInUse() {
        return inUse;
    }

    public String getName() {
        return name;
    }

    public String getPattern() {
        return pattern;
    }

    public int getSequence() {
        return sequence;
    }

    public Rule(String name, String pattern, String formula){
        this.name = name;
        this.pattern = pattern;
        this.formula = formula;
        this.formulaParts = breakByPattern(formula, FormulaMatchFormat);
    }

    private static final String PhoneCharacters = "0123456789#*,;";
    private static final String PatternCharacters = "{}DX% +";
    private static final String FormulaCharacters = "{}M +";
    private static final String FormulaMatchFormat = "\\{M\\d+\\}";

    public boolean validate(){
        validationResult=0;
        String patternCharacters = PhoneCharacters.concat(PatternCharacters);
        String formularCharacters = PhoneCharacters.concat(FormulaCharacters);
        Pattern r;
        for(int i=0; i<pattern.length(); i++){
            if(patternCharacters.indexOf(pattern.charAt(i))<0){
                validationResult= R.string.message_validate_rule_invalid_character;
                validationResultErrorField=R.string.literal_validate_rule_pattern;
                return false;
            }
        }
        for(int i=0; i<formula.length(); i++){
            if(formularCharacters.indexOf(formula.charAt(i))<0){
                validationResult= R.string.message_validate_rule_invalid_character;
                validationResultErrorField=R.string.literal_validate_rule_formula;
                return false;
            }
        }
        String[][] errorPatterns = {
                {"^.{0,2}$", "both", String.valueOf(R.string.message_validate_rule_too_short)},
                {"^[^\\{]+\\+", "both", String.valueOf(R.string.message_validate_rule_plus_first)},
                {"%[^\\}]+.*$", "pattern", String.valueOf(R.string.message_validate_rule_percent_last)},
                {"^[\\+\\{]%", "pattern", String.valueOf(R.string.message_validate_rule_percent_not_first)},
                {"\\{[^\\}]?\\{", "both", String.valueOf(R.string.message_validate_rule_pair_brackets)},
                {"\\}[^\\{]?\\}", "both", String.valueOf(R.string.message_validate_rule_pair_brackets)},
                {"\\{\\}", "both", String.valueOf(R.string.message_validate_rule_empty_brackets)},
                {"[\\{|\\}]%", "pattern", String.valueOf(R.string.message_validate_rule_bracket_before_percent)}
        };
        for(int i=0; i<errorPatterns.length;i++){
            r = Pattern.compile(errorPatterns[i][0]);
            if((errorPatterns[i][1].equals("both") || errorPatterns[i][1].equals("pattern")) && r.matcher(pattern).find()){
                validationResult=Integer.parseInt(errorPatterns[i][2]);
                validationResultErrorField=R.string.literal_validate_rule_pattern;
                return false;
            }
            if((errorPatterns[i][1].equals("both") || errorPatterns[i][1].equals("formula")) && r.matcher(formula).find()){
                validationResult=Integer.parseInt(errorPatterns[i][2]);
                validationResultErrorField=R.string.literal_validate_rule_formula;
                return false;
            }
        }

        //odd number of brackets
        int pCount1 = pattern.length() - pattern.replace("{", "").length();
        int pCount2 = pattern.length() - pattern.replace("}", "").length();
        if(pCount1!=pCount2) {
            validationResult = R.string.message_validate_rule_pair_brackets;
            validationResultErrorField=R.string.literal_validate_rule_pattern;
            return false;
        }
        int fCount1 = formula.length() - formula.replace("{", "").length();
        int fCount2 = formula.length() - formula.replace("}", "").length();
        if(fCount1!=fCount2) {
            validationResult = R.string.message_validate_rule_pair_brackets;
            validationResultErrorField=R.string.literal_validate_rule_formula;
            return false;
        }
        //only {M\d+} in formula
        String[] matches = formula.split("\\{M\\d+\\}");
        String temp = PhoneCharacters+" +";
        for(int i=0;i<matches.length;i++) {
            for (int j = 0; j < matches[i].length(); j++) {
                if (temp.indexOf(matches[i].charAt(j)) < 0) {
                    validationResult = R.string.message_validate_rule_invalid_format;
                    validationResultErrorField=R.string.literal_validate_rule_formula;
                    return false;
                }
            }
        }
        //max match index in formula must <= { count in pattern
        int max_idx_match=-1;
        for(int i=0; i<(formulaParts.size()-1)/2; i++) {
            String part = formulaParts.get(i*2+1);
            int idx_match = Integer.valueOf(part.substring(2, part.length()-1));
            if (idx_match > max_idx_match) {
                max_idx_match = idx_match;
            }
        }
        if(max_idx_match>pCount1){
            validationResult = R.string.message_validate_rule_more_match_in_formula;
            validationResultErrorField=R.string.literal_validate_rule_formula;
            return false;
        }
        validated = true;
        return true;
    }

    private static final int StateOpen = 1;
    private static final int StateInMatch = 2;
    private static final int WildCharNone = 0;
    private static final int WildCharDigit = 3;
    private static final int WildCharAny = 4;
    private static final String Digits = "0123456789";
    public boolean transform(MatchNumber number){
        if(!validated){
            validate();
        }
        if(!validated){
            return false;
        }
        //literal string match from the beginning
        //D matches one number
        //D* matches any sequence of number, towards the end of number
        //X matches on character
        //X* matches any sequence of characters, towards the end of number
        String inputNumber = number.getNumber();
        String pattern = this.pattern.replace(" ", "");
        int matchState=StateOpen;
        int wildCharType=WildCharNone;
        int idx_number = 0, idx_pattern = 0, idx_formula=0;
        boolean charMatched = false;
        String match="";
        List<String> matches = new ArrayList<String>();
        matches.add(inputNumber);
        for(idx_number=0; idx_number<inputNumber.length(); idx_number++) {
            charMatched = false;
            if(idx_pattern>=pattern.length()){
                return false;
            }
            if (matchState == StateInMatch && pattern.charAt(idx_pattern) == '}') {
                wildCharType = WildCharNone;
                matchState = StateOpen;
                matches.add(match);
                idx_pattern++;
                if(idx_pattern>=pattern.length()){
                    return false;
                }
            }
            if (matchState == StateOpen && pattern.charAt(idx_pattern) == '{') {
                wildCharType = WildCharNone;
                matchState = StateInMatch;
                match = "";
                idx_pattern++;
                if(idx_pattern>=pattern.length()){
                    return false;
                }
            }
            if (pattern.charAt(idx_pattern) == 'D') {
                wildCharType = WildCharDigit;
                if (Digits.indexOf(inputNumber.charAt(idx_number)) >= 0) {
                    charMatched = true;
                }
                idx_pattern++;
            } else if (pattern.charAt(idx_pattern) == 'X') {
                wildCharType = WildCharAny;
                charMatched = true;
                idx_pattern++;
            } else if (pattern.charAt(idx_pattern) == '%'){
                if (wildCharType == WildCharAny) {
                    charMatched = true;
                } else if (wildCharType == WildCharDigit && Digits.indexOf(inputNumber.charAt(idx_number)) >= 0) {
                    charMatched = true;
                } else if (wildCharType == WildCharNone && inputNumber.charAt(idx_number) == inputNumber.charAt(idx_number - 1)) {
                    charMatched = true;
                } else {
                    return false;
                }
            } else {
                wildCharType=WildCharNone;
                if(inputNumber.charAt(idx_number)==pattern.charAt(idx_pattern)){
                    charMatched = true;
                }
                idx_pattern++;
            }
            if(!charMatched){
                return false;
            }
            if (matchState == StateInMatch) {
                match += inputNumber.charAt(idx_number);
            }
        }
        if(!charMatched){
            return false;
        }
        if(matchState == StateInMatch){
            matches.add(match);
            if(idx_pattern<pattern.length()) {
                if (pattern.substring(idx_pattern).equals("}") || pattern.substring(idx_pattern).equals("%}")) {
                    idx_pattern = pattern.length();
                }
            }
        }
        if(idx_pattern<pattern.length()){
            return false;
        }

        //combine matches from number and literals from formula
        //formulaParts.size() is an odd number, odd positions are {M1}, even positions are literals from formula
        String resultNumber = "";
        for(int i = 0; i< formulaParts.size(); i++){
            if(i%2==0){
                resultNumber += formulaParts.get(i);
            }else{
                int idx_match = Integer.valueOf(formulaParts.get(i).substring(2, formulaParts.get(i).length()-1));
                resultNumber += matches.get(idx_match);
            }
        }
        //remove any space characters
        resultNumber = resultNumber.replace(" ", "");
        number.setResult(resultNumber);
        number.setMatchingRule(this);
        return true;
    }

    static public List<String> breakByPattern(String text, String pattern){
        int i=0;
        List<String> matches = new ArrayList<String>();
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while(m.find(i)){
            matches.add(text.substring(i, m.start()));
            matches.add(text.substring(m.start(), m.end()));
            i=m.end();
        }
        matches.add(text.substring(i));
        return matches;
    }
}
