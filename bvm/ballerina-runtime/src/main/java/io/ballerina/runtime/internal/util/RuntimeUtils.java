/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.internal.util;

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.ErrorUtils;
import io.ballerina.runtime.internal.TypeConverter;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.MapValueImpl;

import java.io.PrintStream;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BBYTE_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BBYTE_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BLANG_SRC_FILE_SUFFIX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.INTERNAL_ERROR_MESSAGE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.MODULE_INIT_CLASS_NAME;

/**
 * Util methods required for jBallerina runtime.
 *
 * @since 0.995.0
 */

public class RuntimeUtils {

    private static final String CRASH_LOGGER = "b7a.log.crash";
    private static final PrintStream errStream = System.err;
    public static final String USER_DIR = System.getProperty("user.dir");
    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final Logger crashLogger = Logger.getLogger(CRASH_LOGGER);
    private static ConsoleHandler handler;

    /**
     * Used to handle rest args passed in to the main method.
     *
     * @param args args from main method
     * @param index starting index of var args
     * @param type array type
     * @return ArrayValue
     */
    public static ArrayValue createVarArgsArray(String[] args, int index, BArrayType type) {

        ArrayValue array = new ArrayValueImpl(type);
        for (int i = index; i < args.length; i++) {
            addToArray(type.getElementType(), args[i], array);
        }
        return array;
    }

    public static void addToArray(Type type, String value, ArrayValue array) {
        // TODO: need to add parsing logic for ref values for both var args and other args as well.
        switch (type.getTag()) {
            case TypeTags.STRING_TAG:
                array.add(array.size(), StringUtils.fromString(value));
                break;
            case TypeTags.INT_TAG:
                array.add(array.size(), (long) TypeConverter.convertValues(type, value));
                break;
            case TypeTags.FLOAT_TAG:
                array.add(array.size(), (double) TypeConverter.convertValues(type, value));
                break;
            case TypeTags.BOOLEAN_TAG:
                array.add(array.size(), (boolean) TypeConverter.convertValues(type, value));
                break;
            case TypeTags.BYTE_TAG:
                array.add(array.size(), (int) TypeConverter.convertValues(type, value));
                break;
            default:
                array.append(value);
        }
    }

    /**
     * Check a given int value is within ballerina byte value range.
     *
     * @param intValue integer value
     * @return true if within byte value range
     */
    public static boolean isByteLiteral(int intValue) {

        return (intValue >= BBYTE_MIN_VALUE && intValue <= BBYTE_MAX_VALUE);
    }

    /**
     * Keep a function parameter info, required for argument parsing.
     */
    public static class ParamInfo {
        String name;
        boolean hasDefaultable;
        Type type;

        public ParamInfo(boolean hasDefaultable, String name, Type type) {
            this.name = name;
            this.hasDefaultable = hasDefaultable;
            this.type = type;
        }
    }

    public static void handleBErrorAndExit(Throwable throwable) {
        if (throwable instanceof ErrorValue errorValue) {
            printToConsole(errorValue);
        }
        Runtime.getRuntime().exit(1);
    }

    public static void handleAllRuntimeErrorsAndExit(Throwable throwable) {
        handleAllRuntimeErrors(throwable);
        Runtime.getRuntime().exit(1);
    }

    public static void handleAllRuntimeErrors(Throwable throwable) {
        if (throwable instanceof ErrorValue errorValue) {
            printToConsole(errorValue);
        } else {
            logBadSad(throwable);
        }
    }

    private static void printToConsole(ErrorValue throwable) {
        errStream.println("error: " + throwable.getPrintableStackTrace());
    }

    public static void handleRuntimeReturnValues(Object returnValue) {
        if (returnValue instanceof ErrorValue errorValue) {
            errStream.println("error: " + errorValue.getPrintableError());
            Runtime.getRuntime().exit(1);
        }
    }

    public static void handleRuntimeErrorReturns(Object returnValue) {
        if (returnValue instanceof ErrorValue errorValue) {
            errStream.println("error: " + errorValue.getPrintableError());
        }
    }

    public static void handleDiagnosticErrors(RuntimeDiagnosticLog diagnosticLog) {
        diagnosticLog.getDiagnosticList().forEach(diagnostic -> errStream.println(diagnostic.toString()));
        if (diagnosticLog.getErrorCount() > 0) {
            Runtime.getRuntime().exit(1);
        }
    }

