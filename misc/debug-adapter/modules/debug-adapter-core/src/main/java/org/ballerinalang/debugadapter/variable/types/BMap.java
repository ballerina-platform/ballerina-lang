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

import com.sun.jdi.ArrayReference;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.Method;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.IndexedCompoundVariable;
import org.ballerinalang.debugadapter.variable.VariableUtils;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Ballerina map variable type.
 */
public class BMap extends IndexedCompoundVariable {

    private int mapSize = -1;
    private ArrayReference loadedKeys = null;
    private Value[] loadedValues = null;

    private static final String FIELD_SIZE = "size";
    private static final String METHOD_GET_KEYS = "getKeys";
    private static final String METHOD_GET = "get";
    private static final String MAP_TYPEDESC_SEPARATOR = " & ";

    public BMap(SuspendedContext context, String name, Value value) {
        this(context, name, BVariableType.MAP, value);
    }

    public BMap(SuspendedContext context, String name, BVariableType type, Value value) {
        super(context, name, type, value);
    }

    @Override
    public String computeValue() {
        try {
            // Todo - include constraint type (e.g. map<TYPE>), once we have language level support to get the simple
            //  type
            return String.format("map (size = %d)", getChildrenCount());
        } catch (Exception e) {
            return VariableUtils.getBType(jvmValue);
        }
    }

    @Override
    public Either<Map<String, Value>, List<Value>> computeChildVariables(int start, int count) {
        Map<String, Value> childVarMap = new LinkedHashMap<>();
        try {
            // If count > 0, returns a sublist of the child variables
            // If count == 0, returns all child variables
            Map<Value, Value> mapEntries;
            if (count > 0) {
                mapEntries = getEntries(start, count);
            } else {
                mapEntries = getEntries(0, getChildrenCount());
            }

            for (Map.Entry<Value, Value> mapEntry : mapEntries.entrySet()) {
                childVarMap.put(VariableUtils.getStringFrom(mapEntry.getKey()), mapEntry.getValue());
            }
            return Either.forLeft(childVarMap);
        } catch (Exception ignored) {
            return Either.forLeft(childVarMap);
        }
    }

    @Override
    public int getChildrenCount() {
        if (mapSize < 0) {
            populateMapSize();
        }
        return mapSize;
    }

    private Map<Value, Value> getEntries(int startIndex, int count) {
        if (loadedKeys == null) {
            loadAllKeys();
        }
        Map<Value, Value> entries = new LinkedHashMap<>();
        List<Value> keysRange = loadedKeys.getValues(startIndex, count);
        for (int i = startIndex; i < startIndex + count; i++) {
            Value key = keysRange.get(i - startIndex);
            if (loadedValues[i] == null) {
                loadedValues[i] = getValueFor(key);
            }
            entries.put(key, loadedValues[i]);
        }
        return entries;
    }

    private Value getValueFor(Value key) {
        try {
            Optional<Method> getValueMethod = VariableUtils.getMethod(jvmValue, METHOD_GET);
            if (getValueMethod.isEmpty()) {
                return null;
            }
            return VariableUtils.invokeRemoteVMMethod(context, jvmValue, getValueMethod.get(),
                    Collections.singletonList(key));
        } catch (Exception ignored) {
            return null;
        }
    }

    private void loadAllKeys() {
        try {
            Optional<Method> entrySetMethod = VariableUtils.getMethod(jvmValue, METHOD_GET_KEYS);
            if (entrySetMethod.isEmpty()) {
                return;
            }
            Value keyArray = VariableUtils.invokeRemoteVMMethod(context, jvmValue, entrySetMethod.get(), null);
            loadedKeys = (ArrayReference) keyArray;
            loadedValues = new Value[getChildrenCount()];
        } catch (Exception ignored) {
            loadedKeys = null;
            loadedValues = new Value[0];
        }
    }

    private void populateMapSize() {
        try {
            Optional<Value> mapSizeValue = VariableUtils.getFieldValue(jvmValue, FIELD_SIZE);
            if (mapSizeValue.isEmpty() || !(mapSizeValue.get() instanceof IntegerValue)) {
                mapSize = 0;
                return;
            }
            mapSize = ((IntegerValue) mapSizeValue.get()).intValue();
        } catch (Exception ignored) {
            mapSize = 0;
        }
    }
}
