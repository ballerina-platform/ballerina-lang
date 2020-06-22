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
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

import java.util.Optional;

/**
 * JDI-based debug variable implementation related utilities.
 */
public class VariableUtils {

    // Used to trim redundant beginning and ending double quotes from a string, if presents.
    private static final String ADDITIONAL_QUOTES_REMOVE_REGEX = "^\"|\"$";
    public static final String UNKNOWN_VALUE = "unknown";

    /**
     * Returns the corresponding ballerina variable type of a given ballerina backend jvm variable instance.
     *
     * @param value jdi value instance of the ballerina jvm variable.
     * @return variable type in string form.
     */
    public static String getBType(Value value) {
        try {
            ObjectReference valueRef = (ObjectReference) value;
            Field bTypeField = valueRef.referenceType().fieldByName("type");
            Value bTypeRef = valueRef.getValue(bTypeField);
            Field typeNameField = ((ObjectReference) bTypeRef).referenceType().fieldByName("typeName");
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
                return stringRef.toString().replaceAll(ADDITIONAL_QUOTES_REMOVE_REGEX, "");
            }
            Optional<Value> valueField = getFieldValue(stringRef, "value");
            // Additional filtering is required, as some ballerina variable type names may contain redundant
            // double quotes.
            return valueField.map(value -> value.toString().replaceAll(ADDITIONAL_QUOTES_REMOVE_REGEX, ""))
                    .orElse(UNKNOWN_VALUE);
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    /**
     * Verifies whether a given JDI value is a ballerina object instance.
     *
     * @param value JDI value instance.
     * @return true the given JDI value is a ballerina object instance.
     */
    static boolean isObject(Value value) {
        Optional<Value> valueType = getFieldValue(value, "type");
        return valueType.map(type -> type.type().name().endsWith(JVMValueType.BTYPE_OBJECT.getString())).orElse(false);
    }

    /**
     * Verifies whether a given JDI value is a ballerina record instance.
     *
     * @param value JDI value instance.
     * @return true the given JDI value is a ballerina record instance.
     */
    static boolean isRecord(Value value) {
        Optional<Value> valueType = getFieldValue(value, "type");
        return valueType.map(type -> type.type().name().endsWith(JVMValueType.BTYPE_RECORD.getString())).orElse(false);
    }

    /**
     * Returns the JDI value of a given field, for a given JDI object reference (class instance).
     *
     * @param parent    parent JDI value instance.
     * @param fieldName field name
     * @return JDI value of a given field, for a given JDI object reference (class instance).
     */
    public static Optional<Value> getFieldValue(Value parent, String fieldName) {
        if (!(parent instanceof ObjectReference)) {
            return Optional.empty();
        }
        ObjectReference parentRef = (ObjectReference) parent;
        Field field = parentRef.referenceType().fieldByName(fieldName);
        if (field == null) {
            return Optional.empty();
        }
        return Optional.of(parentRef.getValue(field));
    }
}