    public static void handleInvalidOption(String arg) {
        handleUsageError("value for option '--' (<String=String>) should be in KEY=VALUE format but was " + arg);
    }

    public static void handleInvalidConfig() {
        handleUsageError("value for option 'config' is missing");
    }

    public static void handleUsageError(String errorMsg) {
        errStream.println("ballerina: " + errorMsg);
        Runtime.getRuntime().exit(1);
    }

    public static void logBadSad(Throwable throwable) {
        // These errors are unhandled errors in JVM, hence logging them to bre log.
        errStream.println(INTERNAL_ERROR_MESSAGE);
        printCrashLog(throwable);
    }

    public static void printCrashLog(Throwable throwable) {
        // The fileHandle had to be created on demand, since the log file is getting created at the handler init.
        // Which result in empty ballerina-internal.log files always getting created.
        Level logLevel = Level.ALL;

        synchronized (crashLogger) {
            if (handler == null) {
                handler = new ConsoleHandler();
                handler.setFormatter(new DefaultLogFormatter());
                crashLogger.addHandler(handler);
                crashLogger.setUseParentHandlers(false);
                crashLogger.setLevel(logLevel);
            }
        }
        crashLogger.log(Level.SEVERE, throwable.getMessage(), throwable);
    }

    public static String getMajorVersion(String version) {
        return version.split("\\.")[0];
    }

    public static BMap<BString, Object> validateBMapValues(BMap<BString, Object> bMap) {
        if (bMap == null) {
            return new MapValueImpl<>();
        }
        for (Object value : bMap.values()) {
            if (isInvalidBallerinaValue(value)) {
                throw ErrorUtils.createJToBTypeCastError(value.getClass());
            }
        }
        return bMap;
    }

    public static Map<String, Object> validateBMapValues(Map<String, Object> bMap) {
        if (bMap == null) {
            return new MapValueImpl<>();
        }
        for (Object value : bMap.values()) {
            if (isInvalidBallerinaValue(value) && !(value instanceof String)) {
                throw ErrorUtils.createJToBTypeCastError(value.getClass());
            }
        }
        return bMap;
    }

    public static BMap<BString, Object> validateErrorDetails(BMap<BString, Object> details) {
        if (details == null) {
            return new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL);
        }
        for (Object value : details.values()) {
            if (isInvalidBallerinaValue(value)) {
                throw ErrorUtils.createJToBTypeCastError(value.getClass());
            }
        }
        return details;
    }

    private static boolean isInvalidBallerinaValue(Object value) {
        return (value != null && !(value instanceof Number) && !(value instanceof Boolean) &&
                !(value instanceof BValue));
    }

    public static String formatErrorMessage(Throwable e) {
        if (e instanceof BError error) {
            return error.getPrintableStackTrace();
        }
        return getPrintableStackTrace(e);
    }

    private static String getPrintableStackTrace(Throwable throwable) {
        String errorMsg = throwable.toString();
        StringBuilder sb = new StringBuilder();
        sb.append(errorMsg);
        // Append function/action/resource name with package path (if any)
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace.length == 0) {
            return sb.toString();
        }
        sb.append("\n\tat ");
        // print first element
        printStackElement(sb, stackTrace[0], "");
        for (int i = 1; i < stackTrace.length; i++) {
            printStackElement(sb, stackTrace[i], "\n\t   ");
        }
        return sb.toString();
    }

    private static void printStackElement(StringBuilder sb, StackTraceElement stackTraceElement, String tab) {
        String pkgName = Utils.decodeIdentifier(stackTraceElement.getClassName());
        String fileName = stackTraceElement.getFileName();
        if (fileName == null) {
            fileName = "unknown-source";
        }
        // clean file name from pkgName since we print the file name after the method name.
        fileName = fileName.replace(BLANG_SRC_FILE_SUFFIX, "");
        fileName = fileName.replace("/", "-");
        int index = pkgName.lastIndexOf("." + fileName);
        if (index != -1) {
            pkgName = pkgName.substring(0, index);
        }

        sb.append(tab);
        if (!pkgName.equals(MODULE_INIT_CLASS_NAME)) {
            sb.append(pkgName).append(":");
        }

        // Append the method name
        sb.append(Utils.decodeIdentifier(stackTraceElement.getMethodName()));
        // Append the filename
        sb.append("(").append(fileName);
        // Append the line number
        sb.append(":").append(stackTraceElement.getLineNumber()).append(")");
    }

    private RuntimeUtils() {
    }
}
