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

package org.ballerinalang.debugadapter.utils;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Field;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.Value;
import com.sun.tools.jdi.ObjectReferenceImpl;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Utils for jBallerina debugger variable implementation.
 */
public class VariableUtils {

    /**
     * Extracts all the child variable values for a given array type variable, using its object reference.
     *
     * @param variable object reference of the parent variable.
     * @return a map containing child values against their index.
     */
    public static Map<String, Value> getChildVariables(ObjectReferenceImpl variable) {

        List<Field> fields = variable.referenceType().allFields();

        Field arrayValueField = variable.getValues(fields).entrySet().stream().filter(fieldValueEntry ->
                fieldValueEntry.getValue() != null && fieldValueEntry.getKey().toString().endsWith("Values"))
                .map(Map.Entry::getKey).collect(Collectors.toList()).get(0);

        Field arraySizeField = variable.getValues(fields).entrySet().stream().filter(fieldValueEntry ->
                fieldValueEntry.getValue() != null && fieldValueEntry.getKey().toString().endsWith("ArrayValue.size"))
                .map(Map.Entry::getKey).collect(Collectors.toList()).get(0);

        int arraySize = ((IntegerValue) variable.getValue(arraySizeField)).value();
        List<Value> valueList = ((ArrayReference) variable.getValue(arrayValueField)).getValues();

        // List length is 100 by default. Create a sub list with actual array size
        List<Value> valueSubList = valueList.subList(0, arraySize);

        Map<String, Value> values = new TreeMap<>();
        AtomicInteger nextVarIndex = new AtomicInteger(0);
        valueSubList.forEach(item -> {
            int varIndex = nextVarIndex.getAndIncrement();
            values.put("[" + varIndex + "]", valueSubList.get(varIndex));
        });
        return values;
    }
}
