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
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.types.BArray;
import org.ballerinalang.debugadapter.variable.types.BBoolean;
import org.ballerinalang.debugadapter.variable.types.BDecimal;
import org.ballerinalang.debugadapter.variable.types.BError;
import org.ballerinalang.debugadapter.variable.types.BFloat;
import org.ballerinalang.debugadapter.variable.types.BFunction;
import org.ballerinalang.debugadapter.variable.types.BFuture;
import org.ballerinalang.debugadapter.variable.types.BHandle;
import org.ballerinalang.debugadapter.variable.types.BInt;
import org.ballerinalang.debugadapter.variable.types.BJson;
import org.ballerinalang.debugadapter.variable.types.BMap;
import org.ballerinalang.debugadapter.variable.types.BNil;
import org.ballerinalang.debugadapter.variable.types.BObject;
import org.ballerinalang.debugadapter.variable.types.BRecord;
import org.ballerinalang.debugadapter.variable.types.BService;
import org.ballerinalang.debugadapter.variable.types.BStream;
import org.ballerinalang.debugadapter.variable.types.BString;
import org.ballerinalang.debugadapter.variable.types.BTable;
import org.ballerinalang.debugadapter.variable.types.BTuple;
import org.ballerinalang.debugadapter.variable.types.BTypeDesc;
import org.ballerinalang.debugadapter.variable.types.BUnknown;
import org.ballerinalang.debugadapter.variable.types.BXmlComment;
import org.ballerinalang.debugadapter.variable.types.BXmlItem;
import org.ballerinalang.debugadapter.variable.types.BXmlItemAttributeMap;
import org.ballerinalang.debugadapter.variable.types.BXmlPi;
import org.ballerinalang.debugadapter.variable.types.BXmlSequence;
import org.ballerinalang.debugadapter.variable.types.BXmlText;

import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.STRAND_VAR_NAME;
import static org.ballerinalang.debugadapter.variable.VariableUtils.isJson;
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
 * <li> boolean - true, false
 * <li> int - 64-bit signed integers
 * <li> float - 64-bit IEEE 754-2008 binary floating point numbers
 * <li> decimal - decimal floating point numbers
 * <li> string - a sequence of Unicode scalar values
 * <li> array - an ordered list of values, optionally with a specific length, where a single type is specified for all
 * members of the list
 * <li> tuple - an ordered list of values, where a type is specified separately for each member of the list
 * <li> map - a mapping from keys, which are strings, to values; specifies mappings in terms of a single type to which
 * all keys are mapped
 * <li> record - a mapping from keys, which are strings, to values; specifies maps in terms of names of fields
 * (required keys) and value for each field
 * <li> object - a combination of named fields and named methods
 * <li> json - the union of (), int, float, decimal, string, and maps and arrays whose values are, recursively, json
 * <li> XML - a sequence of zero or more elements, processing instructions, comments or text items
 * <li> error - an indication that there has been an error, with a string identifying the reason for the error, and a
 * mapping giving additional details about the error
 * <li> future - a value to be returned by a function execution
 * <li> handle - reference to externally managed storage
 * <li> typedesc - a type descriptor
 * <li> function - a function with 0 or more specified parameter types and a single return type
 * <li> service - a collection of named methods, including resource methods
 * <li> any - any value other than an error // Todo - show runtime type or "any"?
 * <li> anydata - not an error and does not contain behavioral members // Todo - show runtime type or "anydata"?
 * <li> union - the union of the component types // Todo - show runtime type or union type?
 * <li> optional - the underlying type and () // Todo - show runtime type or optional type?
 * <li> byte - int in the range 0 to 255 inclusive // Todo - show runtime type(int) or "byte"?
 * <li> singleton - a single value described by a literal. // Todo - show runtime type?
 * <li> table(Preview) - a two-dimensional collection of immutable values // Todo - show entries
 * <li> stream(Preview) - a sequence of values that can be generated lazily // Todo - show values
 * </ul>
 * <br>
 * To be implemented
 * <ul>
 * <li> never - no value
 * </ul>
 */
public class VariableFactory {

    public static BVariable getVariable(SuspendedContext context, Value value) {
        return getVariable(context, "unknown", value);
    }

