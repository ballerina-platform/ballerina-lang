/*
*   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_RUNTIME_PKG;

/**
 * Util Class for handling Error in Ballerina VM.
 *
 * @since 0.995.0
 */
public class BallerinaErrors {
    
    public static final String ERROR_MESSAGE_FIELD = "message";
    public static final String NULL_REF_EXCEPTION = "NullReferenceException";
    public static final String CALL_STACK_ELEMENT = "CallStackElement";

    public static final String ERROR_PRINT_PREFIX = "error: ";

    public static ErrorValue createError(String reason) {
        return new ErrorValue(reason, new MapValueImpl<>());
    }

    public static ErrorValue createError(String reason, String detail) {
        MapValueImpl<String, Object> detailMap = new MapValueImpl<>();
        if (detail != null) {
            detailMap.put(ERROR_MESSAGE_FIELD, detail);
        }
        return new ErrorValue(reason, detailMap);
    }

    public static ErrorValue createError(String reason, MapValueImpl detailMap) {
        return new ErrorValue(reason, detailMap);
    }

    public static ErrorValue createConversionError(Object inputValue, BType targetType) {
        return createError(BallerinaErrorReasons.CONVERSION_ERROR,
                           BLangExceptionHelper
                                   .getErrorMessage(org.ballerinalang.jvm.util.exceptions.RuntimeErrors
                                                            .INCOMPATIBLE_CONVERT_OPERATION,
                                                    TypeChecker.getType(inputValue), targetType));
    }

    static ErrorValue createTypeCastError(Object sourceVal, BType targetType) {
        throw createError(BallerinaErrorReasons.TYPE_CAST_ERROR,
                          BLangExceptionHelper.getErrorMessage(RuntimeErrors.TYPE_CAST_ERROR,
                                                               TypeChecker.getType(sourceVal), targetType));

    }

    static ErrorValue createNumericConversionError(Object inputValue, BType targetType) {
        throw createError(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                          BLangExceptionHelper.getErrorMessage(
                                  RuntimeErrors.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                  TypeChecker.getType(inputValue), inputValue, targetType));
    }

    public static ArrayValue generateCallStack() {
        List<MapValue<String, Object>> sfList = new ArrayList<>();
        for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
            MapValue<String, Object> sf = getStackFrame(frame);
            if (sf != null) {
                sfList.add(0, sf);
            }
        }
        BType recordType = BallerinaValues.createRecordValue(BALLERINA_RUNTIME_PKG, CALL_STACK_ELEMENT).getType();
        ArrayValue callStack = new ArrayValue(recordType);
        for (int i = 0; i < sfList.size(); i++) {
            callStack.add(i, sfList.get(i));
        }
        return callStack;
    }

    private static MapValue<String, Object> getStackFrame(StackTraceElement stackTraceElement) {
        Object[] values = new Object[4];
        values[0] = stackTraceElement.getMethodName();
        values[1] = stackTraceElement.getClassName();
        values[2] = stackTraceElement.getFileName();
        values[3] = stackTraceElement.getLineNumber();
        return BallerinaValues.populateRecordFields(
                BallerinaValues.createRecordValue(BALLERINA_RUNTIME_PKG, CALL_STACK_ELEMENT), values);
    }
}
