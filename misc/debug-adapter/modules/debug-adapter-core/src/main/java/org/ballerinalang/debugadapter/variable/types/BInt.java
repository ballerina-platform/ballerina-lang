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

import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BSimpleVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableUtils;

import java.util.Optional;

import static org.ballerinalang.debugadapter.variable.VariableUtils.FIELD_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;

/**
 * Ballerina integer type.
 */
public class BInt extends BSimpleVariable {

    public BInt(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.INT, value);
    }

    @Override
    public String computeValue() {
        try {
            if (jvmValue instanceof IntegerValue || jvmValue instanceof LongValue) {
                return jvmValue.toString();
            } else if (jvmValue instanceof ObjectReference) {
                Optional<Value> field = VariableUtils.getFieldValue(jvmValue, FIELD_VALUE);
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
