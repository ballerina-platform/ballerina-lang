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
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.DebugVariableException;
import org.ballerinalang.debugadapter.variable.IndexedCompoundVariable;
import org.ballerinalang.debugadapter.variable.VariableUtils;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.variable.VariableUtils.FIELD_TYPE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.FIELD_TYPENAME;
import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getFieldValue;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getStringFrom;

/**
 * Ballerina table variable type.
 */
public class BTable extends IndexedCompoundVariable {

    private int tableSize = -1;
    private ArrayReference tableKeys = null;

    private static final String FIELD_CONSTRAINT = "constraint";
    private static final String METHOD_SIZE = "size";
    private static final String METHOD_GETKEYS = "getKeys";
    private static final String METHOD_GET = "get";

    public BTable(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.TABLE, value);
    }

    @Override
    public String computeValue() {
        try {
            String constrainedTypeName = getConstrainedTypeName();
            String tableSize = getTableSizeAsString();
            return String.format("table<%s> (entries = %s)", constrainedTypeName, tableSize);
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    @Override
    public Either<Map<String, Value>, List<Value>> computeChildVariables(int start, int count) {
        try {
            if (!(jvmValue instanceof ObjectReference)) {
                return Either.forRight(new ArrayList<>());
            }
            Value[] tableKeys = getTableKeys(start, count);
            return Either.forRight(getTableEntriesFor(tableKeys));
        } catch (Exception ignored) {
            return Either.forRight(new ArrayList<>());
        }
    }

    @Override
    public int getChildrenCount() {
        return getTableSize();
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

    private Value[] getTableKeys(int start, int count) {
        if (tableKeys == null) {
            populateTableKeys();
        }
        if (tableKeys == null) {
            return new Value[0];
        }
        // If count > 0, returns a sublist of the child variables
        // If count == 0, returns all child variables
        if (count > 0) {
            Value[] keyArray = new Value[count];
            for (int index = start; index < start + count; index++) {
                keyArray[index - start] = tableKeys.getValue(index);
            }
            return keyArray;
        } else {
            int variableLimit = getTableSize();
            Value[] keyArray = new Value[variableLimit];
            for (int index = 0; index < variableLimit; index++) {
                keyArray[index] = tableKeys.getValue(index);
            }
            return keyArray;
        }
    }

    private void populateTableKeys() {
        try {
            Optional<Method> method = VariableUtils.getMethod(jvmValue, METHOD_GETKEYS);
            if (method.isEmpty()) {
                return;
            }
            Value keys = ((ObjectReference) jvmValue).invokeMethod(getContext().getOwningThread().getThreadReference(),
                    method.get(), new ArrayList<>(), ObjectReference.INVOKE_SINGLE_THREADED);
            if (!(keys instanceof ArrayReference)) {
                return;
            }
            tableKeys = (ArrayReference) keys;
        } catch (Exception ignored) {
            tableKeys = null;
        }
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
}
