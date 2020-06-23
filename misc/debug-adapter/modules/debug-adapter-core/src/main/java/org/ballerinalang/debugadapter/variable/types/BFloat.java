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

import com.sun.jdi.DoubleValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.variable.BPrimitiveVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableContext;
import org.ballerinalang.debugadapter.variable.VariableUtils;
import org.eclipse.lsp4j.debug.Variable;

import java.util.Optional;

import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;

/**
 * Ballerina float variable type.
 */
public class BFloat extends BPrimitiveVariable {

    public BFloat(VariableContext context, Value value, Variable dapVariable) {
        super(context, BVariableType.FLOAT, value, dapVariable);
    }

    @Override
    public String computeValue() {
        try {
            if (jvmValue instanceof DoubleValue) {
                return jvmValue.toString();
            } else if (jvmValue instanceof ObjectReference) {
                Optional<Value> field = VariableUtils.getFieldValue(jvmValue, "value");
                if (field.isPresent()) {
                    return field.get().toString();
                }
            }
            return UNKNOWN_VALUE;
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }
}
