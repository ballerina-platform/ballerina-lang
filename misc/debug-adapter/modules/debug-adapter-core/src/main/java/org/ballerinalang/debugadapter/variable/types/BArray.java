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
import com.sun.jdi.Value;
import com.sun.tools.jdi.ObjectReferenceImpl;
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
 * Ballerina array variable type.
 */
public class BArray extends BCompoundVariable {

    private final ObjectReferenceImpl jvmValueRef;

    public BArray(Value value, Variable dapVariable) {
         this.jvmValueRef = value instanceof ObjectReferenceImpl ? (ObjectReferenceImpl) value : null;
        dapVariable.setType(BVariableType.ARRAY.getString());
        dapVariable.setValue(this.getValue());
        this.setDapVariable(dapVariable);
        this.computeChildVariables();
    }

    @Override
    public String getValue() {
        try {
            List<Field> fields = jvmValueRef.referenceType().allFields();
            Field arrayValueField = jvmValueRef.getValues(fields).entrySet().stream().filter(fieldValueEntry ->
                    fieldValueEntry.getValue() != null && fieldValueEntry.getKey().toString().endsWith("Values"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()).get(0);

            String arrayType = arrayValueField.toString();
            arrayType = arrayType.replaceFirst("org.ballerinalang.jvm.values.ArrayValueImpl.", "")
                    .replaceFirst("Values", "").replaceFirst("ref", "any");

            Field arraySizeField = jvmValueRef.getValues(fields).entrySet().stream().filter(fieldValueEntry ->
                    fieldValueEntry.getValue() != null &&
                            fieldValueEntry.getKey().toString().endsWith("ArrayValue.size"))
                    .map(Map.Entry::getKey).collect(Collectors.toList()).get(0);
            int arraySize = ((IntegerValue) jvmValueRef.getValue(arraySizeField)).value();
            return String.format("%s[%d]", arrayType, arraySize);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            return "unknown";
        }
    }

    @Override
    public void computeChildVariables() {
        try {
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
            this.setChildVariables(values);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ignored) {
            this.setChildVariables(new HashMap<>());
        }
    }
}
