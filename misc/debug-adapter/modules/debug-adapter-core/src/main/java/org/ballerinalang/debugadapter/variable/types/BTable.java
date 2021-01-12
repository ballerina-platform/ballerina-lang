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
import com.sun.jdi.IntegerValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.DebugVariableException;
import org.ballerinalang.debugadapter.variable.VariableFactory;
import org.ballerinalang.debugadapter.variable.VariableUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ballerinalang.debugadapter.variable.VariableUtils.FIELD_TYPE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.FIELD_TYPENAME;
import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getFieldValue;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getStringFrom;

/**
 * Ballerina table variable type.
 */
public class BTable extends BCompoundVariable {

    private static final String FIELD_CONSTRAINT = "constraint";
    private static final String METHOD_SIZE = "size";
    private static final String METHOD_GETKEYS = "getKeys";
    private static final String METHOD_GET = "get";
    // Maximum number of table entries that will be shown in the debug view.
    private static final int CHILD_VAR_LIMIT = 10;

    private int tableSize = -1;

    public BTable(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.TABLE, value);
    }

    @Override
    public String computeValue() {
        try {
            String constrainedTypeName = getConstrainedTypeName();
            String tableSize = getTableSizeAsString();
            return String.format("table<%s>[%s]", constrainedTypeName, tableSize);
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    @Override
    protected Map<String, Value> computeChildVariables() {
        try {
            if (!(jvmValue instanceof ObjectReference)) {
                return new HashMap<>();
            }
            Value[] tableKeys = getTableKeys();
            List<Value> tableEntries = getTableEntriesFor(tableKeys);

            Map<String, Value> values = new TreeMap<>();
            AtomicInteger nextVarIndex = new AtomicInteger(0);
            tableEntries.forEach(item -> {
                int varIndex = nextVarIndex.getAndIncrement();
                String keyStr = VariableFactory.getVariable(context, tableKeys[varIndex]).getDapVariable().getValue();
                values.put("[" + keyStr + "]", item);
            });

            // If the size of the table exceeds the allowed child variable limit, appends a notification (which is
            // wrapped inside a dummy variable) to the list of child variables, to inform the user.
            if (getTableSize() > CHILD_VAR_LIMIT) {
                addTailChildVariable(values);
            }
            return values;
        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }

    /**
     * Retrieves the constraint type of the table variable, in string format.
     */
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

    private int getTableSize() {
        if (tableSize < 0) {
            populateTableSize();
        }
        return tableSize;
    }

    private String getTableSizeAsString() {
        int tableSize = getTableSize();
        return tableSize >= 0 ? String.valueOf(tableSize) : UNKNOWN_VALUE;
    }

    private void populateTableSize() {
        try {
            // Invokes "size()" method of the table value object.
            Optional<Method> method = VariableUtils.getMethod(jvmValue, METHOD_SIZE);
            if (method.isEmpty()) {
                tableSize = -1;
                return;
            }
            Value size = ((ObjectReference) jvmValue).invokeMethod(getContext().getOwningThread().getThreadReference(),
                    method.get(), new ArrayList<>(), ObjectReference.INVOKE_SINGLE_THREADED);
            tableSize = ((IntegerValue) size).intValue();
        } catch (Exception e) {
            this.tableSize = -1;
        }
    }

    private Value[] getTableKeys() throws Exception {
        Optional<Method> method = VariableUtils.getMethod(jvmValue, METHOD_GETKEYS);
        if (method.isEmpty()) {
            return new Value[0];
        }
        Value keys = ((ObjectReference) jvmValue).invokeMethod(getContext().getOwningThread().getThreadReference(),
                method.get(), new ArrayList<>(), ObjectReference.INVOKE_SINGLE_THREADED);
        if (!(keys instanceof ArrayReference)) {
            return new Value[0];
        }

        int variableLimit = getChildVariableLimit();
        Value[] firstNKeys = new Value[variableLimit];
        for (int index = 0; index < variableLimit; index++) {
            firstNKeys[index] = ((ArrayReference) keys).getValue(index);
        }
        return firstNKeys;
    }

    private List<Value> getTableEntriesFor(Value[] tableKeys) {
        try {
            List<Value> tableValues = new ArrayList<>();
            for (Value key : tableKeys) {
                // Invokes "get(key)" method on table variable instance object.
                Optional<Method> method = VariableUtils.getMethod(jvmValue, METHOD_GET);
                if (method.isEmpty()) {
                    return new ArrayList<>();
                }
                ArrayList<Value> args = new ArrayList<>();
                args.add(key);
                Value tableValue = ((ObjectReference) jvmValue).invokeMethod(getContext().getOwningThread()
                        .getThreadReference(), method.get(), args, ObjectReference.INVOKE_SINGLE_THREADED);
                tableValues.add(tableValue);
            }
            return tableValues;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private int getChildVariableLimit() {
        return Math.max(Math.min(getTableSize(), CHILD_VAR_LIMIT), 0);
    }

    /**
     * If the size of the table exceeds the allowed child variable limit, appends a notification (which is wrapped
     * inside a dummy variable) to the list of child variables, to inform the user.
     *
     * @param values the list of child variables.
     */
    private void addTailChildVariable(Map<String, Value> values) {
        values.put("[...]", context.getAttachedVm().mirrorOf(String.format("Showing first %d elements out of %d " +
                "total entries", CHILD_VAR_LIMIT, getTableSize())));
    }
}
