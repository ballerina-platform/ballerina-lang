package org.ballerinalang.langserver.extensions.ballerina.syntaxhighlighter;

import java.util.HashMap;
import java.util.Map;

public enum ScopeEnum {


    SOURCE("source",1), KEYWORD("keyword",2), ENDPOINT("endpoint",0);

    private String key;
    private int value;

//    private static Map<String, Integer> scopeMap = new HashMap<String, Integer>();
//    static {
//        test();
//    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }


    ScopeEnum(String source, int i) {
        this.key = source;
        this.value = i;
    }
//
//    private static void test() {
//        for (ScopeEnum scopeEnum : ScopeEnum.values()) {
//            scopeMap.put(scopeEnum.key, scopeEnum.value);
//        }
//    }
//
//    public int getVal(String s) {
//        return scopeMap.get(s);
//    }
};