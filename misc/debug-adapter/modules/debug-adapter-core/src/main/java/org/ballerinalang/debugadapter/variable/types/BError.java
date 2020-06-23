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

import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableContext;
import org.ballerinalang.debugadapter.variable.VariableUtils;
import org.eclipse.lsp4j.debug.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static org.ballerinalang.debugadapter.variable.VariableUtils.getBType;

/**
 * Ballerina error variable type.
 */
public class BError extends BCompoundVariable {

    private static final String FIELD_MESSAGE = "message";
    private static final String FIELD_CAUSE = "cause";
    private static final String FIELD_DETAILS = "details";

    public BError(VariableContext context, Value value, Variable dapVariable) {
        super(context, BVariableType.ERROR, value, dapVariable);
    }

    @Override
    public String computeValue() {
        return getBType(jvmValue);
    }

    @Override
    public Map<String, Value> computeChildVariables() {
        try {
            Map<String, Value> childVarMap = new TreeMap<>();
            // Fetches message, cause and details of the error.
            Optional<Value> message = VariableUtils.getFieldValue(jvmValue, FIELD_MESSAGE);
            Optional<Value> cause = VariableUtils.getFieldValue(jvmValue, FIELD_CAUSE);
            Optional<Value> details = VariableUtils.getFieldValue(jvmValue, FIELD_DETAILS);
            // Adds NotNull information as child attributes.
            message.ifPresent(value -> childVarMap.put(FIELD_MESSAGE, value));
            cause.ifPresent(value -> childVarMap.put(FIELD_CAUSE, value));
            details.ifPresent(value -> childVarMap.put(FIELD_DETAILS, value));
            return childVarMap;
        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }
}
