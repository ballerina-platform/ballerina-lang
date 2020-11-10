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
package io.ballerina.runtime.util;

import io.ballerina.runtime.TypeConverter;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.ArrayValueImpl;
import io.ballerina.runtime.values.ErrorValue;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static io.ballerina.runtime.util.BLangConstants.BBYTE_MAX_VALUE;
import static io.ballerina.runtime.util.BLangConstants.BBYTE_MIN_VALUE;

/**
 * Util methods required for jBallerina runtime.
 *
 * @since 0.995.0
 */

public class RuntimeUtils {

    private static final String CRASH_LOGGER = "b7a.log.crash";
    private static final String DEFAULT_CRASH_LOG_FILE = "ballerina-internal.log";
    private static final String ENCODING_PATTERN = "\\$(\\d{4})";
    private static PrintStream errStream = System.err;
    public static final String USER_DIR = System.getProperty("user.dir");
    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static Logger crashLogger = Logger.getLogger(CRASH_LOGGER);

    private static final PrintStream stderr = System.err;
    private static FileHandler handler;

    /**
     * Used to handle rest args passed in to the main method.
     *
     * @param args args from main method
     * @param index starting index of var args
     * @param type array type
     * @return ArrayValue
     */
    public static ArrayValue createVarArgsArray(String[] args, int index, BArrayType type) {

        ArrayValue array = new ArrayValueImpl(type, type.getSize());
        for (int i = index; i < args.length; i++) {
            addToArray(type.getElementType(), args[i], array);
        }
        return array;
    }

    public static void addToArray(Type type, String value, ArrayValue array) {
        // TODO: need to add parsing logic for ref values for both var args and other args as well.
        switch (type.getTag()) {
            case TypeTags.STRING_TAG:
                array.add(array.size(), value);
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
                array.append((Object) value);
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
        int index = -1;

        public ParamInfo(boolean hasDefaultable, String name, Type type) {

            this.name = name;
            this.hasDefaultable = hasDefaultable;
            this.type = type;
        }
    }

    public static void handleRuntimeErrorsAndExit(Throwable throwable) {
        handleRuntimeErrors(throwable);
        Runtime.getRuntime().exit(1);
    }

    public static void handleRuntimeErrors(Throwable throwable) {
        if (throwable instanceof ErrorValue) {
            errStream.println("error: " + ((ErrorValue) throwable).getPrintableStackTrace());
        } else {
            // These errors are unhandled errors in JVM, hence logging them to bre log.
            errStream.println(BLangConstants.INTERNAL_ERROR_MESSAGE);
            silentlyLogBadSad(throwable);
        }
    }

    public static void handleRuntimeReturnValues(Object returnValue) {
        if (returnValue instanceof ErrorValue) {
            ErrorValue errorValue = (ErrorValue) returnValue;
            errStream.println("error: " + errorValue.getMessage() +
                    Optional.ofNullable(errorValue.getDetails()).map(details -> " " + details).orElse(""));
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

    public static void silentlyLogBadSad(Throwable throwable) {
        // These errors are unhandled errors in JVM, hence logging them to bre log.
        printCrashLog(throwable);
    }

    public static void printCrashLog(Throwable throwable) {
        // The fileHandle had to be created on demand, since the log file is getting created at the handler init.
        // Which result in empty ballerina-internal.log files always getting created.
        Level logLevel = Level.ALL;

        try {
            synchronized (crashLogger) {
                if (handler == null) {
                    handler = new FileHandler(initBRELogHandler(), true);
                    handler.setFormatter(new DefaultLogFormatter());
                    crashLogger.addHandler(handler);
                    crashLogger.setUseParentHandlers(false);
                    crashLogger.setLevel(logLevel);
                }
            }
        } catch (IOException ioException) {
            stderr.print("error initializing crash logger");
        }
        crashLogger.log(Level.SEVERE, throwable.getMessage(), throwable);
    }

    private static String initBRELogHandler() {
        String fileName = LogManager.getLogManager().getProperty(BLangConstants.DEFAULT_LOG_FILE_HANDLER_PATTERN);
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = DEFAULT_CRASH_LOG_FILE;
        }

        if (Files.isWritable(Paths.get(USER_DIR))) {
            return Paths.get(USER_DIR, fileName).toString();
        } else {
            return Paths.get(TEMP_DIR, fileName).toString();
        }
    }
}
