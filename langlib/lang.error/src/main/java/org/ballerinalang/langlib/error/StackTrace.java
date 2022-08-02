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

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.scheduling.Strand;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_LANG_ERROR_PKG_ID;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BLANG_SRC_FILE_SUFFIX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.DOT;
import static io.ballerina.runtime.api.constants.RuntimeConstants.EMPTY;
import static io.ballerina.runtime.api.constants.RuntimeConstants.FILE_NAME_PERIOD_SEPARATOR;
import static io.ballerina.runtime.api.values.BError.CALL_STACK_ELEMENT;

/**
 * Get the stackTrace of an error value.
 *
 * @since 0.990.4
 */
public class StackTrace {

    public static BObject stackTrace(BError value) {

        ObjectType callStackObjType = TypeCreator
                .createObjectType("CallStack", new Module("ballerina", "lang.error", null), 0);
        callStackObjType.setMethods(new MethodType[]{});
        callStackObjType
                .setFields(Collections.singletonMap("callStack",
                                                    TypeCreator.createField(TypeCreator.createArrayType(
                                                            PredefinedTypes.TYPE_ANY),
                                                                            null, 0)));

        CallStack callStack = new CallStack(callStackObjType);
        callStack.callStack = getCallStackArray(value.getStackTrace());
        callStack.callStack.freezeDirect();
        return callStack;
    }

    private static BArray getCallStackArray(StackTraceElement[] stackTrace) {
        Type recordType = ValueCreator
                .createRecordValue(BALLERINA_LANG_ERROR_PKG_ID, CALL_STACK_ELEMENT).getType();
        Object[] array = new Object[stackTrace.length];
        for (int i = 0; i < stackTrace.length; i++) {
            array[i] = getStackFrame(stackTrace[i]);
        }
        return ValueCreator.createArrayValue(array, TypeCreator.createArrayType(recordType));
    }

    static BMap<BString, Object> getStackFrame(StackTraceElement stackTraceElement) {

        Object[] values = new Object[4];
        values[0] = stackTraceElement.getMethodName();
        values[2] = stackTraceElement.getFileName();
        values[3] = stackTraceElement.getLineNumber();

        String moduleName = Utils.decodeIdentifier(stackTraceElement.getClassName())
                .replace(FILE_NAME_PERIOD_SEPARATOR, DOT);
        String fileName = stackTraceElement.getFileName().replace(BLANG_SRC_FILE_SUFFIX, EMPTY);
        if (!moduleName.equals(fileName)) {
            int index = moduleName.lastIndexOf(DOT + fileName);
            if (index != -1) {
                values[1] = moduleName.substring(0, index);
            } else {
                values[1] = moduleName;
            }
        }
        return ValueCreator.createRecordValue(
                ValueCreator.createRecordValue(BALLERINA_LANG_ERROR_PKG_ID, CALL_STACK_ELEMENT), values);
    }

    /**
     * Represent Ballerina call stack when the error is constructed.
     */
    public static class CallStack implements BObject {

        BArray callStack;

        private ObjectType type;
        private BTypedesc typedesc;

        public CallStack(ObjectType type) {
            this.type = type;
            this.typedesc = ValueCreator.createTypedescValue(type);
        }

        @Override
        public Object call(Strand strand, String funcName, Object... args) {
            throw ErrorCreator.createError(StringUtils.fromString("No such field or method: " + funcName));
        }

        @Override
        public BFuture start(Strand strand, String funcName, Object... args) {
            throw ErrorCreator.createError(StringUtils.fromString("No such field or method: " + funcName));
        }

        @Override
        public String stringValue(BLink parent) {
            return "object " + type.toString();
        }

        @Override
        public String expressionStringValue(BLink parent) {
            return null;
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public Object get(BString fieldName) {
            if (fieldName.getValue().equals("callStack")) {
                return callStack;
            }
            throw ErrorCreator.createError(StringUtils.fromString("No such field or method: callStack"));
        }

        @Override
        public long getIntValue(BString fieldName) {
            return 0;
        }

        @Override
        public double getFloatValue(BString fieldName) {
            return 0;
        }

        @Override
        public BString getStringValue(BString fieldName) {
            return null;
        }

        @Override
        public boolean getBooleanValue(BString fieldName) {
            return false;
        }

        @Override
        public BMap getMapValue(BString fieldName) {
            return null;
        }

        @Override
        public BObject getObjectValue(BString fieldName) {
            return null;
        }

        @Override
        public BArray getArrayValue(BString fieldName) {
            return null;
        }

        @Override
        public void addNativeData(String key, Object data) {

        }

        @Override
        public Object getNativeData(String key) {
            return null;
        }

        @Override
        public HashMap<String, Object> getNativeData() {
            return null;
        }

        @Override
        public void set(BString fieldName, Object value) {
            throw ErrorCreator.createError(StringUtils.fromString("No such field or method: callStack"));
        }

        @Override
        public Object copy(Map<Object, Object> refs) {
            return null;
        }

        @Override
        public Object frozenCopy(Map<Object, Object> refs) {
            return null;
        }

        @Override
        public BTypedesc getTypedesc() {
            return typedesc;
        }

    }
}
