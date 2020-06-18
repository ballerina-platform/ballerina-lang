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

    public static final String UNKNOWN_VALUE = "unknown";

    /**
     * Returns the type of a given ballerina array typed variable.
     *
     * @param arrayRef object reference of the array instance.
     * @return type of the array.
     */
    public static String getArrayType(ObjectReference arrayRef) {
        List<Field> fields = arrayRef.referenceType().allFields();
        String arrayValueFiled = arrayRef.getValues(fields).entrySet().stream().filter(fieldValueEntry ->
                fieldValueEntry.getValue() != null && fieldValueEntry.getKey().toString().endsWith("Values"))
                .map(Map.Entry::getKey).collect(Collectors.toList()).get(0).toString();
        String arrayType = arrayValueFiled.substring(arrayValueFiled.lastIndexOf(".") + 1).replace("Values", "");
        return arrayType.equals("ref") ? "any" : arrayType;
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
     * Returns type of a given ballerina backend jvm variable instance.
     *
     * @param value jdi value instance of the ballerina jvm variable.
     * @return variable type in string form.
     */
    static String getBType(Value value) {
        try {
            ObjectReference mapRef = (ObjectReference) value;
            Field mapTypeField = mapRef.referenceType().fieldByName("type");
            String mapTypeName = mapRef.getValue(mapTypeField).type().name();
            return mapTypeName.substring(mapTypeName.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }
}
