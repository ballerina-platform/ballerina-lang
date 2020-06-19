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
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.eclipse.lsp4j.debug.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.ballerinalang.debugadapter.variable.VariableUtils.getBType;

/**
 * Ballerina error variable type.
 */
public class BError extends BCompoundVariable {

    private static final String FIELD_MESSAGE = "message";
    private static final String FIELD_CAUSE = "cause";
    private static final String FIELD_DETAILS = "details";

    public BError(Value value, Variable dapVariable) {
        super(BVariableType.ERROR, value, dapVariable);
    }

    @Override
    public String computeValue() {
        return getBType(jvmValue);
    }

    @Override
    public Map<String, Value> computeChildVariables() {
        try {
            if (!(jvmValue instanceof ObjectReference)) {
                return new HashMap<>();
            }
            Map<String, Value> childVarMap = new TreeMap<>();
            ObjectReference jvmValueRef = (ObjectReference) jvmValue;
            Field messageField = jvmValueRef.referenceType().fieldByName(FIELD_MESSAGE);
            Field causeField = jvmValueRef.referenceType().fieldByName(FIELD_CAUSE);
            Field detailsField = jvmValueRef.referenceType().fieldByName(FIELD_DETAILS);
            if (messageField != null) {
                childVarMap.put(FIELD_MESSAGE, jvmValueRef.getValue(messageField));
            }
            if (causeField != null) {
                childVarMap.put(FIELD_CAUSE, jvmValueRef.getValue(causeField));
            }
            if (detailsField != null) {
                childVarMap.put(FIELD_DETAILS, jvmValueRef.getValue(detailsField));
            }
            return childVarMap;
        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }
}
