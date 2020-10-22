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

import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BSimpleVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;

import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getStringValue;

/**
 * Ballerina typedesc variable type.
 */
public class BTypeDesc extends BSimpleVariable {

    private static final String TYPEDESC_VALUE_PREFIX = "typedesc ";

    public BTypeDesc(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.TYPE_DESC, value);
    }

    @Override
    public String computeValue() {
        try {
            String stringValue = getStringValue(context, jvmValue);
            if (stringValue.startsWith(TYPEDESC_VALUE_PREFIX)) {
                stringValue = stringValue.replaceAll(TYPEDESC_VALUE_PREFIX, "");
            }
            return stringValue;
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }
}
