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

import com.sun.jdi.Method;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.NamedCompoundVariable;
import org.ballerinalang.debugadapter.variable.VariableUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getStringFrom;

/**
 * Ballerina future variable type.
 */
public class BFuture extends NamedCompoundVariable {

    private static final String FIELD_RESULT = "result";
    private static final String FIELD_IS_DONE = "isDone";
    private static final String FIELD_PANIC = "panic";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_CONSTRAINT = "constraint";
    private static final String FIELD_TYPENAME = "typeName";
    private static final String METHOD_LOCALIZEDMESSAGE = "getLocalizedMessage";

    public BFuture(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.FUTURE, value);
    }

    @Override
    public String computeValue() {
        return BVariableType.FUTURE.getString() + "<" + getConstrainedType() + ">";
    }

    @Override
    public Map<String, Value> computeChildVariables() {
        Map<String, Value> childVarMap = new LinkedHashMap<>();
        try {
            Optional<Value> isDone = VariableUtils.getFieldValue(jvmValue, FIELD_IS_DONE);
            Optional<Value> result = VariableUtils.getFieldValue(jvmValue, FIELD_RESULT);
            Optional<Value> panic = VariableUtils.getFieldValue(jvmValue, FIELD_PANIC);

            isDone.ifPresent(value -> childVarMap.put(FIELD_IS_DONE, value));
            result.ifPresent(value -> childVarMap.put(FIELD_RESULT, value));
            if (panic.isPresent()) {
                // Invokes "getLocalizedMessage()" method of the panic object.
                Optional<Method> method = VariableUtils.getMethod(panic.get(), METHOD_LOCALIZEDMESSAGE);
                if (method.isPresent()) {
                    Value stringValue = VariableUtils.invokeRemoteVMMethod(context, panic.get(),
                            METHOD_LOCALIZEDMESSAGE, null);
                    childVarMap.put(FIELD_PANIC, stringValue);
                }
            }
            return childVarMap;
        } catch (Exception ignored) {
            return childVarMap;
        }
    }

    @Override
    public int getChildrenCount() {
        // maximum children size will be 3 (isDone, result and panic).
        return 3;
    }

    private String getConstrainedType() {
        try {
            Optional<Value> futureType = VariableUtils.getFieldValue(jvmValue, FIELD_TYPE);
            Optional<Value> constrainedType = VariableUtils.getFieldValue(futureType.get(), FIELD_CONSTRAINT);
            Optional<Value> typeName = VariableUtils.getFieldValue(constrainedType.get(), FIELD_TYPENAME);
            return getStringFrom(typeName.get());
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }
}
