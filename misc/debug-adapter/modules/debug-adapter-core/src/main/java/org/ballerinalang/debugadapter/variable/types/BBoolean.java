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

import com.sun.jdi.BooleanValue;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.variable.BPrimitiveVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.eclipse.lsp4j.debug.Variable;

import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;

/**
 * Ballerina boolean variable type.
 */
public class BBoolean extends BPrimitiveVariable {

    public BBoolean(Value value, Variable dapVariable) {
        super(BVariableType.BOOLEAN, value, dapVariable);
    }

    @Override
    public String computeValue() {
        if (jvmValue instanceof BooleanValue) {
            return jvmValue.toString();
        } else if (jvmValue instanceof ObjectReference) {
            ObjectReference valueObjectRef = ((ObjectReference) jvmValue);
            Field valueField = valueObjectRef.referenceType().allFields().stream().filter(field ->
                    field.name().equals("value")).collect(Collectors.toList()).get(0);
            return valueObjectRef.getValue(valueField).toString();
        } else {
            return UNKNOWN_VALUE;
        }
    }
}
