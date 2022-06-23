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
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import io.ballerina.identifier.Utils;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.NamedCompoundVariable;
import org.ballerinalang.debugadapter.variable.VariableUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;

/**
 * Ballerina record variable type.
 */
public class BRecord extends NamedCompoundVariable {

    private static final String GETKEYS_METHOD_SIGNATURE_PATTERN = ".*Object;$";
    private static final String GET_METHOD_SIGNATURE_PATTERN = "\\(Ljava/lang/Object;\\)Ljava/lang/Object;";
    private static final String METHOD_GET_KEYS = "getKeys";
    private static final String METHOD_GET = "get";

    private ArrayReference loadedKeys = null;

    public BRecord(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.RECORD, value);
    }

    @Override
    public String computeValue() {
        try {
            return isAnonymous() ? "anonymous" : VariableUtils.getRecordBType(jvmValue);
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    @Override
    public Map<String, Value> computeChildVariables() {
        try {
            if (!(jvmValue instanceof ObjectReference)) {
                return new LinkedHashMap<>();
            }

            Map<String, Value> childVarMap = new LinkedHashMap<>();
            Map<Value, Value> recordFields = getRecordFields();

            for (Map.Entry<Value, Value> mapEntry : recordFields.entrySet()) {
                childVarMap.put(Utils.encodeNonFunctionIdentifier(
                        Utils.escapeSpecialCharacters(VariableUtils.getStringFrom(mapEntry.getKey()))),
                        mapEntry.getValue());
            }
            return childVarMap;
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    private Map<Value, Value> getRecordFields() {
        try {
            loadAllKeys();
            Map<Value, Value> recordFields = new LinkedHashMap<>();
            List<Value> keysRange = loadedKeys.getValues(0, loadedKeys.length());

            for (int i = 0; i < loadedKeys.length(); i++) {
                Value key = keysRange.get(i);
                recordFields.put(key, getValueFor(key));
            }
            return recordFields;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void loadAllKeys() {
        if (loadedKeys == null) {
            try {
                Optional<Method> getKeysMethod = VariableUtils.getMethod(jvmValue, METHOD_GET_KEYS,
                        GETKEYS_METHOD_SIGNATURE_PATTERN);
                Value keyArray = ((ObjectReference) jvmValue).invokeMethod(
                        context.getOwningThread().getThreadReference(), getKeysMethod.get(), Collections.emptyList(),
                        ObjectReference.INVOKE_SINGLE_THREADED);
                loadedKeys = (ArrayReference) keyArray;
            } catch (Exception ignored) {
                loadedKeys = null;
            }
        }
    }

    private Value getValueFor(Value key) {
        try {
            Optional<Method> getMethod = VariableUtils.getMethod(jvmValue, METHOD_GET, GET_METHOD_SIGNATURE_PATTERN);
            if (getMethod.isEmpty()) {
                return null;
            }
            return ((ObjectReference) jvmValue).invokeMethod(context.getOwningThread().getThreadReference(),
                    getMethod.get(), Collections.singletonList(key), ObjectReference.INVOKE_SINGLE_THREADED);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public int getChildrenCount() {
        try {
            if (!(jvmValue instanceof ObjectReference)) {
                return 0;
            }
            loadAllKeys();
            return loadedKeys == null ? 0 : loadedKeys.length();
        } catch (Exception ignored) {
            return 0;
        }
    }

    /**
     * Returns whether this record instance is anonymous.
     *
     * @return whether the given record instance is anonymous.
     */
    private boolean isAnonymous() {
        String bType = VariableUtils.getBType(jvmValue);
        return bType.startsWith("$");
    }
}
