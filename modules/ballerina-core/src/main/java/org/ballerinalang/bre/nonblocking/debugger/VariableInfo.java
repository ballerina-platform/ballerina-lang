/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*/
package org.ballerinalang.bre.nonblocking.debugger;

import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BStruct;
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
        } else if (bValue instanceof BNewArray) {
            BNewArray bArray = (BNewArray) bValue;
            bValueString = "Array[" + bArray.size() + "] ";
            bValueString = bValueString + bArray.stringValue();
        } else if (bValue instanceof BMap) {
            BMap bmap = (BMap) bValue;
            bValueString = "Map[" + bmap.size() + "] ";
            bValueString = bValueString + bmap.stringValue();
        } else if (bValue instanceof BStruct) {
            BStruct bStruct = (BStruct) bValue;
            bValueString = "struct " + bStruct.getType().getName() + " ";
            bValueString = bValueString + bStruct.stringValue();
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
