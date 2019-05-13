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
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;

import java.util.Arrays;

/**
 * Util Class for handling Error in Ballerina VM.
 *
 * @since 0.995.0
 */
public class BallerinaErrors {
    
    public static final String ERROR_MESSAGE_FIELD = "message";

    public static ErrorValue createError(String reason, String detail) {
        MapValue<String, Object> detailMap = new MapValue<>();
        if (detail != null) {
            detailMap.put(ERROR_MESSAGE_FIELD, detail);
        }
        return new ErrorValue(reason, detailMap);
    }

    public static String getPrintableStackTrace(ErrorValue error) {
        return getPrintableStackTrace(getErrorMessage(error), error.getStackTrace());
    }

    public static String getPrintableStackTrace(String errorMsg, StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        sb.append(errorMsg).append("\n\tat ");
        // Append function/action/resource name with package path (if any)
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement stackFrame = stackTrace[i];
            String pkgName = stackFrame.getClassName();
            String fileName = stackFrame.getFileName();
            if (!fileName.equals(pkgName.concat(".bal"))) {
                sb.append(pkgName).append(":");
            }
            sb.append(stackFrame.getMethodName());
            // Append the filename
            sb.append("(").append(fileName);

            // Append the line number
            int lineNo = stackFrame.getLineNumber();
            if (lineNo > 0) {
                sb.append(":").append(lineNo);
            }
            sb.append(")");
            if (i != stackTrace.length - 1) {
                sb.append("\n\t   ");
            }
        }
        return sb.toString();
    }

    public static String getErrorMessage(ErrorValue errorValue) {
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
        StackTraceElement[] stackTrace = errorValue.getStackTrace();
        StackTraceElement[] stackWithoutJavaMain = Arrays.copyOf(stackTrace, stackTrace.length - 3);
        ErrorHandlerUtils.printError(
                "error: " + getPrintableStackTrace(getErrorMessage(errorValue), stackWithoutJavaMain));
    }
}
