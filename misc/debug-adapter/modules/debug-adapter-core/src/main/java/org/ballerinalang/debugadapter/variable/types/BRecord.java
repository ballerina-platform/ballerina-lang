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
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.NamedCompoundVariable;
import org.ballerinalang.debugadapter.variable.VariableUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;

/**
 * Ballerina record variable type.
 */
public class BRecord extends NamedCompoundVariable {

    private static final String RECORD_FIELD_PATTERN_IDENTIFIER = "$value$";

    public BRecord(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.RECORD, value);
    }

    @Override
    public String computeValue() {
        try {
            return isAnonymous() ? "anonymous" : VariableUtils.getRecordBType(jvmValue);
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    @Override
    public Map<String, Value> computeChildVariables() {
        try {
            if (!(jvmValue instanceof ObjectReference)) {
                return new LinkedHashMap<>();
            }
            ObjectReference jvmValueRef = (ObjectReference) jvmValue;
            Map<Field, Value> fieldValueMap = jvmValueRef.getValues(jvmValueRef.referenceType().allFields());
            Map<String, Value> recordFields = new LinkedHashMap<>();
            fieldValueMap.forEach((field, value) -> {
                if (field.toString().contains(RECORD_FIELD_PATTERN_IDENTIFIER)) {
                    recordFields.put(field.name(), value);
                }
            });
            return recordFields;
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    @Override
    public int getChildrenCount() {
        try {
            if (!(jvmValue instanceof ObjectReference)) {
                return 0;
            }
            ObjectReference jvmValueRef = (ObjectReference) jvmValue;
            Map<Field, Value> fieldValueMap = jvmValueRef.getValues(jvmValueRef.referenceType().allFields());
            long recordFieldCount = fieldValueMap.keySet()
                    .stream()
                    .filter(field -> field.toString().contains(RECORD_FIELD_PATTERN_IDENTIFIER))
                    .count();

            return Long.valueOf(recordFieldCount).intValue();
        } catch (Exception ignored) {
            return 0;
        }
    }

    /**
     * Returns whether this record instance is anonymous.
     *
     * @return whether the given record instance is anonymous.
     */
    private boolean isAnonymous() {
        String bType = VariableUtils.getBType(jvmValue);
        return bType.startsWith("$");
    }
}
