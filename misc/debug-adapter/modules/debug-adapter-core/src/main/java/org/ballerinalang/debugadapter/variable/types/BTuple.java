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

package org.ballerinalang.debugadapter.variable.types;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Field;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.eclipse.lsp4j.debug.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Ballerina tuple variable type.
 */
public class BTuple extends BCompoundVariable {

    public BTuple(Value value, Variable dapVariable) {
        super(BVariableType.TUPLE, value, dapVariable);
    }

    @Override
    public String computeValue() {
        try {
            if (!(jvmValue instanceof ObjectReference)) {
                return "unknown";
            }
            ObjectReference jvmValueRef = (ObjectReference) jvmValue;
            List<Field> fields = jvmValueRef.referenceType().allFields();
            Field arrayValueField = jvmValueRef.getValues(fields).entrySet().stream().filter(fieldValueEntry ->
                    fieldValueEntry.getValue() != null && fieldValueEntry.getKey().toString().endsWith("Values"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()).get(0);

            String arrayType = arrayValueField.toString();
            arrayType = arrayType.replaceFirst("org.ballerinalang.jvm.values.TupleValueImpl.", "")
                    .replaceFirst("Values", "").replaceFirst("ref", "any");

            Field arraySizeField = jvmValueRef.getValues(fields).entrySet().stream().filter(fieldValueEntry ->
                    fieldValueEntry.getValue() != null &&
                            fieldValueEntry.getKey().toString().endsWith("ArrayValue.size"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()).get(0);
            int arraySize = ((IntegerValue) jvmValueRef.getValue(arraySizeField)).value();
            return String.format("%s[%d]", arrayType, arraySize);
        } catch (Exception ignored) {
            return "unknown";
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

            Field arraySizeField = jvmValueRef.getValues(fields).entrySet().stream().filter(fieldValueEntry ->
                    fieldValueEntry.getValue() != null &&
                            fieldValueEntry.getKey().toString().endsWith("ArrayValue.size"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()).get(0);

            int arraySize = ((IntegerValue) jvmValueRef.getValue(arraySizeField)).value();
            List<Value> valueList = ((ArrayReference) jvmValueRef.getValue(arrayValueField)).getValues();

            // List length is 100 by default. Create a sub list with actual array size
            List<Value> valueSubList = valueList.subList(0, arraySize);
            Map<String, Value> values = new TreeMap<>();
            AtomicInteger nextVarIndex = new AtomicInteger(0);
            valueSubList.forEach(item -> {
                int varIndex = nextVarIndex.getAndIncrement();
                values.put("[" + varIndex + "]", valueSubList.get(varIndex));
            });
            return values;
        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }
}
