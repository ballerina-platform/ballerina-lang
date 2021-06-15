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
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.NamedCompoundVariable;
import org.ballerinalang.debugadapter.variable.VariableUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Ballerina strand wait result type.
 *
 * @since 2.0.0
 */
public class BStrandWaitResult extends NamedCompoundVariable {

    private static final String FIELD_DONE = "done";
    private static final String FIELD_RESULT = "result";
    private static final String VALUE_DISPLAY_TEXT = "Wait Result";

    public BStrandWaitResult(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.OBJECT, value);
    }

    @Override
    public String computeValue() {
        return VALUE_DISPLAY_TEXT;
    }

    @Override
    public Map<String, Value> computeChildVariables() {
        try {
            Map<String, Value> childVarMap = new LinkedHashMap<>();
            Optional<Value> done = VariableUtils.getFieldValue(jvmValue, FIELD_DONE);
            Optional<Value> result = VariableUtils.getFieldValue(jvmValue, FIELD_RESULT);

            // Adds NotNull information as child attributes.
            done.ifPresent(value -> childVarMap.put(FIELD_DONE, value));
            result.ifPresent(value -> childVarMap.put(FIELD_RESULT, value));
            return childVarMap;
        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }

    @Override
    public int getChildrenCount() {
        // maximum children size will be 2 ('isDone' and the result).
        return 2;
    }
}
