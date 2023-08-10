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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.variable.VariableUtils.FIELD_TYPE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.FIELD_TYPENAME;
import static org.ballerinalang.debugadapter.variable.VariableUtils.INTERNAL_TYPE_PREFIX;
import static org.ballerinalang.debugadapter.variable.VariableUtils.INTERNAL_TYPE_REF_TYPE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getFieldValue;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getStringFrom;

/**
 * Ballerina table variable type.
 */
public class BTable extends IndexedCompoundVariable {

    private int tableSize = -1;
    private final Map<String, Value> tableValues = new LinkedHashMap<>();

    private static final String FIELD_CONSTRAINT = "constraint";
    private static final String FIELD_REFERRED_TYPE = "referredType";
    private static final String METHOD_SIZE = "size";
    private static final String METHOD_GET_ITERATOR = "getIterator";
    private static final String METHOD_HAS_NEXT = "hasNext";
    private static final String METHOD_NEXT = "next";
    private static final String METHOD_GET_VALUES = "getValues";
    private static final String ITERATOR_VALUE_PATTERN = ".*IteratorValue;$";

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
            // If count is missing or 0, all variables should be returned as defined in debug adapter protocol.
            if (count == 0) {
                count = getChildrenCount();
            }
            populateTableValues(start, count);
            return Either.forRight(getChildVariables(start, count));
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
    private String getConstrainedTypeName() {
        try {
            Optional<Value> type = getFieldValue(jvmValue, FIELD_TYPE);
            if (type.isPresent() && isTypeReferenceType(type.get())) {
                type = getFieldValue(type.get(), FIELD_REFERRED_TYPE);
            }
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
        } catch (DebugVariableException e) {
            return UNKNOWN_VALUE;
        }
    }

    private boolean isTypeReferenceType(Value runtimeType) {
        return runtimeType.type().name().equals(INTERNAL_TYPE_PREFIX + INTERNAL_TYPE_REF_TYPE);
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

    private void populateTableValues(int start, int count) throws Exception {
        int index = 0;
        Value iterator = getIterator();
        while (hasNext(iterator) && index < start + count) {
            Value next = nextElement(iterator);
            if (index >= start && index < start + count) {
                if (tableValues.containsKey(String.valueOf(index))) {
                    continue;
                }
                Value values = getValues(next);
                if (values == null) {
                    continue;
                }
                Value value = ((ArrayReference) values).getValue(1);
                tableValues.put(String.valueOf(index), value);
            }
            index++;
        }
    }

    private ArrayList<Value> getChildVariables(int start, int count) {
        ArrayList<Value> childVariables = new ArrayList<>();
        for (int i = start; i < start + count; i++) {
            childVariables.add(tableValues.get(String.valueOf(i)));
        }
        return childVariables;
    }

    private Value getIterator() throws Exception {
        Optional<Method> getIteratorMethod =
                VariableUtils.getMethod(jvmValue, METHOD_GET_ITERATOR, ITERATOR_VALUE_PATTERN);
        if (getIteratorMethod.isEmpty()) {
            return null;
        }
        return ((ObjectReference) jvmValue).invokeMethod(getContext().getOwningThread().getThreadReference(),
                getIteratorMethod.get(), new ArrayList<>(), ObjectReference.INVOKE_SINGLE_THREADED);
    }

    private boolean hasNext(Value iterator) throws Exception {
        Optional<Method> hasNextMethod = VariableUtils.getMethod(iterator, METHOD_HAS_NEXT);
        if (hasNextMethod.isEmpty()) {
            return false;
        }
        Value hasNext = ((ObjectReference) iterator).invokeMethod(getContext().getOwningThread().getThreadReference(),
                hasNextMethod.get(), new ArrayList<>(), ObjectReference.INVOKE_SINGLE_THREADED);
        return Boolean.parseBoolean(hasNext.toString());
    }

    private Value nextElement(Value iterator) throws Exception {
        Optional<Method> nextMethod = VariableUtils.getMethod(iterator, METHOD_NEXT);
        if (nextMethod.isEmpty()) {
            return null;
        }
        return ((ObjectReference) iterator).invokeMethod(getContext().getOwningThread().getThreadReference(),
                nextMethod.get(), new ArrayList<>(), ObjectReference.INVOKE_SINGLE_THREADED);
    }

    private Value getValues(Value next) throws Exception {
        Optional<Method> getValuesMethod = VariableUtils.getMethod(next, METHOD_GET_VALUES);
        if (getValuesMethod.isEmpty()) {
            return null;
        }
        return ((ObjectReference) next).invokeMethod(getContext().getOwningThread().getThreadReference(),
                getValuesMethod.get(), new ArrayList<>(), ObjectReference.INVOKE_SINGLE_THREADED);
    }
}
