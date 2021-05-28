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
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.IndexedCompoundVariable;
import org.ballerinalang.debugadapter.variable.VariableUtils;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getFieldValue;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getStringValue;

/**
 * Ballerina xml variable type.
 */
public class BXmlSequence extends IndexedCompoundVariable {

    private int elementsCount = -1;
    private static final String FIELD_CHILDREN = "children";
    private static final String FIELD_ELEMENT_DATA = "elementData";

    public BXmlSequence(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.XML, value);
    }

    @Override
    public String computeValue() {
        try {
            return String.format("XMLSequence (children = %d)", getChildrenCount());
        } catch (Exception ignored) {
            return UNKNOWN_VALUE;
        }
    }

    @Override
    public Either<Map<String, Value>, List<Value>> computeChildVariables(int start, int count) {
        List<Value> childValues = new ArrayList<>();
        try {
            Optional<Value> children = getFieldValue(jvmValue, FIELD_CHILDREN);
            if (children.isEmpty()) {
                return Either.forRight(childValues);
            }
            Optional<Value> childArray = VariableUtils.getFieldValue(children.get(), FIELD_ELEMENT_DATA);
            if (childArray.isEmpty()) {
                return Either.forRight(childValues);
            }

            // If count > 0, returns a sublist of the child variables
            // If count == 0, returns all child variables
            if (count > 0) {
                childValues = ((ArrayReference) childArray.get()).getValues(start, count);
            } else {
                childValues = ((ArrayReference) childArray.get()).getValues();
            }
            return Either.forRight(childValues);
        } catch (Exception e) {
            return Either.forRight(childValues);
        }
    }

    @Override
    public int getChildrenCount() {
        if (elementsCount < 0) {
            populateElementCount();
        }
        return elementsCount;
    }

    private void populateElementCount() {
        try {
            Optional<Value> children = getFieldValue(jvmValue, FIELD_CHILDREN);
            if (children.isEmpty()) {
                elementsCount = 0;
            }
            Optional<Value> childArray = VariableUtils.getFieldValue(children.get(), FIELD_ELEMENT_DATA);
            if (childArray.isEmpty()) {
                elementsCount = 0;
            }
            elementsCount = ((ArrayReference) childArray.get()).length();
        } catch (Exception e) {
            elementsCount = 0;
        }
    }
}
