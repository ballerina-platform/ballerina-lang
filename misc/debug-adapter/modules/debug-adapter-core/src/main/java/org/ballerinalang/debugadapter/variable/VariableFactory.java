/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.variable;

import com.sun.jdi.Type;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.variable.types.BArray;
import org.ballerinalang.debugadapter.variable.types.BBoolean;
import org.ballerinalang.debugadapter.variable.types.BDecimal;
import org.ballerinalang.debugadapter.variable.types.BError;
import org.ballerinalang.debugadapter.variable.types.BFloat;
import org.ballerinalang.debugadapter.variable.types.BInt;
import org.ballerinalang.debugadapter.variable.types.BJson;
import org.ballerinalang.debugadapter.variable.types.BMap;
import org.ballerinalang.debugadapter.variable.types.BNil;
import org.ballerinalang.debugadapter.variable.types.BObjectType;
import org.ballerinalang.debugadapter.variable.types.BObjectValue;
import org.ballerinalang.debugadapter.variable.types.BRecord;
import org.ballerinalang.debugadapter.variable.types.BString;
import org.ballerinalang.debugadapter.variable.types.BTuple;
import org.ballerinalang.debugadapter.variable.types.BUnknown;
import org.ballerinalang.debugadapter.variable.types.BXmlItem;
import org.eclipse.lsp4j.debug.Variable;

/**
 * Factory implementation for ballerina variable types.
 */
public class VariableFactory {

    /**
     * Returns the corresponding BType variable instance for a given java variable.
     *
     * @param value          jdi value instance of the java variable
     * @param parentTypeName variable type of the java parent variable
     * @param varName        variable name
     * @return Ballerina type variable instance which corresponds to the given java variable
     */
    public static BVariable getVariable(Value value, String parentTypeName, String varName) {

        if (parentTypeName == null || parentTypeName.isEmpty() || varName == null || varName.isEmpty()
                || varName.startsWith("$")) {
            return null;
        }

        Variable dapVariable = new Variable();
        dapVariable.setName(varName);
        if (value == null) {
            // variables of "nil" type.
            if (parentTypeName.equals(JVMValueType.J_OBJECT.getString())) {
                return new BNil(dapVariable);
            } else {
                return null;
            }
        }

        Type valueType = value.type();
        String valueTypeName = valueType.name();
        if (valueTypeName.equals(JVMValueType.LONG.getString())
                || valueTypeName.equals(JVMValueType.J_LONG.getString())) {
            return new BInt(value, dapVariable);
        } else if (valueTypeName.equals(JVMValueType.BOOLEAN.getString())
                || valueTypeName.equals(JVMValueType.J_BOOLEAN.getString())) {
            return new BBoolean(value, dapVariable);
        } else if (valueTypeName.equals(JVMValueType.DOUBLE.getString())
                || valueTypeName.equals(JVMValueType.J_DOUBLE.getString())) {
            return new BFloat(value, dapVariable);
        } else if (parentTypeName.equals(JVMValueType.DECIMAL.getString())) {
            return new BDecimal(value, dapVariable);
        } else if (parentTypeName.equals(JVMValueType.J_STRING.getString())) {
            return new BString(value, dapVariable);
        } else if (parentTypeName.equals(JVMValueType.OBJECT_TYPE.getString())) {
            return new BObjectType(value, dapVariable);
        } else if (parentTypeName.equals(JVMValueType.OBJECT_VALUE.getString())) {
            return new BObjectValue(value, dapVariable);
        } else if (valueTypeName.contains(JVMValueType.ARRAY_VALUE.getString())) {
            return new BArray(value, dapVariable);
        } else if (valueTypeName.contains(JVMValueType.TUPLE_VALUE.getString())) {
            return new BTuple(value, dapVariable);
        } else if (parentTypeName.equals(JVMValueType.J_OBJECT.getString())
                && valueTypeName.contains(JVMValueType.MAP_VALUE.getString())) {
            return new BJson(value, dapVariable);
        } else if (parentTypeName.contains(JVMValueType.MAP_VALUE.getString())
                && valueTypeName.contains(JVMValueType.MAP_VALUE.getString())) {
            return new BMap(value, dapVariable);
        } else if (parentTypeName.contains(JVMValueType.MAP_VALUE.getString())
                || (parentTypeName.contains("$value$") && valueTypeName.contains("$value$"))) {
            return new BRecord(value, dapVariable);
        } else if (valueTypeName.contains(JVMValueType.ERROR_VALUE.getString())) {
            return new BError(value, dapVariable);
        } else if (valueTypeName.contains(JVMValueType.XML_ITEM.getString())) {
            return new BXmlItem(value, dapVariable);
        }

        // If the variable doesn't match any of the above types, returns as a variable with type "unknown".
        dapVariable.setType(parentTypeName);
        return new BUnknown(value, dapVariable);
    }
}
