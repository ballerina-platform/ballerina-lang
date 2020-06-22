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
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableUtils;
import org.eclipse.lsp4j.debug.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Ballerina map variable type.
 */
public class BMap extends BCompoundVariable {

    public BMap(Value value, Variable dapVariable) {
        super(BVariableType.MAP, value, dapVariable);
    }

    @Override
    public String computeValue() {
        // Todo - Extract map type.
        return "map";
    }

    @Override
    public Map<String, Value> computeChildVariables() {
        try {
            if (!(jvmValue instanceof ObjectReference)) {
                return new HashMap<>();
            }
            ObjectReference jvmValueRef = (ObjectReference) jvmValue;
            List<Field> fields = jvmValueRef.referenceType().allFields();
            Optional<Field> valueField = fields.stream().filter(field -> field.typeName()
                    .equals("java.util.HashMap$Node[]")).findFirst();
            if (!valueField.isPresent()) {
                return new HashMap<>();
            }
            Value values = jvmValueRef.getValue(valueField.get());
            Map<String, Value> childVarMap = new HashMap<>();
            ((ArrayReference) values).getValues().stream().filter(Objects::nonNull).forEach(map -> {
                Optional<Value> mapKey = VariableUtils.getFieldValue(map, "key");
                Optional<Value> mapValue = VariableUtils.getFieldValue(map, "value");
                if (mapKey.isPresent() && mapValue.isPresent()) {
                    childVarMap.put(VariableUtils.getStringFrom(mapKey.get()), mapValue.get());
                }
            });
            return childVarMap;
        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }
}
