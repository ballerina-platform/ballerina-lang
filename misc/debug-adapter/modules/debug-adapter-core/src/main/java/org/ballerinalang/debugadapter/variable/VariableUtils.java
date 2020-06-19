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
import com.sun.jdi.IntegerValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JDI-based debug variable implementation related utilities.
 */
public class VariableUtils {

    // Used to trim redundant beginning and ending double quotes from a string, if presents.
    private static final String ADDITIONAL_QUOTES_REMOVE_REGEX = "^\"|\"$";
    public static final String UNKNOWN_VALUE = "unknown";

    /**
     * Returns the type of a given ballerina array typed variable.
     *
     * @param arrayRef object reference of the array instance.
     * @return type of the array.
     */
    public static String getArrayType(ObjectReference arrayRef) {
        Field bTypeField = arrayRef.referenceType().fieldByName("elementType");
        Value bTypeRef = arrayRef.getValue(bTypeField);
        Field typeNameField = ((ObjectReference) bTypeRef).referenceType().fieldByName("typeName");
        Value typeNameRef = ((ObjectReference) bTypeRef).getValue(typeNameField);
        return getStringFrom(typeNameRef);
    }

    /**
     * Returns the size/length of a given ballerina array typed variable.
     *
     * @param arrayRef object reference of the array instance.
     * @return size of the array.
     */
    public static int getArraySize(ObjectReference arrayRef) {
        List<Field> fields = arrayRef.referenceType().allFields();
        Field arraySizeField = arrayRef.getValues(fields).entrySet().stream().filter(fieldValueEntry ->
                fieldValueEntry.getValue() != null && fieldValueEntry.getKey().toString().endsWith("ArrayValue.size"))
                .map(Map.Entry::getKey).collect(Collectors.toList()).get(0);
        return ((IntegerValue) arrayRef.getValue(arraySizeField)).value();
    }

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
            Field valueField = stringRef.referenceType().fieldByName("value");
            if (valueField != null) {
                // Additional filtering is required, as some ballerina variable type names may contain redundant
                // double quotes.
                return stringRef.getValue(valueField).toString().replaceAll(ADDITIONAL_QUOTES_REMOVE_REGEX, "");
            }
            return UNKNOWN_VALUE;
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
        ObjectReference valueRef = (ObjectReference) value;
        Field typeField = valueRef.referenceType().fieldByName("type");
        String typeName = valueRef.getValue(typeField).type().name();
        return typeName.endsWith(JVMValueType.BTYPE_OBJECT.getString());
    }

    /**
     * Verifies whether a given JDI value is a ballerina record instance.
     *
     * @param value JDI value instance.
     * @return true the given JDI value is a ballerina record instance.
     */
    static boolean isRecord(Value value) {
        ObjectReference valueRef = (ObjectReference) value;
        Field typeField = valueRef.referenceType().fieldByName("type");
        String typeName = valueRef.getValue(typeField).type().name();
        return typeName.endsWith(JVMValueType.BTYPE_RECORD.getString());
    }
}
