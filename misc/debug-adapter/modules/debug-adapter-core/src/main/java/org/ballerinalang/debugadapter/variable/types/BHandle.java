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

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static org.ballerinalang.debugadapter.variable.VariableUtils.FIELD_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getFieldValue;

/**
 * Ballerina handle variable type.
 */
public class BHandle extends BCompoundVariable {

    public BHandle(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.HANDLE, value);
    }

    @Override
    public String computeValue() {
        try {
            Optional<Value> value = getFieldValue(jvmValue, FIELD_VALUE);
            if (value.isPresent() && (value.get() instanceof ObjectReference)) {
                return String.format("instance of %s", value.get().type().name());
            }
            return UNKNOWN_VALUE;
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    @Override
    public Map<String, Value> computeChildVariables() {
        try {
            Map<String, Value> childVarMap = new TreeMap<>();
            Optional<Value> isDone = VariableUtils.getFieldValue(jvmValue, FIELD_VALUE);
            isDone.ifPresent(value -> childVarMap.put(FIELD_VALUE, value));
            return childVarMap;
        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }
}
