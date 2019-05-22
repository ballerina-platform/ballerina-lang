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

import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;

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

    public static ErrorValue createError(String reason, MapValue detailMap) {
        return new ErrorValue(reason, detailMap);
    }

    public static ErrorValue createConversionError(Object inputValue, BType targetType) {
        return createError(org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.CONVERSION_ERROR,
                             org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper
                                     .getErrorMessage(org.ballerinalang.jvm.util.exceptions.RuntimeErrors
                                                              .INCOMPATIBLE_CONVERT_OPERATION,
                                                      TypeChecker.getType(inputValue), targetType));
    }

    static ErrorValue createTypeCastError(Object sourceVal, BType targetType) {
        throw new ErrorValue(BallerinaErrorReasons.TYPE_CAST_ERROR,
                             BLangExceptionHelper.getErrorMessage(RuntimeErrors.TYPE_CAST_ERROR,
                                                                  TypeChecker.getType(sourceVal), targetType));
    }

    static ErrorValue createNumericConversionError(Object inputValue, BType targetType) {
        throw new ErrorValue(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                             BLangExceptionHelper.getErrorMessage(
                                     RuntimeErrors.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                     TypeChecker.getType(inputValue), inputValue, targetType));
    }

    public static String getPrintableStackTrace(ErrorValue error) {
        return getPrintableStackTrace(getErrorMessage(error), error.getStackTrace());
    }

    public static String getPrintableStackTrace(String errorMsg, StackTraceElement[] stackTrace) {

        StringBuilder sb = new StringBuilder();
        sb.append(errorMsg).append("\n\tat ");
        // Append function/action/resource name with package path (if any)
        appendStackTraceElement(sb, stackTrace, 0, "");
        for (int i = 1; i < stackTrace.length; i++) {
            if (!appendStackTraceElement(sb, stackTrace, i, "\n\t   ")) {
                break;
            }
        }
        return sb.toString();
    }

    private static boolean appendStackTraceElement(StringBuilder sb, StackTraceElement[] stackTrace, int currentIndex,
                                                   String tab) {
        StackTraceElement stackFrame = stackTrace[currentIndex];
        String pkgName = stackFrame.getClassName();
        String fileName = stackFrame.getFileName();
        int lineNo = stackFrame.getLineNumber();
        if (lineNo < 0) {
            return false;
        }
        // Handle init function
        if (pkgName.equals(MODULE_INIT_CLASS_NAME)) {
            sb.append(tab);
            sb.append(INIT_FUNCTION_SUFFIX);
            if (currentIndex != 0) {
                fileName = stackTrace[currentIndex - 1].getFileName();
            }
            sb.append("(").append(fileName);
            // Append the line number
            sb.append(":").append(lineNo);
            sb.append(")");
            return false;
        }
        // Remove java sources for bal stacktrace.
        if (!fileName.equals(pkgName.concat(BLANG_SRC_FILE_SUFFIX))) {
            return true;
        }
        // Append the method name
        sb.append(tab).append(stackFrame.getMethodName());
        // Append the filename
        sb.append("(").append(fileName);
        // Append the line number
        sb.append(":").append(lineNo).append(")");
        return true;
    }

    private static String getErrorMessage(ErrorValue errorValue) {
        String errorMsg = "";
        boolean reasonAdded = false;
        String reason = errorValue.getReason();
        Object details = errorValue.getDetails();
        if (reason != null && !reason.isEmpty()) {
            errorMsg = reason;
            reasonAdded = true;
        }
        if (details != null) {
            errorMsg = errorMsg + (reasonAdded ? " " : "") + details.toString();
        }
        return errorMsg;
    }

    public static void printStackTraceOnMainMethodError(ErrorValue errorValue) {
        ErrorHandlerUtils.printError("error: " + getPrintableStackTrace(errorValue));
    }
}
