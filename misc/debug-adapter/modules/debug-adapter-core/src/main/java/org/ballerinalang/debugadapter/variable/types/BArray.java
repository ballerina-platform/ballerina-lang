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

package org.ballerinalang.debugadapter.variable.types;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Field;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getStringFrom;

/**
 * Ballerina array variable type.
 */
public class BArray extends BCompoundVariable {

    public BArray(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.ARRAY, value);
    }

    @Override
    public String computeValue() {
        try {
            if (!(jvmValue instanceof ObjectReference)) {
                return UNKNOWN_VALUE;
            }
            ObjectReference jvmValueRef = (ObjectReference) jvmValue;
            String arrayType = getArrayType(jvmValueRef);
            int arraySize = getArraySize(jvmValueRef);
            return String.format("%s[%d]", arrayType, arraySize);
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    @Override
    public Map<String, Value> computeChildVariables() {
        try {
            if (!(jvmValue instanceof ObjectReference)) {
                return new HashMap<>();
            }
            ObjectReference jvmValueRef = (ObjectReference) jvmValue;
            List<Field> fields = jvmValueRef.referenceType().allFields();
            Field arrayValueField = jvmValueRef.getValues(fields).entrySet().stream().filter(fieldValueEntry ->
                    fieldValueEntry.getValue() != null && fieldValueEntry.getKey().toString().endsWith("Values"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()).get(0);

            List<Value> valueList = ((ArrayReference) jvmValueRef.getValue(arrayValueField)).getValues();
            // List length is 100 by default. Create a sub list with actual array size.
            List<Value> valueSubList = valueList.subList(0, getArraySize(jvmValueRef));
            Map<String, Value> values = new TreeMap<>();
            AtomicInteger nextVarIndex = new AtomicInteger(0);
            valueSubList.forEach(item -> {
                int varIndex = nextVarIndex.getAndIncrement();
                values.put(String.format("[%d]", varIndex), valueSubList.get(varIndex));
            });
            return values;
        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }

    /**
     * Returns the type of a given ballerina array typed variable.
     *
     * @param arrayRef object reference of the array instance.
     * @return type of the array.
     */
    private String getArrayType(ObjectReference arrayRef) {
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
    private int getArraySize(ObjectReference arrayRef) {
        List<Field> fields = arrayRef.referenceType().allFields();
        Field arraySizeField = arrayRef.getValues(fields).entrySet().stream().filter(fieldValueEntry ->
                fieldValueEntry.getValue() != null && fieldValueEntry.getKey().toString().endsWith("ArrayValue.size"))
                .map(Map.Entry::getKey).collect(Collectors.toList()).get(0);
        return ((IntegerValue) arrayRef.getValue(arraySizeField)).value();
    }
}
