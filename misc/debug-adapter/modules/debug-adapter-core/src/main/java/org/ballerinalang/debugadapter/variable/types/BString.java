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

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.variable.BPrimitiveVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.JVMValueType;
import org.eclipse.lsp4j.debug.Variable;

import java.util.Optional;

import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;

/**
 * Ballerina string variable type.
 */
public class BString extends BPrimitiveVariable {

    public BString(Value value, Variable dapVariable) {
        super(BVariableType.STRING, value, dapVariable);
    }

    @Override
    public String computeValue() {
        try {
            if (!(jvmValue instanceof ObjectReference)) {
                return UNKNOWN_VALUE;
            }
            ObjectReference jvmValueRef = (ObjectReference) jvmValue;
            if (jvmValueRef.referenceType().name().equals(JVMValueType.BMPSTRING.getString())
                    || jvmValueRef.referenceType().name().equals(JVMValueType.NONBMPSTRING.getString())) {
                Optional<Field> valueField = jvmValueRef.referenceType().allFields().stream()
                        .filter(field -> field.name().equals("value")).findAny();
                if (valueField.isPresent()) {
                    return jvmValueRef.getValue(valueField.get()).toString();
                }
                return UNKNOWN_VALUE;
            } else {
                return jvmValueRef.toString();
            }
        } catch (Exception ignored) {
            return UNKNOWN_VALUE;
        }
    }
}
