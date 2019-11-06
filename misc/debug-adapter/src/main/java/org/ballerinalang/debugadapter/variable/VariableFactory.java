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
 * Variable factory.
 */
public class VariableFactory {
    public VariableImpl getVariable(Value value, String parentVarType, String varName) {
        VariableImpl variable = new VariableImpl();
        Variable dapVariable = new Variable();
        variable.setDapVariable(dapVariable);
        dapVariable.setName(varName);

        if (value == null) {
            return null;
        }

        if ("org.ballerinalang.jvm.values.ArrayValue".equalsIgnoreCase(parentVarType)) {
            variable = new BArray(value, dapVariable);
            return variable;
        } else if ("java.lang.Object".equalsIgnoreCase(parentVarType)
                || "org.ballerinalang.jvm.values.MapValue".equalsIgnoreCase(parentVarType)
                || "org.ballerinalang.jvm.values.MapValueImpl".equalsIgnoreCase(parentVarType) // for nested json arrays
        ) {
            // JSONs
            dapVariable.setType("Object");
            if (value.type() == null || value.type().name() == null) {
                dapVariable.setValue("null");
                return variable;
            }
            if ("org.ballerinalang.jvm.values.ArrayValue".equalsIgnoreCase(value.type().name())) {
                // JSON array
                variable = new BArray(value, dapVariable);
                return variable;
            } else if ("java.lang.Long".equalsIgnoreCase(value.type().name())) {
                variable = new BLong(value, dapVariable);
                return variable;
            } else if ("java.lang.Boolean".equalsIgnoreCase(value.type().name())) {
                variable = new BBoolean(value, dapVariable);
                return variable;
            } else if ("java.lang.Double".equalsIgnoreCase(value.type().name())) {
                variable = new BDouble(value, dapVariable);
                return variable;
            } else if ("java.lang.String".equalsIgnoreCase(value.type().name())) {
                // union
                variable = new BString(value, dapVariable);
                return variable;
            } else if ("org.ballerinalang.jvm.values.ErrorValue".equalsIgnoreCase(value.type().name())) {
                variable = new BError(value, dapVariable);
                return variable;
            } else if ("org.ballerinalang.jvm.values.XMLItem".equalsIgnoreCase(value.type().name())) {
                // TODO: support xml values
                dapVariable.setType("xml");
                dapVariable.setValue(value.toString());
                return variable;
            } else {
                variable = new BMapObject(value, dapVariable);
                return variable;
            }
        } else if ("org.ballerinalang.jvm.values.ObjectValue".equalsIgnoreCase(parentVarType)) {
            variable = new BObjectValue(value, dapVariable);
            return variable;
        } else if ("java.lang.Long".equalsIgnoreCase(value.type().name())) {
            variable = new BLong(value, dapVariable);
            return variable;
        } else if ("java.lang.Boolean".equalsIgnoreCase(value.type().name())) {
            variable = new BBoolean(value, dapVariable);
            return variable;
        } else if ("java.lang.Double".equalsIgnoreCase(value.type().name())) {
            variable = new BDouble(value, dapVariable);
            return variable;
        } else if ("java.lang.String".equalsIgnoreCase(parentVarType)) {
            variable = new BString(value, dapVariable);
            return variable;
        } else if (parentVarType.contains("$value$")) {
            variable = new BMapObject(value, dapVariable);
            return variable;
        } else if ("org.ballerinalang.jvm.types.BObjectType".equalsIgnoreCase(parentVarType)) {
            variable = new BObjectType(value, dapVariable);
            return variable;
        } else {
            dapVariable.setType(parentVarType);
            String stringValue = value.toString();
            dapVariable.setValue(stringValue);
            return variable;
        }
    }
}
