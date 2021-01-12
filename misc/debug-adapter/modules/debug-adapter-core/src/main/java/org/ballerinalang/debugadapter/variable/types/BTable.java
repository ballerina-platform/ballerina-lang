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
import org.ballerinalang.debugadapter.variable.BSimpleVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.DebugVariableException;
import org.ballerinalang.debugadapter.variable.VariableUtils;

import java.util.ArrayList;
import java.util.Optional;

import static org.ballerinalang.debugadapter.variable.VariableUtils.FIELD_TYPE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.FIELD_TYPENAME;
import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getFieldValue;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getStringFrom;

/**
 * Ballerina table variable type.
 */
public class BTable extends BSimpleVariable {

    private static final String FIELD_CONSTRAINT = "constraint";
    private static final String METHOD_SIZE = "size()";

    public BTable(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.TABLE, value);
    }

    @Override
    public String computeValue() {
        try {
            String constrainedTypeName = getConstrainedTypeName();
            String tableSize = getTableSize();
            return String.format("table<%s>[%s]", constrainedTypeName, tableSize);
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    private String getConstrainedTypeName() throws DebugVariableException {
        Optional<Value> type = getFieldValue(jvmValue, FIELD_TYPE);
        if (type.isEmpty()) {
            return UNKNOWN_VALUE;
        }
        Optional<Value> constraint = getFieldValue(type.get(), FIELD_CONSTRAINT);
        if (constraint.isEmpty()) {
            return UNKNOWN_VALUE;
        }
        Optional<Value> constraintTypeName = getFieldValue(constraint.get(), FIELD_TYPENAME);
        if (constraintTypeName.isEmpty()) {
            return UNKNOWN_VALUE;
        }
        return getStringFrom(constraintTypeName.get());
    }

    private String getTableSize() throws Exception {
        // Invokes "size()" method of the table value object.
        Optional<Method> method = VariableUtils.getMethod(jvmValue, METHOD_SIZE);
        if (method.isEmpty()) {
            return UNKNOWN_VALUE;
        }
        Value size = ((ObjectReference) jvmValue).invokeMethod(getContext().getOwningThread().getThreadReference(),
                method.get(), new ArrayList<>(), ObjectReference.INVOKE_SINGLE_THREADED);
        return getStringFrom(size);
    }
}
