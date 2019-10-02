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

package org.ballerinalang.debugadapter.variable;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Field;
import com.sun.jdi.Value;
import com.sun.tools.jdi.ObjectReferenceImpl;
import org.ballerinalang.debugadapter.VariableUtils;
import org.ballerinalang.debugadapter.variable.types.BArray;
import org.eclipse.lsp4j.debug.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Variable factory.
 */
public class VariableFactory {
    public VariableImpl getVariable(Value value, String varType, String varName) {
        VariableImpl variable = new VariableImpl();
        Variable dapVariable = new Variable();
        variable.setDapVariable(dapVariable);
        dapVariable.setName(varName);

        if (value == null) {
            return null;
        }

        if ("org.ballerinalang.jvm.values.ArrayValue".equalsIgnoreCase(varType)) {
            variable = new BArray();

            Map<String, Value> values = VariableUtils.getChildVariables((ObjectReferenceImpl) value);
            variable.setChildVariables(values);

            dapVariable.setType(varType);
            dapVariable.setValue("Array");
            return variable;
        } else if ("java.lang.Object".equalsIgnoreCase(varType)
                || "org.ballerinalang.jvm.values.MapValue".equalsIgnoreCase(varType)
                || "org.ballerinalang.jvm.values.MapValueImpl".equalsIgnoreCase(varType) // for nested json arrays
        ) {
            // JSONs
            dapVariable.setType("Object");
            if (value.type() == null || value.type().name() == null) {
                dapVariable.setValue("null");
                return variable;
            }
            if ("org.ballerinalang.jvm.values.ArrayValue".equalsIgnoreCase(value.type().name())) {
                // JSON array
                dapVariable.setValue("Array");
                Map<String, Value> values = VariableUtils.getChildVariables((ObjectReferenceImpl) value);
                variable.setChildVariables(values);
                return variable;
            } else if ("java.lang.Long".equalsIgnoreCase(value.type().name())
                    || "java.lang.Boolean".equalsIgnoreCase(value.type().name())
                    || "java.lang.Double".equalsIgnoreCase(value.type().name())) {
                // anydata
                Field valueField = ((ObjectReferenceImpl) value).referenceType().allFields().stream().filter(
                        field -> "value".equals(field.name())).collect(Collectors.toList()).get(0);
                Value longValue = ((ObjectReferenceImpl) value).getValue(valueField);
                dapVariable.setType(varType.split("\\.")[2]);
                dapVariable.setValue(longValue.toString());
                return variable;
            } else if ("java.lang.String".equalsIgnoreCase(value.type().name())) {
                // union
                dapVariable.setType("String");
                String stringValue = value.toString();
                dapVariable.setValue(stringValue);
                return variable;
            } else if ("org.ballerinalang.jvm.values.ErrorValue".equalsIgnoreCase(value.type().name())) {

                List<Field> fields = ((ObjectReferenceImpl) value).referenceType().allFields();

                Field valueField = fields.stream().filter(field ->
                        field.name().equals("reason"))
                        .collect(Collectors.toList()).get(0);

                Value error = ((ObjectReferenceImpl) value).getValue(valueField);
                dapVariable.setType("BError");
                dapVariable.setValue(error.toString());
                return variable;
            } else if ("org.ballerinalang.jvm.values.XMLItem".equalsIgnoreCase(value.type().name())) {
                // TODO: support xml values
                dapVariable.setType("xml");
                dapVariable.setValue(value.toString());
                return variable;
            } else {
                dapVariable.setType("Object");
                List<Field> fields = ((ObjectReferenceImpl) value).referenceType().allFields();

                Optional<Field> valueField = fields.stream().filter(field ->
                        field.typeName().equals("java.util.HashMap$Node[]")).findFirst();
                if (!valueField.isPresent()) {
                    return variable;
                }
                Value jsonValue = ((ObjectReferenceImpl) value).getValue(valueField.get());
                String stringValue = jsonValue == null ? "null" : "Object";
                if (jsonValue == null) {
                    dapVariable.setValue(stringValue);
                    return variable;
                }
                Map<String, Value> values = new HashMap<>();
                ((ArrayReference) jsonValue).getValues().stream().filter(Objects::nonNull).forEach(jsonMap -> {
                    List<Field> jsonValueFields = ((ObjectReferenceImpl) jsonMap).referenceType().visibleFields();
                    Optional<Field> jsonKeyField = jsonValueFields.stream().filter(field ->
                            field.name().equals("key")).findFirst();

                    Optional<Field> jsonValueField = jsonValueFields.stream().filter(field ->
                            field.name().equals("value")).findFirst();

                    if (jsonKeyField.isPresent() && jsonValueField.isPresent()) {
                        Value jsonKey = ((ObjectReferenceImpl) jsonMap).getValue(jsonKeyField.get());
                        Value jsonValue1 = ((ObjectReferenceImpl) jsonMap).getValue(jsonValueField.get());
                        values.put(jsonKey.toString(), jsonValue1);
                    }
                });
                variable.setChildVariables(values);
                dapVariable.setValue(stringValue);
                return variable;
            }
        } else if ("org.ballerinalang.jvm.values.ObjectValue".equalsIgnoreCase(varType)) {
            Map<Field, Value> fieldValueMap = ((ObjectReferenceImpl) value)
                    .getValues(((ObjectReferenceImpl) value).referenceType().allFields());
            Map<String, Value> values = new HashMap<>();
            fieldValueMap.forEach((field, value1) -> {
                // Filter out internal variables
                if (!field.name().startsWith("$") && !field.name().startsWith("nativeData")) {
                    values.put(field.name(), value1);
                }
            });

            variable.setChildVariables(values);
            dapVariable.setType("Object");
            dapVariable.setValue("Object");
            return variable;
        } else if ("java.lang.Long".equalsIgnoreCase(varType) || "java.lang.Boolean".equalsIgnoreCase(varType)
                || "java.lang.Double".equalsIgnoreCase(varType)) {
            Field valueField = ((ObjectReferenceImpl) value).referenceType().allFields().stream().filter(
                    field -> "value".equals(field.name())).collect(Collectors.toList()).get(0);
            Value longValue = ((ObjectReferenceImpl) value).getValue(valueField);
            dapVariable.setType(varType.split("\\.")[2]);
            dapVariable.setValue(longValue.toString());
            return variable;
        } else if ("java.lang.String".equalsIgnoreCase(varType)) {
            dapVariable.setType("String");
            String stringValue = value.toString();
            dapVariable.setValue(stringValue);
            return variable;
        } else if (varType.contains("$value$")) {
            // Record type
            String stringValue = value.type().name();

            List<Field> fields = ((ObjectReferenceImpl) value).referenceType().allFields();

            Optional<Field> valueField = fields.stream().filter(field ->
                    field.typeName().equals("java.util.HashMap$Node[]")).findFirst();

            if (!valueField.isPresent()) {
                dapVariable.setValue(stringValue);
                return variable;
            }
            Value jsonValue = ((ObjectReferenceImpl) value).getValue(valueField.get());

            Map<String, Value> values = new HashMap<>();
            ((ArrayReference) jsonValue).getValues().stream().filter(Objects::nonNull).forEach(jsonMap -> {
                List<Field> jsonValueFields = ((ObjectReferenceImpl) jsonMap).referenceType().visibleFields();


                Optional<Field> jsonKeyField = jsonValueFields.stream().filter(field ->
                        field.name().equals("key")).findFirst();


                Optional<Field> jsonValueField = jsonValueFields.stream().filter(field ->
                        field.name().equals("value")).findFirst();

                if (jsonKeyField.isPresent() && jsonValueField.isPresent()) {
                    Value jsonKey = ((ObjectReferenceImpl) jsonMap).getValue(jsonKeyField.get());
                    Value jsonValue1 = ((ObjectReferenceImpl) jsonMap).getValue(jsonValueField.get());
                    values.put(jsonKey.toString(), jsonValue1);
                }
            });

            variable.setChildVariables(values);
            stringValue = stringValue.replace("$value$", "");
            dapVariable.setType(stringValue);
            dapVariable.setValue(stringValue);
            return variable;
        } else if ("org.ballerinalang.jvm.types.BObjectType".equalsIgnoreCase(varType)) {
            Value typeName = ((ObjectReferenceImpl) value)
                    .getValue(((ObjectReferenceImpl) value).referenceType().fieldByName("typeName"));
            dapVariable.setType(varType);
            String stringValue = typeName.toString();
            dapVariable.setValue(stringValue);
            return variable;
        } else {
            dapVariable.setType(varType);
            String stringValue = value.toString();
            dapVariable.setValue(stringValue);
            return variable;
        }
    }
}
