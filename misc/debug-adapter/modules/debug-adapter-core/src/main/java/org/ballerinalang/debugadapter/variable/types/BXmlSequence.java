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
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getFieldValue;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getStringValue;

/**
 * Ballerina xml variable type.
 */
public class BXmlSequence extends BCompoundVariable {

    private static final String FIELD_CHILDREN = "children";
    private static final String FIELD_ELEMENT_DATA = "elementData";

    public BXmlSequence(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.XML, value);
    }

    @Override
    public String computeValue() {
        try {
            return getStringValue(context, jvmValue);
        } catch (Exception ignored) {
            return UNKNOWN_VALUE;
        }
    }

    @Override
    public Map<String, Value> computeChildVariables() {
        Map<String, Value> childMap = new HashMap<>();
        try {
            Optional<Value> children = getFieldValue(jvmValue, FIELD_CHILDREN);
            if (!children.isPresent()) {
                return childMap;
            }
            Optional<Value> childArray = VariableUtils.getFieldValue(children.get(), FIELD_ELEMENT_DATA);
            if (!childArray.isPresent()) {
                return childMap;
            }
            List<Value> childrenValues = ((ArrayReference) childArray.get()).getValues();
            AtomicInteger index = new AtomicInteger();
            childrenValues.forEach(ref -> {
                if (ref != null) {
                    childMap.put(Integer.toString(index.getAndIncrement()), ref);
                }
            });
            return childMap;
        } catch (Exception e) {
            return childMap;
        }
    }
}
