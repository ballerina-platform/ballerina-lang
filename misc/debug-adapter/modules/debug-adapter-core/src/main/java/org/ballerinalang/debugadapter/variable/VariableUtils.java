/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import com.sun.jdi.Field;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * JDI-based debug variable implementation related utilities.
 */
public class VariableUtils {

    public static final String FIELD_TYPE = "type";
    public static final String FIELD_TYPENAME = "typeName";
    public static final String FIELD_VALUE = "value";
    public static final String FIELD_CONSTRAINT = "constraint";
    public static final String METHOD_STRINGVALUE = "stringValue";
    public static final String UNKNOWN_VALUE = "unknown";
    // Used to trim redundant beginning and ending double quotes from a string, if presents.
    private static final String ADDITIONAL_QUOTES_REMOVE_REGEX = "^\"|\"$";

    /**
     * Returns the corresponding ballerina variable type of a given ballerina backend jvm variable instance.
     *
     * @param value jdi value instance of the ballerina jvm variable.
     * @return variable type in string form.
     */
    public static String getBType(Value value) {
        try {
            ObjectReference valueRef = (ObjectReference) value;
            Field bTypeField = valueRef.referenceType().fieldByName(FIELD_TYPE);
            Value bTypeRef = valueRef.getValue(bTypeField);
            Field typeNameField = ((ObjectReference) bTypeRef).referenceType().fieldByName(FIELD_TYPENAME);
            Value typeNameRef = ((ObjectReference) bTypeRef).getValue(typeNameField);
            return getStringFrom(typeNameRef);
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    /**
     * Returns the actual string value from ballerina jvm types for strings.
     *
     * @param stringValue JDI value of the string instance
     * @return actual string.
     */
    public static String getStringFrom(Value stringValue) {
        try {
            if (!(stringValue instanceof ObjectReference)) {
                return UNKNOWN_VALUE;
            }
            ObjectReference stringRef = (ObjectReference) stringValue;
            if (!stringRef.referenceType().name().equals(JVMValueType.BMPSTRING.getString())
                    && !stringRef.referenceType().name().equals(JVMValueType.NONBMPSTRING.getString())) {
                // Additional filtering is required, as some ballerina variable type names may contain redundant
                // double quotes.
                return removeRedundantQuotes(stringRef.toString());
            }
            Optional<Value> valueField = getFieldValue(stringRef, FIELD_VALUE);
            // Additional filtering is required, as some ballerina variable type names may contain redundant
            // double quotes.
            return valueField.map(value -> removeRedundantQuotes(value.toString())).orElse(UNKNOWN_VALUE);
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    /**
     * Invokes "stringValue()" method of the given ballerina jvm variable instance and returns the result as a string.
     *
     * @param context   variable debug context.
     * @param jvmObject ballerina jvm variable instance.
     * @return result of the method invocation as a string.
     */
    public static String getStringValue(SuspendedContext context, Value jvmObject) {
        try {
            Optional<Method> method = VariableUtils.getMethod(jvmObject, METHOD_STRINGVALUE);
            if (method.isPresent()) {
                Value stringValue = ((ObjectReference) jvmObject).invokeMethod(context.getOwningThread()
                                .getThreadReference(), method.get(), Collections.singletonList(null),
                        ObjectReference.INVOKE_SINGLE_THREADED);
                return VariableUtils.getStringFrom(stringValue);
            }
            return UNKNOWN_VALUE;
        } catch (Exception ignored) {
            return UNKNOWN_VALUE;
        }
    }

    /**
     * Verifies whether a given JDI value is a ballerina object variable instance.
     *
     * @param value JDI value instance.
     * @return true the given JDI value is a ballerina object variable instance.
     */
    static boolean isObject(Value value) {
        try {
            return getFieldValue(value, FIELD_TYPE).map(type -> type.type().name().endsWith
                    (JVMValueType.BTYPE_OBJECT.getString())).orElse(false);
        } catch (DebugVariableException e) {
            return false;
        }
    }

    /**
     * Verifies whether a given JDI value is a ballerina record variable instance.
     *
     * @param value JDI value instance.
     * @return true the given JDI value is a ballerina record variable instance.
     */
    static boolean isRecord(Value value) {
        try {
            return getFieldValue(value, FIELD_TYPE).map(type -> type.type().name().endsWith
                    (JVMValueType.BTYPE_RECORD.getString())).orElse(false);
        } catch (DebugVariableException e) {
            return false;
        }
    }

    /**
     * Verifies whether a given JDI value is a ballerina JSON variable instance.
     *
     * @param value JDI value instance.
     * @return true the given JDI value is a ballerina JSON variable instance.
     */
    static boolean isJson(Value value) {
        try {
            Optional<Value> typeField = getFieldValue(value, FIELD_TYPE);
            if (!typeField.isPresent()) {
                return false;
            }
            if (typeField.get().type().name().endsWith(JVMValueType.BTYPE_JSON.getString())) {
                return true;
            }
            Optional<Value> constraint = getFieldValue(typeField.get(), FIELD_CONSTRAINT);
            return constraint.map(val -> val.type().name().endsWith(JVMValueType.BTYPE_JSON.getString())).orElse(false);
        } catch (DebugVariableException e) {
            return false;
        }
    }

    /**
     * Returns the JDI value of a given field, for a given JDI object reference (class instance).
     *
     * @param parent    parent JDI value instance.
     * @param fieldName field name
     * @return JDI value of a given field, for a given JDI object reference (class instance).
     */
    public static Optional<Value> getFieldValue(Value parent, String fieldName) throws DebugVariableException {
        if (!(parent instanceof ObjectReference)) {
            return Optional.empty();
        }
        ObjectReference parentRef = (ObjectReference) parent;
        Field field = parentRef.referenceType().fieldByName(fieldName);
        if (field == null) {
            throw new DebugVariableException(
                    String.format("No fields found with name: \"%s\", in %s", fieldName, parent.toString()));
        }
        return Optional.ofNullable(parentRef.getValue(field));
    }

    /**
     * Returns a JDI method instance of a any given method which exists in the given JDI object reference.
     *
     * @param parent     parent JDI value instance.
     * @param methodName method name.
     * @return the JDI method instance of a any given method which exists in the given JDI object reference.
     */
    public static Optional<Method> getMethod(Value parent, String methodName) throws DebugVariableException {
        if (!(parent instanceof ObjectReference)) {
            return Optional.empty();
        }
        ObjectReference parentRef = (ObjectReference) parent;
        List<Method> methods = parentRef.referenceType().methodsByName(methodName);
        if (methods.isEmpty()) {
            throw new DebugVariableException(String.format("No methods found for name: \"%s\", in %s",
                    methodName, parent.toString()));
        } else if (methods.size() > 1) {
            throw new DebugVariableException(String.format("Overloaded methods found for name: \"%s\", in %s",
                    methodName, parent.toString()));
        }
        return Optional.of(methods.get(0));
    }

    public static String removeRedundantQuotes(String str) {
        do {
            str = str.replaceAll(ADDITIONAL_QUOTES_REMOVE_REGEX, "");
        } while (str.startsWith("\"") || str.endsWith("\""));
        return str;
    }
}
