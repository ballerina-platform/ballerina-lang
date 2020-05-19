/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.log;

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.utils.StringUtils;
import org.ballerinalang.logging.util.BLogLevel;

import java.util.IllegalFormatConversionException;

/**
 * Native function implementations of the log-api module.
 *
 * @since 1.1.0
 */
public class Utils extends AbstractLogFunction {
    public static void printDebug(Object msg) {
        logMessage(Scheduler.getStrand(), msg, BLogLevel.DEBUG, getPackagePath(),
                (pkg, message) -> {
            getLogger(pkg).debug(message);
        });
    }

    public static void printError(Object msg, Object err) {
        logMessage(Scheduler.getStrand(), msg, BLogLevel.ERROR, getPackagePath(),
                (pkg, message) -> {
            String errorMsg = (err == null) ? "" : " : " + err.toString();
            getLogger(pkg).error(message + errorMsg);
        });
    }

    public static void printInfo(Object msg) {
        logMessage(Scheduler.getStrand(), msg, BLogLevel.INFO, getPackagePath(),
                (pkg, message) -> {
            getLogger(pkg).info(message);
        });
    }

    public static void printTrace(Object msg) {
        logMessage(Scheduler.getStrand(), msg, BLogLevel.TRACE, getPackagePath(),
                (pkg, message) -> {
            getLogger(pkg).trace(message);
        });
    }

    public static void printWarn(Object msg) {
        logMessage(Scheduler.getStrand(), msg, BLogLevel.WARN, getPackagePath(),
                (pkg, message) -> {
            getLogger(pkg).warn(message);
        });
    }

    public static void sprintDebug(BString format, Object... args) {
        logMessage(Scheduler.getStrand(), createFormattedString(format, args), BLogLevel.DEBUG, getPackagePath(),
                (pkg, message) -> {
            getLogger(pkg).debug(message);
        });
    }

    public static void sprintError(BString format, Object... args) {
        logMessage(Scheduler.getStrand(), createFormattedString(format, args), BLogLevel.ERROR, getPackagePath(),
                (pkg, message) -> {
            getLogger(pkg).error(message);
        });
    }

    public static void sprintInfo(BString format, Object... args) {
        logMessage(Scheduler.getStrand(), createFormattedString(format, args), BLogLevel.INFO, getPackagePath(),
                (pkg, message) -> {
            getLogger(pkg).info(message);
        });
    }

    public static void sprintTrace(BString format, Object... args) {
        logMessage(Scheduler.getStrand(), createFormattedString(format, args), BLogLevel.TRACE, getPackagePath(),
                (pkg, message) -> {
            getLogger(pkg).trace(message);
        });
    }

    public static void sprintWarn(BString format, Object... args) {
        logMessage(Scheduler.getStrand(), createFormattedString(format, args), BLogLevel.WARN, getPackagePath(),
                (pkg, message) -> {
            getLogger(pkg).warn(message);
        });
    }

    private static Object createFormattedString(BString format, Object... args) {
        StringBuilder result = new StringBuilder();

        /* Special chars in case additional formatting is required later
         *
         * Primitive built-in types (Same as Java String.format())
         * b            boolean
         * B            boolean (ALL_CAPS)
         * d            int
         * f            float
         * s            string
         * x            hex
         * X            HEX (ALL_CAPS)
         *
         * s            is applicable for any of the supported types in Ballerina. These values will be converted to
         * their string representation and displayed.
         */

        // i keeps index for checking %
        // j reads format specifier to apply
        // k records number of format specifiers seen so far, used to read respective array element
        for (int i = 0, j, k = 0; i < format.length(); i++) {
            if (StringUtils.getStringValue(format).charAt(i) == '%' && ((i + 1) < format.length())) {

                // skip % character
                j = i + 1;

                if (k >= args.length) {
                    // there's not enough arguments
                    throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.NOT_ENOUGH_FORMAT_ARGUMENTS);
                }
                StringBuilder padding = new StringBuilder();
                while (Character.isDigit(StringUtils.getStringValue(format).charAt(j)) ||
                        StringUtils.getStringValue(format).charAt(j) == '.') {
                    padding.append(StringUtils.getStringValue(format).charAt(j));
                    j += 1;
                }
                try {
                    char formatSpecifier = StringUtils.getStringValue(format).charAt(j);
                    Object ref = args[k];
                    switch (formatSpecifier) {
                        case 'b':
                        case 'B':
                        case 'd':
                        case 'f':
                            if (ref == null) {
                                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.ILLEGAL_FORMAT_CONVERSION,
                                        StringUtils.getStringValue(format).charAt(j) + " != ()");
                            }
                            result.append(String.format("%" + padding + formatSpecifier, ref));
                            break;
                        case 'x':
                        case 'X':
                            if (ref == null) {
                                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.ILLEGAL_FORMAT_CONVERSION,
                                        StringUtils.getStringValue(format).charAt(j) + " != ()");
                            }
                            formatHexString(result, k, padding, formatSpecifier, args);
                            break;
                        case 's':
                            if (ref != null) {
                                result.append(String.format("%" + padding + "s", StringUtils.getStringValue(ref)));
                            }
                            break;
                        case '%':
                            result.append("%");
                            break;
                        default:
                            // format string not supported
                            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INVALID_FORMAT_SPECIFIER,
                                    StringUtils.getStringValue(format).charAt(j));
                    }
                } catch (IllegalFormatConversionException e) {
                    throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.ILLEGAL_FORMAT_CONVERSION,
                            StringUtils.getStringValue(format).charAt(j) + " != " + TypeChecker.getType(args[k]));
                }
                if (StringUtils.getStringValue(format).charAt(j) == '%') {
                    // special case %%, don't count as a format specifier
                    i++;
                } else {
                    k++;
                    i = j;
                }
                continue;
            }
            // no match, copy and continue
            result.append(StringUtils.getStringValue(format).charAt(i));
        }
        return result.toString();
    }

    private static void formatHexString(StringBuilder result, int k, StringBuilder padding, char x, Object... args) {
        final Object argsValues = args[k];
        final BType type = TypeChecker.getType(argsValues);
        if (TypeTags.ARRAY_TAG == type.getTag() && TypeTags.BYTE_TAG == ((BArrayType) type).getElementType().getTag()) {
            ArrayValue byteArray = ((ArrayValue) argsValues);
            for (int i = 0; i < byteArray.size(); i++) {
                result.append(String.format("%" + padding + x, byteArray.getByte(i)));
            }
        } else {
            result.append(String.format("%" + padding + x, argsValues));
        }
    }
}
