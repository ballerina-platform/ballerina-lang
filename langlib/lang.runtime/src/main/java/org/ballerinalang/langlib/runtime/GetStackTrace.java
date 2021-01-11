/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.langlib.runtime;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.util.List;

/**
 * Native implementation for get error's call stack.
 *
 * @since 2.0.0
 */
public class GetStackTrace {

    private static final BString EMPTY_ERROR_MESSAGE = StringUtils.fromString("");

    public static BArray getStackTrace() {
        List<StackTraceElement> filteredStack = ErrorCreator.createError(EMPTY_ERROR_MESSAGE).getCallStack();
        Type recordType = ValueCreator.createRecordValue(Constants.BALLERINA_RUNTIME_PKG_ID,
                Constants.CALL_STACK_ELEMENT).getType();
        BArray callStack = ValueCreator.createArrayValue(TypeCreator.createArrayType(recordType));
        for (int i = 0; i < filteredStack.size(); i++) {
            Object[] values = getStackFrame(filteredStack.get(i));
            BMap<BString, Object> createRecordValue = ValueCreator.createRecordValue(ValueCreator.
                    createRecordValue(Constants.BALLERINA_RUNTIME_PKG_ID, Constants.CALL_STACK_ELEMENT), values);
            callStack.add(i, createRecordValue);
        }
        return callStack;
    }

    private static Object[] getStackFrame(StackTraceElement stackTraceElement) {
        Object[] values = new Object[4];
        values[0] = stackTraceElement.getMethodName();
        values[1] = stackTraceElement.getClassName();
        values[2] = stackTraceElement.getFileName();
        values[3] = stackTraceElement.getLineNumber();
        return values;
    }
}
