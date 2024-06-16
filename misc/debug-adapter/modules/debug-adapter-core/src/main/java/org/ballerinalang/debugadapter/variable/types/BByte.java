/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import com.sun.jdi.ByteValue;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BSimpleVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;

import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;

/**
 * Ballerina byte type.
 */
public class BByte extends BSimpleVariable {

    public BByte(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.BYTE, value);
    }

    @Override
    public String computeValue() {
        try {
            // The ballerina byte type is a predefined name for a union of the int values in the range 0 to 255
            // inclusive. It is equivalent to the built-in subtype int:Unsigned8.
            // Since JDI represents byte values as signed values by default, we need to explicitly converts them to
            // unsigned values in here.
            if (jvmValue instanceof ByteValue byteValue) {
                byte signedByteValue = byteValue.byteValue();
                int unsignedValue = signedByteValue & 0xFF;
                return String.valueOf(unsignedValue);
            }
            return UNKNOWN_VALUE;
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }
}
