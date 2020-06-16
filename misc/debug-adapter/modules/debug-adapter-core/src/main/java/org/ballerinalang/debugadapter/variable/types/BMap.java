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
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.JVMValueType;
import org.eclipse.lsp4j.debug.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;

/**
 * Ballerina map variable type.
 */
public class BMap extends BCompoundVariable {

    public BMap(Value value, Variable dapVariable) {
        super(BVariableType.MAP, value, dapVariable);
    }

    @Override
    public String computeValue() {
        // Todo - Extract map type.
        return "map";
    }

    @Override
    public Map<String, Value> computeChildVariables() {
        try {
            if (!(jvmValue instanceof ObjectReference)) {
                return new HashMap<>();
            }
            ObjectReference jvmValueRef = (ObjectReference) jvmValue;
            List<Field> fields = jvmValueRef.referenceType().allFields();
            Optional<Field> valueField = fields.stream().filter(field -> field.typeName()
                    .equals("java.util.HashMap$Node[]")).findFirst();
            if (!valueField.isPresent()) {
                return new HashMap<>();
            }
            Value mapValue = jvmValueRef.getValue(valueField.get());
            Map<String, Value> values = new HashMap<>();
            ((ArrayReference) mapValue).getValues().stream().filter(Objects::nonNull).forEach(jsonMap -> {
                List<Field> mapValueFields = ((ObjectReference) jsonMap).referenceType().visibleFields();
                Optional<Field> mapKeyFields = mapValueFields.stream().filter(field ->
                        field.name().equals("key")).findFirst();
                Optional<Field> jsonValueField = mapValueFields.stream().filter(field ->
                        field.name().equals("value")).findFirst();

                if (mapKeyFields.isPresent() && jsonValueField.isPresent()) {
                    Value mapKey = ((ObjectReference) jsonMap).getValue(mapKeyFields.get());
                    Value mapValue1 = ((ObjectReference) jsonMap).getValue(jsonValueField.get());
                    values.put(getJsonKeyString(mapKey), mapValue1);
                }
            });
            return values;
        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }

    private String getJsonKeyString(Value key) {
        ObjectReference keyRef = (key instanceof ObjectReference) ? (ObjectReference) key : null;
        if (keyRef == null) {
            return UNKNOWN_VALUE;
        }
        if (keyRef.referenceType().name().equals(JVMValueType.BMPSTRING.getString())
                || keyRef.referenceType().name().equals(JVMValueType.NONBMPSTRING.getString())) {
            Optional<Field> valueField = keyRef.referenceType().allFields().stream()
                    .filter(field -> field.name().equals("value")).findAny();
            if (valueField.isPresent()) {
                return keyRef.getValue(valueField.get()).toString();
            }
            return UNKNOWN_VALUE;
        } else {
            return keyRef.toString();
        }
    }
}
