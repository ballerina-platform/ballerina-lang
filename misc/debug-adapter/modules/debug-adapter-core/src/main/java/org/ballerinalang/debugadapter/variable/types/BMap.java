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
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.IndexedCompoundVariable;
import org.ballerinalang.debugadapter.variable.VariableUtils;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Ballerina map variable type.
 */
public class BMap extends IndexedCompoundVariable {

    private static final String FIELD_MAP_DATA = "table";
    private static final String FIELD_MAP_KEY = "key";
    private static final String FIELD_MAP_VALUE = "value";

    public BMap(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.MAP, value);
    }

    @Override
    public String computeValue() {
        return VariableUtils.getBType(jvmValue);
    }

    @Override
    public Either<Map<String, Value>, List<Value>> computeIndexedChildVariables(int start, int count) {
        Map<String, Value> childVarMap = new LinkedHashMap<>();
        try {
            Optional<Value> mapValues = VariableUtils.getFieldValue(jvmValue, FIELD_MAP_DATA);
            if (mapValues.isEmpty()) {
                return Either.forLeft(childVarMap);
            }

            // If count > 0, returns a sublist of the child variables
            // If count == 0, returns all child variables
            List<Value> mapEntries;
            if (count > 0) {
                mapEntries = ((ArrayReference) mapValues.get()).getValues(start, count);
            } else {
                mapEntries = ((ArrayReference) mapValues.get()).getValues(0, getChildrenCount());
            }

            for (Value mapEntry : mapEntries) {
                if (mapEntry != null) {
                    Optional<Value> mapKey = VariableUtils.getFieldValue(mapEntry, FIELD_MAP_KEY);
                    Optional<Value> mapValue = VariableUtils.getFieldValue(mapEntry, FIELD_MAP_VALUE);
                    if (mapKey.isPresent() && mapValue.isPresent()) {
                        childVarMap.put(VariableUtils.getStringFrom(mapKey.get()), mapValue.get());
                    }
                }
            }
            return Either.forLeft(childVarMap);
        } catch (Exception ignored) {
            return Either.forLeft(childVarMap);
        }
    }

    @Override
    public int getChildrenCount() {
        try {
            Optional<Value> mapValues = VariableUtils.getFieldValue(jvmValue, FIELD_MAP_DATA);
            if (mapValues.isEmpty()) {
                return 0;
            }
            return ((ArrayReference) mapValues.get()).length();
        } catch (Exception ignored) {
            return 0;
        }
    }
}
