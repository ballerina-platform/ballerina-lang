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

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableContext;
import org.ballerinalang.debugadapter.variable.VariableUtils;
import org.eclipse.lsp4j.debug.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getFieldValue;

/**
 * Ballerina xml variable type.
 */
public class BXmlSequence extends BCompoundVariable {

    public BXmlSequence(VariableContext context, Value value, Variable dapVariable) {
        super(context, BVariableType.XML, value, dapVariable);
    }

    @Override
    public String computeValue() {
        try {
            Optional<Method> method = VariableUtils.getMethod(jvmValue, "stringValue");
            if (method.isPresent()) {
                Value decimalValue = ((ObjectReference) jvmValue).invokeMethod(getContext().getOwningThread(),
                        method.get(), new ArrayList<>(), ObjectReference.INVOKE_SINGLE_THREADED);
                return VariableUtils.getStringFrom(decimalValue);
            }
            return UNKNOWN_VALUE;
        } catch (Exception ignored) {
            return UNKNOWN_VALUE;
        }
    }

    @Override
    public Map<String, Value> computeChildVariables() {
        try {
            Optional<Value> children = getFieldValue(jvmValue, "children");
            if (!children.isPresent()) {
                return new HashMap<>();
            }
            Optional<Value> childArray = VariableUtils.getFieldValue(children.get(), "elementData");
            if (!childArray.isPresent()) {
                return new HashMap<>();
            }
            List<Value> childrenValues = ((ArrayReference) childArray.get()).getValues();
            Map<String, Value> childMap = new HashMap<>();
            AtomicInteger index = new AtomicInteger();
            childrenValues.forEach(ref -> {
                if (ref != null) {
                    childMap.put(Integer.toString(index.getAndIncrement()), ref);
                }
            });
            return childMap;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
