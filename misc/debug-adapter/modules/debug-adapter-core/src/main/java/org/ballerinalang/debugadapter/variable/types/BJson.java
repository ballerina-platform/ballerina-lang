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
 * Ballerina json variable type.
 */
public class BJson extends IndexedCompoundVariable {

    private static final String FIELD_JSON_DATA = "table";
    private static final String FIELD_JSON_KEY = "key";
    private static final String FIELD_JSON_VALUE = "value";

    public BJson(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.JSON, value);
    }

    @Override
    public String computeValue() {
        return "map<json>";
    }

    @Override
    public Either<Map<String, Value>, List<Value>> computeIndexedChildVariables(int start, int count) {
        Map<String, Value> childMap = new LinkedHashMap<>();
        try {
            Optional<Value> jsonValues = VariableUtils.getFieldValue(jvmValue, FIELD_JSON_DATA);
            if (jsonValues.isEmpty()) {
                return Either.forLeft(childMap);
            }

            // If count > 0, returns a sublist of the child variables
            // If count == 0, returns all child variables
            List<Value> jsonEntries;
            if (count > 0) {
                jsonEntries = ((ArrayReference) jsonValues.get()).getValues(start, count);
            } else {
                jsonEntries = ((ArrayReference) jsonValues.get()).getValues(0, getChildrenCount());
            }

            for (Value jsonEntry : jsonEntries) {
                if (jsonEntry == null) {
                    continue;
                }
                Optional<Value> jsonKey = VariableUtils.getFieldValue(jsonEntry, FIELD_JSON_KEY);
                Optional<Value> jsonValue = VariableUtils.getFieldValue(jsonEntry, FIELD_JSON_VALUE);
                if (jsonKey.isPresent() && jsonValue.isPresent()) {
                    childMap.put(VariableUtils.getStringFrom(jsonKey.get()), jsonValue.get());
                }
            }
            return Either.forLeft(childMap);
        } catch (Exception ignored) {
            return Either.forLeft(childMap);
        }
    }

    @Override
    public int getChildrenCount() {
        try {
            Optional<Value> jsonValues = VariableUtils.getFieldValue(jvmValue, FIELD_JSON_DATA);
            if (jsonValues.isEmpty()) {
                return 0;
            }
            return ((ArrayReference) jsonValues.get()).length();
        } catch (Exception ignored) {
            return 0;
        }
    }
}
