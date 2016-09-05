package com.mevoice.callrouter;

import com.mevoice.callrouter.model.MatchNumber;
import com.mevoice.callrouter.model.Rule;

import junit.framework.TestCase;

import static org.junit.Assert.*;

/**
 * Created by Admin on 8/11/2016.
 */

public class RuleUnitTest extends TestCase{
    public void test_validate() {
        Rule rule001;
        rule001=new Rule("normal rule", "+{86D%}", "+123");
        assertTrue(rule001.validate());
        rule001=new Rule("invalid character in pattern", "1L212", "+123");
        assertFalse(rule001.validate());
        rule001=new Rule("invalid character in formula", "1212", "+123K");
        assertFalse(rule001.validate());
        rule001=new Rule("+ not in beginning", "12+12", "+123");
        assertFalse(rule001.validate());
        rule001=new Rule("+ not in beginning", "{+1212}", "+123");
        assertTrue(rule001.validate());
        rule001=new Rule("D% not at end", "1212D%1", "+123");
        assertFalse(rule001.validate());
        rule001=new Rule("D% at end", "121{2D%}", "+123");
        assertTrue(rule001.validate());
        rule001=new Rule("1% not at end", "12121%2", "+123");
        assertFalse(rule001.validate());
        rule001=new Rule("% not at end", "{DDDDDD} %{DDDD}", "+123");
        assertFalse(rule001.validate());
        rule001=new Rule("unmatched bracket", "121{2D%{1", "+123");
        assertFalse(rule001.validate());
        rule001=new Rule("unmatched bracket", "121{2}D%{1", "+123");
        assertFalse(rule001.validate());
        rule001=new Rule("nesting bracket", "12{1{2}D%}1", "+123");
        assertFalse(rule001.validate());
        rule001=new Rule("bracket before %", "1212D{%1}", "+123");
        assertFalse(rule001.validate());
        rule001=new Rule("{}", "1212D{}", "+123");
        assertFalse(rule001.validate());
        rule001=new Rule("bad match char", "1212D{123}", "+123{M12M3}");
        assertFalse(rule001.validate());
        rule001=new Rule("bad match char", "1212D{123}", "+123{M12}M3");
        assertFalse(rule001.validate());
        rule001=new Rule("too many matches in formula0", "1212D{123}", "+123{M1}{M2}");
        assertFalse(rule001.validate());
        rule001=new Rule("too many matches in formula1", "1212123", "+123{M1}");
        assertFalse(rule001.validate());
        rule001=new Rule("% at beginning of pattern", "+{%}1212123", "+123{M1}");
        assertFalse(rule001.validate());
    }

    public void test_breakByPattern(){
        Rule rule_001 = new Rule("rule001", "+{86D%}", "+123{M1}48{M2}59");
        String[] parts_001 = {"+123", "{M1}", "48", "{M2}", "59"};
        assertArrayEquals(parts_001, rule_001.getFormulaParts().toArray());
        Rule rule_002 = new Rule("rule002", "+{86D%}", "+123{M1}");
        String[] parts_002 = {"+123", "{M1}", ""};
        assertArrayEquals(parts_002, rule_002.getFormulaParts().toArray());
    }

    public void test_transform() {
        Rule rule_001 = new Rule("rule001", "+{86D%}", "+123{M1}");
        MatchNumber number_001 = new MatchNumber("+8611123456");
        assertTrue(rule_001.transform(number_001));
        assertTrue(number_001.getResult().equals("+1238611123456"));

        Rule rule_002 = new Rule("rule002", "+{1D%}", "17910,87878787#,00{M1}#");
        MatchNumber number_002 = new MatchNumber("+18001234567");
        assertTrue(rule_002.transform(number_002));
        assertTrue(number_002.getResult().equals("17910,87878787#,0018001234567#"));
        rule_002 = new Rule("rule002 space characters", "+{1D%}", "17910 ,87878787#,00{M1}#");
        assertTrue(rule_002.transform(number_002));
        assertTrue(number_002.getResult().equals("17910,87878787#,0018001234567#"));
        rule_002 = new Rule("rule002 leading bracket", "+{X%}", "17910 ,87878787#,00{M1}#");
        assertTrue(rule_002.transform(number_002));
        assertTrue(number_002.getResult().equals("17910,87878787#,0018001234567#"));


        MatchNumber number_003 = new MatchNumber("");
        Rule rule_003 = new Rule("rule003 all literal", "123", "1791087");
        assertFalse(rule_003.transform(number_003));
        number_003 = new MatchNumber("123");
        assertTrue(rule_003.transform(number_003));
        assertTrue(number_003.getResult().equals("1791087"));
        number_003 = new MatchNumber("1234");
        assertFalse(rule_003.transform(number_003));

        MatchNumber number_004 = new MatchNumber("671234");
        Rule rule_004 = new Rule("rule004 office number expansion", "67{DDDD}", "1 800 980 9891,{M1}#");
        assertTrue(rule_004.transform(number_004));
        assertTrue(number_004.getResult().equals("18009809891,1234#"));
        number_004 = new MatchNumber("6712345");
        assertFalse(rule_004.transform(number_004));
        rule_004 = new Rule("rule004 conf number", "{DDD} {DDD}", "1 800 980 9891,{M1}#,7,{M2}#");
        number_004 = new MatchNumber("123456");
        assertTrue(rule_004.transform(number_004));
        assertTrue(number_004.getResult().equals("18009809891,123#,7,456#"));
        rule_004 = new Rule("rule004 number contains non digit", "12{D%}", "1 800 980 9891,{M1}");
        number_004 = new MatchNumber("123,44");
        assertFalse(rule_004.transform(number_004));
        rule_004 = new Rule("rule004 number contains non digit2", "12{X%}", "1 800 980 9891,{M1}");
        number_004 = new MatchNumber("123,44");
        assertTrue(rule_004.transform(number_004));
        rule_004 = new Rule("rule004 % match 0 times", "1{2%}", "1 800 980 9891,{M1}");
        number_004 = new MatchNumber("12");
        assertTrue(rule_004.transform(number_004));
    }
}