/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*/
package org.ballerinalang.bre.nonblocking.debugger;

import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.model.values.BXML;

/**
 * {@link VariableInfo} represents information about a variable in frame.
 */
public class VariableInfo {

    private final String scope, name;
    private BValue bValue;
    private String type, bValueSting;

    public VariableInfo(String name, String scope) {
        this.scope = scope;
        this.name = name;
    }

    public BValue getBValue() {
        return bValue;
    }

    public void setBValue(BValue bValue) {
        this.bValue = bValue;
        if (bValue == null) {
            type = "null";
            bValueSting = "null";
            return;
        }
        type = bValue.getClass().getSimpleName();
        bValueSting = getStringValue(bValue);
    }

    private String getStringValue(BValue bValue) {
        String bValueString = "";
        if (bValue instanceof BValueType || bValue instanceof BXML || bValue instanceof BJSON) {
            bValueString = bValue.stringValue();
        } else if (bValue instanceof BArray) {
            BArray bArray = (BArray) bValue;
            bValueString = "Array[" + bArray.size() + "] {";
            for (int i = 0; i < bArray.size(); i++) {
                bValueString = bValueString + bArray.get(i).stringValue();
                if (i + 1 != bArray.size()) {
                    bValueString = bValueString + ", ";
                }
            }
            bValueString = bValueString + "} ";
        } else if (bValue instanceof BMap) {
            BMap bmap = (BMap) bValue;
            bValueString = "Map[" + ((BMap) bValue).size() + "] {\n\t";
            for (Object key : bmap.keySet()) {
                bValueString = bValueString + key + " : " + getStringValue(bmap.get(key)) + "\n";
            }
            bValueString = bValueString + "} ";
        } else {
            bValueString = "<Complex_Value>";
        }
        return bValueString;
    }

    /**
     * Generates String representation of the BValue.
     *
     * @return Converted String.
     */
    public String getStringRepresntation() {
        return bValueSting;
    }

    @Override
    public String toString() {
        return "(" + scope + ") " + name + " = " + bValueSting + " {" + type + "}";
    }

    public String getScope() {
        return scope;
    }

    public String getName() {
        return name;
    }

    public BValue getbValue() {
        return bValue;
    }

    public String getType() {
        return type;
    }

    public String getbValueSting() {
        return bValueSting;
    }
}
