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

import com.sun.jdi.Field;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.Value;
import com.sun.tools.jdi.ObjectReferenceImpl;
import org.ballerinalang.debugadapter.VariableUtils;
import org.ballerinalang.debugadapter.variable.VariableImpl;
import org.eclipse.lsp4j.debug.Variable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * Array variable type.
 */
public class BArray extends VariableImpl {

    private final ObjectReferenceImpl value;
    private Map<String, Value> childVariables;

    public BArray(Value value, Variable dapVariable) {
        this.value = (ObjectReferenceImpl) value;
        this.setDapVariable(dapVariable);
        dapVariable.setType(this.toString());
        dapVariable.setValue(this.toString());

        Map<String, Value> values = VariableUtils.getChildVariables((ObjectReferenceImpl) value);
        this.setChildVariables(values);
    }

    @Override
    public Map<String, Value> getChildVariables() {
        return childVariables;
    }

    @Override
    public void setChildVariables(Map<String, Value> childVariables) {
        this.childVariables = childVariables;
    }

    @Override
    public String toString() {
        List<Field> fields = value.referenceType().allFields();
        Field arrayValueField = value.getValues(fields).entrySet().stream()
                .filter(fieldValueEntry ->
                        fieldValueEntry.getValue() != null
                                && fieldValueEntry.getKey().toString().endsWith("Values"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()).get(0);
        String arrayType = arrayValueField.toString();

        arrayType = arrayType.replaceFirst("org.ballerinalang.jvm.values.ArrayValue.", "");
        arrayType = arrayType.replaceFirst("Values", "");
        arrayType = arrayType.replaceFirst("ref", "Array");

        Field arraySizeField = value
                .getValues(fields).entrySet().stream()
                .filter(fieldValueEntry ->
                        fieldValueEntry.getValue() != null
                                && fieldValueEntry.getKey().toString().endsWith("ArrayValue.size"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()).get(0);
        int arraySize = ((IntegerValue) value.getValue(arraySizeField)).value();

        return arrayType + "[" + arraySize + "]";
    }

}