    /**
     * Returns the corresponding BType variable instance for a given java variable.
     *
     * @param context suspended context
     * @param varName variable name
     * @param value   jdi value instance of the java variable
     * @return Ballerina type variable instance which corresponds to the given java variable
     */
    public static BVariable getVariable(SuspendedContext context, String varName, Value value) {

        if (varName == null || varName.isEmpty() || varName.startsWith("$") || varName.equals(STRAND_VAR_NAME)) {
            return null;
        } else if (value == null) {
            return new BNil(context, varName, null);
        }

        Type valueType = value.type();
        String valueTypeName = valueType.name();
        if (valueTypeName.equals(JVMValueType.INT.getString())
                || valueTypeName.equals(JVMValueType.J_INT.getString())
                || valueTypeName.equals(JVMValueType.LONG.getString())
                || valueTypeName.equals(JVMValueType.J_LONG.getString())) {
            return new BInt(context, varName, value);
        } else if (valueTypeName.equals(JVMValueType.BOOLEAN.getString())
                || valueTypeName.equals(JVMValueType.J_BOOLEAN.getString())) {
            return new BBoolean(context, varName, value);
        } else if (valueTypeName.equals(JVMValueType.DOUBLE.getString())
                || valueTypeName.equals(JVMValueType.J_DOUBLE.getString())) {
            return new BFloat(context, varName, value);
        } else if (valueTypeName.equals(JVMValueType.DECIMAL.getString())) {
            return new BDecimal(context, varName, value);
        } else if (valueTypeName.equals(JVMValueType.BMPSTRING.getString())
                || valueTypeName.equals(JVMValueType.NONBMPSTRING.getString())
                || valueTypeName.equals(JVMValueType.J_STRING.getString())) {
            return new BString(context, varName, value);
        } else if (valueTypeName.contains(JVMValueType.ARRAY_VALUE.getString())) {
            return new BArray(context, varName, value);
        } else if (valueTypeName.contains(JVMValueType.TUPLE_VALUE.getString())) {
            return new BTuple(context, varName, value);
        } else if (valueTypeName.contains(JVMValueType.ERROR_VALUE.getString())) {
            return new BError(context, varName, value);
        } else if (valueTypeName.contains(JVMValueType.TYPEDESC_VALUE.getString())) {
            return new BTypeDesc(context, varName, value);
        } else if (valueTypeName.contains(JVMValueType.TABLE_VALUE.getString())) {
            return new BTable(context, varName, value);
        } else if (valueTypeName.equals(JVMValueType.FP_VALUE.getString())) {
            return new BFunction(context, varName, value);
        } else if (valueTypeName.equals(JVMValueType.FUTURE_VALUE.getString())) {
            return new BFuture(context, varName, value);
        } else if (valueTypeName.equals(JVMValueType.HANDLE_VALUE.getString())) {
            return new BHandle(context, varName, value);
        } else if (valueTypeName.equals(JVMValueType.STREAM_VALUE.getString())) {
            return new BStream(context, varName, value);
        } else if (valueTypeName.equals(JVMValueType.XML_TEXT.getString())) {
            return new BXmlText(context, varName, value);
        } else if (valueTypeName.equals(JVMValueType.XML_COMMENT.getString())) {
            return new BXmlComment(context, varName, value);
        } else if (valueTypeName.equals(JVMValueType.XML_PI.getString())) {
            return new BXmlPi(context, varName, value);
        } else if (valueTypeName.equals(JVMValueType.XML_SEQUENCE.getString())) {
            return new BXmlSequence(context, varName, value);
        } else if (valueTypeName.equals(JVMValueType.XML_ITEM.getString())) {
            return new BXmlItem(context, varName, value);
        } else if (valueTypeName.equals(JVMValueType.XML_ATTRIB_MAP.getString())) {
            return new BXmlItemAttributeMap(context, varName, value);
        } else if (valueTypeName.contains(JVMValueType.ANON_SERVICE.getString())) {
            return new BService(context, varName, value);
        } else if (valueTypeName.contains(JVMValueType.MAP_VALUE.getString())) {
            if (isJson(value)) {
                return new BJson(context, varName, value);
            } else {
                return new BMap(context, varName, value);
            }
        } else if (value instanceof ObjectReference) {
            if (isObject(value)) {
                return new BObject(context, varName, value);
            } else if (isRecord(value)) {
                return new BRecord(context, varName, value);
            }
        }
        // If the variable doesn't match any of the above types, returns as a variable with type "unknown".
        return new BUnknown(context, varName, value);
    }
}
