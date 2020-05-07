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
import org.ballerinalang.debugadapter.variable.types.BDouble;
import org.ballerinalang.debugadapter.variable.types.BError;
import org.ballerinalang.debugadapter.variable.types.BLong;
import org.ballerinalang.debugadapter.variable.types.BMapObject;
import org.ballerinalang.debugadapter.variable.types.BObjectType;
import org.ballerinalang.debugadapter.variable.types.BObjectValue;
import org.ballerinalang.debugadapter.variable.types.BString;
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

        if (value == null || parentTypeName == null || varName == null || parentTypeName.isEmpty()) {
            return null;
        }

        Type valueType = value.type();
        String valueTypeName = valueType.name();
        Variable dapVariable = new Variable();
        dapVariable.setName(varName);

        if (valueTypeName.equalsIgnoreCase(JVMValueType.LONG.toString())) {
            return new BLong(value, dapVariable);
        } else if (valueTypeName.equalsIgnoreCase(JVMValueType.BOOLEAN.getString())) {
            return new BBoolean(value, dapVariable);
        } else if (valueTypeName.equalsIgnoreCase(JVMValueType.DOUBLE.getString())) {
            return new BDouble(value, dapVariable);
        } else if (parentTypeName.equalsIgnoreCase(JVMValueType.STRING.getString())) {
            return new BString(value, dapVariable);
        } else if (parentTypeName.contains("$value$")) {
            return new BMapObject(value, dapVariable);
        } else if (parentTypeName.equalsIgnoreCase(JVMValueType.OBJECT_TYPE.getString())) {
            return new BObjectType(value, dapVariable);
        } else if (parentTypeName.equalsIgnoreCase(JVMValueType.OBJECT_VALUE.getString())) {
            return new BObjectValue(value, dapVariable);
        } else if (parentTypeName.contains(JVMValueType.ARRAY_VALUE.getString())) {
            return new BArray(value, dapVariable);
        } else if (parentTypeName.contains(JVMValueType.OBJECT.getString())
                || parentTypeName.contains(JVMValueType.MAP_VALUE.getString())) {
            dapVariable.setType("object");
            if (valueType == null) {
                dapVariable.setValue("null");
                return new BVariable(dapVariable);
            } else if (valueTypeName.equalsIgnoreCase(JVMValueType.ARRAY_VALUE.getString())) {
                return new BArray(value, dapVariable);
            } else if (valueTypeName.equalsIgnoreCase(JVMValueType.LONG.toString())) {
                return new BLong(value, dapVariable);
            } else if (valueTypeName.equalsIgnoreCase(JVMValueType.BOOLEAN.toString())) {
                return new BBoolean(value, dapVariable);
            } else if (valueTypeName.equalsIgnoreCase(JVMValueType.DOUBLE.toString())) {
                return new BDouble(value, dapVariable);
            } else if (valueTypeName.equalsIgnoreCase(JVMValueType.STRING.toString())) {
                return new BString(value, dapVariable);
            } else if (valueTypeName.equalsIgnoreCase(JVMValueType.ERROR_VALUE.toString())) {
                return new BError(value, dapVariable);
            } else if (valueTypeName.equalsIgnoreCase(JVMValueType.XML_ITEM.toString())) {
                // TODO: support xml values
                dapVariable.setType("xml");
                dapVariable.setValue(value.toString());
                return new BVariable(dapVariable);
            } else {
                return new BMapObject(value, dapVariable);
            }
        } else {
            dapVariable.setType(parentTypeName);
            String stringValue = value.toString();
            dapVariable.setType("unknown");
            dapVariable.setValue(stringValue);
            return new BVariable(dapVariable);
        }
    }
}
