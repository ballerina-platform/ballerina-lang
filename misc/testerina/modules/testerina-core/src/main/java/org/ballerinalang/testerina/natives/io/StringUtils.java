/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.testerina.natives.io;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;
import org.ballerinalang.test.runtime.util.TesterinaConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.IllegalFormatConversionException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * External function ballerina/test#sprintf.
 *
 * @since 2.0.0
 */
public class StringUtils {

    private static final String CHAR_PREFIX = "$";

    private StringUtils() {
    }

    public static Object matchWildcard(BString functionName, BString functionPattern) {
        try {
            return Pattern.matches(functionPattern.getValue().replace(TesterinaConstants.WILDCARD,
                    TesterinaConstants.DOT + TesterinaConstants.WILDCARD), functionName.getValue());
        } catch (PatternSyntaxException e) {
            return BLangExceptionHelper.getRuntimeException(
                    RuntimeErrors.OPERATION_NOT_SUPPORTED_ERROR, "Invalid wildcard pattern: " + e.getMessage());
        }
    }

    public static Object decode(BString str, BString charset) {
        try {
            String javaStr = str.getValue();
            javaStr = javaStr.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            javaStr = javaStr.replaceAll("\\+", "%2B");
            return io.ballerina.runtime.api.utils.StringUtils.fromString(
                    URLDecoder.decode(javaStr, charset.getValue()));
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            return BLangExceptionHelper.getRuntimeException(
                    RuntimeErrors.INCOMPATIBLE_ARGUMENTS, "Error while decoding: " + e.getMessage());
        }
    }

    public static BString sprintf(BString format, Object... args) {
        StringBuilder result = new StringBuilder();

        for (int i = 0, j, k = 0; i < format.length(); i++) {
            if (format.getValue().charAt(i) == '%' && ((i + 1) < format.length())) {

                // skip % character
                j = i + 1;

                if (k >= args.length) {
                    // there's not enough arguments
                    throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.NOT_ENOUGH_FORMAT_ARGUMENTS);
                }
                StringBuilder padding = new StringBuilder();
                while (Character.isDigit(format.getValue().charAt(j)) || format.getValue().charAt(j) == '.') {
                    padding.append(format.getValue().charAt(j));
                    j += 1;
                }
                try {
                    char formatSpecifier = format.getValue().charAt(j);
                    Object ref = args[k];
                    switch (formatSpecifier) {
                        case 'b':
                        case 'B':
                        case 'd':
                        case 'f':
                            if (ref == null) {
                                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.ILLEGAL_FORMAT_CONVERSION,
                                        format.getValue().charAt(j) + " != ()");
                            }
                            result.append(String.format("%" + padding + formatSpecifier, ref));
                            break;
                        case 'x':
                        case 'X':
                            if (ref == null) {
                                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.ILLEGAL_FORMAT_CONVERSION,
                                        format.getValue().charAt(j) + " != ()");
                            }
                            formatHexString(result, k, padding, formatSpecifier, args);
                            break;
                        case 's':
                            if (ref != null) {
                                result.append(String.format("%" + padding + "s",
                                        io.ballerina.runtime.api.utils.StringUtils.getStringValue(ref, null)));
                            }
                            break;
                        case '%':
                            result.append("%");
                            break;
                        default:
                            // format string not supported
                            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INVALID_FORMAT_SPECIFIER,
                                    format.getValue().charAt(j));
                    }
                } catch (IllegalFormatConversionException e) {
                    throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.ILLEGAL_FORMAT_CONVERSION,
                            format.getValue().charAt(j) + " != " +
                                    TypeChecker.getType(args[k]));
                }
                if (format.getValue().charAt(j) == '%') {
                    // special case %%, don't count as a format specifier
                    i++;
                } else {
                    k++;
                    i = j;
                }
                continue;
            }
            // no match, copy and continue
            result.append(format.getValue().charAt(i));
        }
        return io.ballerina.runtime.api.utils.StringUtils.fromString(result.toString());
    }

    private static void formatHexString(StringBuilder result, int k, StringBuilder padding, char x, Object... args) {
        final Object argsValues = args[k];
        final Type type = TypeChecker.getType(argsValues);
        if (TypeTags.ARRAY_TAG == type.getTag() && TypeTags.BYTE_TAG == ((ArrayType) type).getElementType().getTag()) {
            BArray byteArray = ((BArray) argsValues);
            for (int i = 0; i < byteArray.size(); i++) {
                result.append(String.format("%" + padding + x, byteArray.getByte(i)));
            }
        } else {
            result.append(String.format("%" + padding + x, argsValues));
        }
    }

    public static boolean isSystemConsole() {
        return System.console() != null;
    }
}
