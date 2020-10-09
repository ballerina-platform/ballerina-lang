/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/
package org.ballerinalang.langlib.error;

import io.ballerina.jvm.api.BErrorCreator;
import io.ballerina.jvm.api.BStringUtils;
import io.ballerina.jvm.api.BValueCreator;
import io.ballerina.jvm.api.Types;
import io.ballerina.jvm.api.runtime.Module;
import io.ballerina.jvm.api.types.Type;
import io.ballerina.jvm.api.values.BMap;
import io.ballerina.jvm.api.values.BString;
import io.ballerina.jvm.scheduling.Strand;
import io.ballerina.jvm.types.AttachedFunction;
import io.ballerina.jvm.types.BArrayType;
import io.ballerina.jvm.types.BField;
import io.ballerina.jvm.types.BObjectType;
import io.ballerina.jvm.values.AbstractObjectValue;
import io.ballerina.jvm.values.ArrayValue;
import io.ballerina.jvm.values.ArrayValueImpl;
import io.ballerina.jvm.values.ErrorValue;
import io.ballerina.jvm.values.FutureValue;
import io.ballerina.jvm.values.ObjectValue;

import java.util.Collections;

import static io.ballerina.jvm.util.BLangConstants.BALLERINA_LANG_ERROR_PKG_ID;
import static io.ballerina.jvm.values.ErrorValue.CALL_STACK_ELEMENT;

/**
 * Get the stackTrace of an error value.
 *
 * @since 0.990.4
 */
public class StackTrace {

    public static ObjectValue stackTrace(ErrorValue value) {

        BObjectType callStackObjType = new BObjectType("CallStack", new Module("ballerina", "lang.error", null), 0);
        callStackObjType.setAttachedFunctions(new AttachedFunction[]{});
        callStackObjType.setFields(
                Collections.singletonMap("callStack", new BField(new BArrayType(Types.TYPE_ANY), null, 0)));

        CallStack callStack = new CallStack(callStackObjType);
        callStack.callStack = getCallStackArray(value.getStackTrace());
        callStack.callStack.freezeDirect();
        return callStack;
    }

    private static ArrayValue getCallStackArray(StackTraceElement[] stackTrace) {
        Type recordType = BValueCreator
                .createRecordValue(BALLERINA_LANG_ERROR_PKG_ID, CALL_STACK_ELEMENT).getType();
        Object[] array = new Object[stackTrace.length];
        for (int i = 0; i < stackTrace.length; i++) {
            array[i] = getStackFrame(stackTrace[i]);
        }
        return new ArrayValueImpl(array, new BArrayType(recordType));
    }

    static BMap<BString, Object> getStackFrame(StackTraceElement stackTraceElement) {
        Object[] values = new Object[4];
        values[0] = stackTraceElement.getMethodName();
        values[1] = stackTraceElement.getClassName();
        values[2] = stackTraceElement.getFileName();
        values[3] = stackTraceElement.getLineNumber();
        return BValueCreator.createRecordValue(
                BValueCreator.createRecordValue(BALLERINA_LANG_ERROR_PKG_ID, CALL_STACK_ELEMENT), values);
    }

    /**
     * Represent Ballerina call stack when the error is constructed.
     */
    public static class CallStack extends AbstractObjectValue {
        ArrayValue callStack;

        public CallStack(BObjectType type) {
            super(type);
        }

        @Override
        public Object call(Strand strand, String funcName, Object... args) {
            throw BErrorCreator.createError(BStringUtils.fromString("No such field or method: " + funcName));
        }

        @Override
        public FutureValue start(Strand strand, String funcName, Object... args) {
            throw BErrorCreator.createError(BStringUtils.fromString("No such field or method: " + funcName));
        }

        @Override
        public Object get(BString fieldName) {
            if (fieldName.getValue().equals("callStack")) {
                return callStack;
            }
            throw BErrorCreator.createError(BStringUtils.fromString("No such field or method: callStack"));
        }

        @Override
        public void set(BString fieldName, Object value) {
            throw BErrorCreator.createError(BStringUtils.fromString("No such field or method: callStack"));
        }
    }
}
