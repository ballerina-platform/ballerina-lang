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

import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;

import java.util.LinkedList;
import java.util.List;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_RUNTIME_PKG;
import static org.ballerinalang.jvm.util.BLangConstants.BLANG_SRC_FILE_SUFFIX;
import static org.ballerinalang.jvm.util.BLangConstants.INIT_FUNCTION_SUFFIX;
import static org.ballerinalang.jvm.util.BLangConstants.MODULE_INIT_CLASS_NAME;

/**
 * Util Class for handling Error in Ballerina VM.
 *
 * @since 0.995.0
 */
public class BallerinaErrors {
    
    public static final String ERROR_MESSAGE_FIELD = "message";
    public static final String NULL_REF_EXCEPTION = "NullReferenceException";
    public static final String CALL_STACK_ELEMENT = "CallStackElement";
    public static final String ERROR_CAUSE_FIELD = "cause";
    public static final String ERROR_STACK_TRACE = "stackTrace";

    public static final String ERROR_PRINT_PREFIX = "error: ";

    public static ErrorValue createError(String reason) {
        return new ErrorValue(reason, new MapValueImpl<>(BTypes.typeErrorDetail));
    }

    public static ErrorValue createError(String reason, String detail) {
        MapValueImpl<String, Object> detailMap = new MapValueImpl<>(BTypes.typeErrorDetail);
        if (detail != null) {
            detailMap.put(ERROR_MESSAGE_FIELD, detail);
        }
        return new ErrorValue(reason, detailMap);
    }

    public static ErrorValue createError(String reason, MapValue detailMap) {
        return new ErrorValue(reason, detailMap);
    }

    public static ErrorValue createError(Throwable error) {
        if (error instanceof ErrorValue) {
            return (ErrorValue) error;
        }
        return createError(error.getMessage());
    }

    public static ErrorValue createConversionError(Object inputValue, BType targetType) {
        return createError(BallerinaErrorReasons.CONVERSION_ERROR,
                           BLangExceptionHelper
                                   .getErrorMessage(org.ballerinalang.jvm.util.exceptions.RuntimeErrors
                                                            .INCOMPATIBLE_CONVERT_OPERATION,
                                                    TypeChecker.getType(inputValue), targetType));
    }

    public static ErrorValue createTypeCastError(Object sourceVal, BType targetType) {
        throw createError(BallerinaErrorReasons.TYPE_CAST_ERROR,
                          BLangExceptionHelper.getErrorMessage(RuntimeErrors.TYPE_CAST_ERROR,
                                                               TypeChecker.getType(sourceVal), targetType));

    }

    public static ErrorValue createNumericConversionError(Object inputValue, BType targetType) {
        throw createError(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                          BLangExceptionHelper.getErrorMessage(
                                  RuntimeErrors.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                  TypeChecker.getType(inputValue), inputValue, targetType));
    }

    public static ErrorValue createNumericConversionError(Object inputValue, BType inputType, BType targetType) {
        throw createError(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR, BLangExceptionHelper.getErrorMessage(
                RuntimeErrors.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION, inputType, inputValue, targetType));
    }

    static String getErrorMessageFromDetail(MapValueImpl<String, Object> detailMap) {
        return (String) detailMap.get(ERROR_MESSAGE_FIELD);
    }

    public static ErrorValue createCancelledFutureError() {
        return createError(BallerinaErrorReasons.FUTURE_CANCELLED);
    }

    public static ErrorValue createNullReferenceError() {
        return createError(BallerinaErrors.NULL_REF_EXCEPTION);
    }

    public static ErrorValue createUsageError(String errorMsg) {
        return createError("ballerina: " + errorMsg);
    }

    /**
     * Create ballerian error using java exception for interop.
     * @param e java exception
     * @return ballerina error
     */
    public static ErrorValue createInteropError(Exception e) {
        MapValueImpl<String, Object> detailMap = new MapValueImpl<>(BTypes.typeErrorDetail);
        if (e.getMessage() != null) {
            detailMap.put(ERROR_MESSAGE_FIELD, e.getMessage());
        }
        if (e.getCause() != null) {
            detailMap.put(ERROR_CAUSE_FIELD, createError(e.getCause().getClass().getName(), e.getCause().getMessage()));
        }

        return createError(e.getClass().getName(), detailMap);
    }

    public static Object handleResourceError(Object returnValue) {
        if (returnValue instanceof ErrorValue) {
            throw (ErrorValue) returnValue;
        }
        return returnValue;
    }

    public static ArrayValue generateCallStack() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> filteredStack = new LinkedList<>();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement stackTraceElement = BallerinaErrors.filterStackTraceElement(stackTrace, i);
            if (stackTraceElement != null) {
                filteredStack.add(stackTraceElement);
            }
        }
        BType recordType = BallerinaValues.createRecordValue(BALLERINA_RUNTIME_PKG, CALL_STACK_ELEMENT).getType();
        ArrayValue callStack = new ArrayValue(new BArrayType(recordType));
        for (int i = 0; i < filteredStack.size(); i++) {
            callStack.add(i, getStackFrame(filteredStack.get(i)));
        }
        return callStack;
    }

    public static StackTraceElement filterStackTraceElement(StackTraceElement[] stackTrace,
                                                            int currentIndex) {
        StackTraceElement stackFrame = stackTrace[currentIndex];
        String pkgName = stackFrame.getClassName();
        String fileName = stackFrame.getFileName();
        int lineNo = stackFrame.getLineNumber();
        if (lineNo < 0) {
            return null;
        }
        // Handle init function
        if (pkgName.equals(MODULE_INIT_CLASS_NAME)) {
            if (currentIndex != 0) {
                return new StackTraceElement(stackFrame.getClassName(), INIT_FUNCTION_SUFFIX,
                                             fileName, stackFrame.getLineNumber());
            }

            return null;
        }

        if (!fileName.endsWith(BLANG_SRC_FILE_SUFFIX)) {
            // Remove java sources for bal stacktrace if they are not extern functions.
            return null;
        }
        return stackFrame;
    }


    private static MapValue<String, Object> getStackFrame(StackTraceElement stackTraceElement) {
        Object[] values = new Object[4];
        values[0] = stackTraceElement.getMethodName();
        values[1] = stackTraceElement.getClassName();
        values[2] = stackTraceElement.getFileName();
        values[3] = stackTraceElement.getLineNumber();
        return BallerinaValues.createRecord(
                BallerinaValues.createRecordValue(BALLERINA_RUNTIME_PKG, CALL_STACK_ELEMENT), values);
    }
}
