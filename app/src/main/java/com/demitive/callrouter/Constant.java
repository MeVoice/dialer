package com.demitive.callrouter;

/**
 * Created by Admin on 8/17/2016.
 */
public class Constant {
    public static final int ERROR_GROUPNAME_TOOSHORT=1;
    public static final int ERROR_GROUPNAME_DUP=2;
    public static final String ACTION_GROUPS_RESET ="ACTION_RESET";
    public static final String ACTION_EDIT_RULES="ACTION_EDIT_RULE";
    public static final String ACTION_ROUTER_ON_OFF="ACTION_ROUTER_ON_OFF";
    public static final int MAX_GROUPS=10;
    public static final int MAX_RULES=30;

    public static final String ACTION_GROUP_VALIDATE = "ACTION_GROUP_VALIDATE";
    public static final String ACTION_GROUP_VALIDATE_RESULT = "ACTION_GROUP_VALIDATE_RESULT";
    public static final String FRAGMENT_KEY_GROUPS="groups";
    public static final String FRAGMENT_KEY_GROUP_ADD="group_add";
    public static final String FRAGMENT_KEY_RULES="rules";
    public static final String FRAGMENT_KEY_RULE_ADD="rule_add";
    public static final String FRAGMENT_KEY_HELP="help";
    public static final String FRAGMENT_KEY_SETTINGS ="settings" ;

    public static final String EVENT_TYPE_ACTION ="action";

    public static final String RUNTIME_DATA_GROUPINEDIT = "GROUPINEDIT";
}
