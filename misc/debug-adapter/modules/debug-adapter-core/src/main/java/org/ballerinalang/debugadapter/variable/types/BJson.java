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
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Ballerina json variable type.
 */
public class BJson extends BCompoundVariable {

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
    public Map<String, Value> computeChildVariables() {
        Map<String, Value> childMap = new HashMap<>();
        try {
            Optional<Value> jsonValues = VariableUtils.getFieldValue(jvmValue, FIELD_JSON_DATA);
            if (!jsonValues.isPresent()) {
                return childMap;
            }
            for (Value jsonMap : ((ArrayReference) jsonValues.get()).getValues()) {
                if (jsonMap != null) {
                    Optional<Value> jsonKey = VariableUtils.getFieldValue(jsonMap, FIELD_JSON_KEY);
                    Optional<Value> jsonValue = VariableUtils.getFieldValue(jsonMap, FIELD_JSON_VALUE);
                    if (jsonKey.isPresent() && jsonValue.isPresent()) {
                        childMap.put(VariableUtils.getStringFrom(jsonKey.get()), jsonValue.get());
                    }
                }
            }
            return childMap;
        } catch (Exception ignored) {
            return childMap;
        }
    }
}
