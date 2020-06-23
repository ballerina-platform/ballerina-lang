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

import com.sun.jdi.ObjectReference;
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
import org.ballerinalang.debugadapter.variable.types.BObject;
import org.ballerinalang.debugadapter.variable.types.BRecord;
import org.ballerinalang.debugadapter.variable.types.BString;
import org.ballerinalang.debugadapter.variable.types.BTuple;
import org.ballerinalang.debugadapter.variable.types.BUnknown;
import org.ballerinalang.debugadapter.variable.types.BXmlItem;
import org.eclipse.lsp4j.debug.Variable;

import static org.ballerinalang.debugadapter.variable.VariableUtils.getBType;
import static org.ballerinalang.debugadapter.variable.VariableUtils.isObject;
import static org.ballerinalang.debugadapter.variable.VariableUtils.isRecord;

/**
 * Factory implementation of ballerina debug variable types.
 * <br><br>
 * Language Specification Version - 2020R1.
 * <br><br>
 * Supported Types
 * <ul>
 * <li> nil
 * <li> boolean -	true, false
 * <li> int	- 64-bit signed integers
 * <li> float - 64-bit IEEE 754-2008 binary floating point numbers
 * <li> decimal - decimal floating point numbers
 * <li> string - a sequence of Unicode scalar values
 * <li> array - an ordered list of values, optionally with a specific length, where a single type is specified for all
 * members of the list
 * <li> tuple - an ordered list of values, where a type is specified separately for each member of the list
 * <li> map	- a mapping from keys, which are strings, to values; specifies mappings in terms of a single type to which
 * all keys are mapped
 * <li> record - a mapping from keys, which are strings, to values; specifies maps in terms of names of fields
 * (required keys) and value for each field
 * <li> object - a combination of named fields and named methods
 * <li> json - the union of (), int, float, decimal, string, and maps and arrays whose values are, recursively, json
 * <li> error - an indication that there has been an error, with a string identifying the reason for the error, and a
 * mapping giving additional details about the error
 * <li> any - any value other than an error         // Todo - show runtime type or "any"?
 * <li> union - the union of the component types    // Todo - show runtime type or union type?
 * <li> optional - the underlying type and ()       // Todo - show runtime type or optional type?
 * <li> byte - int in the range 0 to 255 inclusive  // Todo - show runtime type(int) or "byte"?
 * </ul>
 * <br>
 * To be implemented
 * <ul>
 * <li> XML - a sequence of zero or more elements, processing instructions, comments or text items
 * <li> table - a two-dimensional collection of immutable values
 * <li> function - a function with 0 or more specified parameter types and a single return type
 * <li> future - a value to be returned by a function execution
 * <li> service	- a collection of named methods, including resource methods
 * <li> typedesc - a type descriptor
 * <li> handle - reference to externally managed storage
 * <li> stream - a sequence of values that can be generated lazily
 * <li> singleton - a single value described by a literal
 * <li> anydata	- not an error and does not contain behavioral members at any depth
 * <li> never - no value
 * </ul>
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
    public static BVariable getVariable(VariableContext context, Value value, String parentTypeName, String varName) {

        if (varName == null || varName.isEmpty() || varName.startsWith("$")) {
            return null;
        }
        Variable dapVariable = new Variable();
        dapVariable.setName(varName);
        if (value == null) {
            return new BNil(context, null, dapVariable);
        }

        Type valueType = value.type();
        String valueTypeName = valueType.name();
        if (valueTypeName.equals(JVMValueType.INT.getString())
                || valueTypeName.equals(JVMValueType.J_INT.getString())
                || valueTypeName.equals(JVMValueType.LONG.getString())
                || valueTypeName.equals(JVMValueType.J_LONG.getString())) {
            return new BInt(context, value, dapVariable);
        } else if (valueTypeName.equals(JVMValueType.BOOLEAN.getString())
                || valueTypeName.equals(JVMValueType.J_BOOLEAN.getString())) {
            return new BBoolean(context, value, dapVariable);
        } else if (valueTypeName.equals(JVMValueType.DOUBLE.getString())
                || valueTypeName.equals(JVMValueType.J_DOUBLE.getString())) {
            return new BFloat(context, value, dapVariable);
        } else if (valueTypeName.equals(JVMValueType.DECIMAL.getString())) {
            return new BDecimal(context, value, dapVariable);
        } else if (valueTypeName.equals(JVMValueType.BMPSTRING.getString())
                || valueTypeName.equals(JVMValueType.NONBMPSTRING.getString())
                || valueTypeName.equals(JVMValueType.J_STRING.getString())) {
            return new BString(context, value, dapVariable);
        } else if (valueTypeName.contains(JVMValueType.ARRAY_VALUE.getString())) {
            return new BArray(context, value, dapVariable);
        } else if (valueTypeName.contains(JVMValueType.TUPLE_VALUE.getString())) {
            return new BTuple(context, value, dapVariable);
        } else if (valueTypeName.contains(JVMValueType.ERROR_VALUE.getString())) {
            return new BError(context, value, dapVariable);
        } else if (valueTypeName.contains(JVMValueType.XML_ITEM.getString())) {
            return new BXmlItem(context, value, dapVariable);
        } else if (valueTypeName.contains(JVMValueType.MAP_VALUE.getString())) {
            // Todo - Remove checks on parentTypeName, after backend is fixed to contain correct BTypes for JSON
            //  variables.
            String bType = getBType(value);
            if (bType.equals(BVariableType.JSON.getString())
                    || parentTypeName.equals(JVMValueType.J_OBJECT.getString())) {
                return new BJson(context, value, dapVariable);
            } else if (bType.equals(BVariableType.MAP.getString())) {
                return new BMap(context, value, dapVariable);
            }
        } else if (value instanceof ObjectReference) {
            if (isObject(value)) {
                return new BObject(context, value, dapVariable);
            } else if (isRecord(value)) {
                return new BRecord(context, value, dapVariable);
            }
        }
        // If the variable doesn't match any of the above types, returns as a variable with type "unknown".
        dapVariable.setType(valueTypeName);
        return new BUnknown(context, value, dapVariable);
    }
}
