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

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.eclipse.lsp4j.debug.Variable;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;

/**
 * Ballerina record variable type.
 */
public class BRecord extends BCompoundVariable {

    public BRecord(Value value, Variable dapVariable) {
        super(BVariableType.RECORD, value, dapVariable);
    }

    @Override
    public String computeValue() {
        try {
            if (!(jvmValue instanceof ObjectReference)) {
                return UNKNOWN_VALUE;
            }
            ObjectReference jvmValueRef = (ObjectReference) jvmValue;
            // Extracts object type name from the reflected type class.
            String[] split = jvmValueRef.referenceType().classObject().reflectedType().name().split("\\.");
            for (String element : split) {
                if (element.contains("$value$")) {
                    return element.replaceFirst("\\$value\\$", "");
                }
            }
            return UNKNOWN_VALUE;
        } catch (Exception ignored) {
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
            Map<Field, Value> fieldValueMap = jvmValueRef.getValues(jvmValueRef.referenceType().allFields());
            Map<String, Value> values = new HashMap<>();
            // Uses ballerina record type name to filter record fields from the jvm reference.
            String balRecordFiledIdentifier = this.computeValue() + ".";
            fieldValueMap.forEach((field, value) -> {
                if (field.toString().contains(balRecordFiledIdentifier)) {
                    values.put(field.name(), value);
                }
            });
            return values;
        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }
}
