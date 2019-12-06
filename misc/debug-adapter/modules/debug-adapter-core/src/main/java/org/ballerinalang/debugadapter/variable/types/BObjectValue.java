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
import com.sun.jdi.Value;
import com.sun.tools.jdi.ObjectReferenceImpl;
import org.ballerinalang.debugadapter.variable.VariableImpl;
import org.eclipse.lsp4j.debug.Variable;

import java.util.HashMap;
import java.util.Map;


/**
 * object value type.
 */
public class BObjectValue extends VariableImpl {

    public BObjectValue(Value value, Variable dapVariable) {
        this.setDapVariable(dapVariable);
        Map<Field, Value> fieldValueMap = ((ObjectReferenceImpl) value)
                .getValues(((ObjectReferenceImpl) value).referenceType().allFields());
        Map<String, Value> values = new HashMap<>();
        fieldValueMap.forEach((field, value1) -> {
            // Filter out internal variables
            if (!field.name().startsWith("$") && !field.name().startsWith("nativeData")) {
                values.put(field.name(), value1);
            }
        });

        this.setChildVariables(values);
        dapVariable.setType("object");
        dapVariable.setValue(this.toString());
    }

    @Override
    public String toString() {
        return "object";
    }
}
