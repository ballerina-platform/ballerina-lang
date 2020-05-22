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
import com.sun.jdi.Value;
import com.sun.tools.jdi.ObjectReferenceImpl;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.eclipse.lsp4j.debug.Variable;

import java.util.HashMap;
import java.util.Map;

/**
 * Ballerina record variable type.
 */
public class BRecord extends BCompoundVariable {

    private final ObjectReferenceImpl jvmValueRef;

    public BRecord(Value value, Variable dapVariable) {
        this.jvmValueRef = value instanceof ObjectReferenceImpl ? (ObjectReferenceImpl) value : null;
        dapVariable.setType(BVariableType.RECORD.getString());
        dapVariable.setValue(this.getValue());
        this.setDapVariable(dapVariable);
        this.computeChildVariables();
    }

    @Override
    public String getValue() {
        try {
            // Extracts object type name from the reflected type class.
            String[] split = this.jvmValueRef.referenceType().classObject().reflectedType().name().split("\\.");
            for (String element : split) {
                if (element.contains("$value$")) {
                    return element.replaceFirst("\\$value\\$", "");
                }
            }
            return "unknown";
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ignored) {
            return "unknown";
        }
    }

    @Override
    public void computeChildVariables() {
        try {
            Map<Field, Value> fieldValueMap = jvmValueRef.getValues(jvmValueRef.referenceType().allFields());
            Map<String, Value> values = new HashMap<>();
            // Uses the ballerina record type name to extract ballerina record fields from the jvm reference.
            String balRecordFiledIdentifier = this.getValue() + ".";
            fieldValueMap.forEach((field, value) -> {
                if (field.toString().contains(balRecordFiledIdentifier)) {
                    values.put(field.name(), value);
                }
            });
            this.setChildVariables(values);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ignored) {
            this.setChildVariables(new HashMap<>());
        }
    }
}
