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
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Ballerina future variable type.
 */
public class BFuture extends BCompoundVariable {

    private static final String FIELD_RESULT = "result";
    private static final String FIELD_IS_DONE = "isDone";
    private static final String FIELD_PANIC = "panic";
    private static final String METHOD_LOCALIZEDMESSAGE = "getLocalizedMessage";

    public BFuture(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.FUTURE, value);
    }

    @Override
    public String computeValue() {
        return BVariableType.FUTURE.getString();
    }

    @Override
    public Map<String, Value> computeChildVariables() {
        Map<String, Value> childVarMap = new TreeMap<>();
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
                    Value stringValue = ((ObjectReference) panic.get()).invokeMethod(getContext().getOwningThread()
                                    .getThreadReference(), method.get(), new ArrayList<>(),
                            ObjectReference.INVOKE_SINGLE_THREADED);
                    childVarMap.put(FIELD_PANIC, stringValue);
                }
            }
            return childVarMap;
        } catch (Exception ignored) {
            return childVarMap;
        }
    }
}
