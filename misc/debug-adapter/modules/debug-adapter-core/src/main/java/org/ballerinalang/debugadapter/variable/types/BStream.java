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
 * Ballerina stream variable type.
 */
public class BStream extends BSimpleVariable {

    public BStream(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.STREAM, value);
    }

    @Override
    public String computeValue() {
        try {
            String stringValue = getStringValue(context, jvmValue);
            return processStringValue(stringValue);
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    /**
     * Some additional processing is required, as `stringValue()` output of the runtime stream values contains some
     * redundant information.
     */
    private static String processStringValue(String stringValue) {
        if (stringValue.startsWith("stream<(") && stringValue.endsWith(")>")) {
            stringValue = stringValue.replaceFirst("stream<\\(", "stream <").replaceFirst("\\)>$", ">");
        }
        // Adds trailing whitespaces for comma separators, if required.
        stringValue = stringValue.replaceAll(",\\s*", ", ");
        return stringValue;
    }
}
