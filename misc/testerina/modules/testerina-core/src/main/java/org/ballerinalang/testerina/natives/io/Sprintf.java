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

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.util.exceptions.RuntimeErrors;

import java.util.IllegalFormatConversionException;

/**
 * External function ballerina/test#sprintf.
 *
 * @since 2.0.0
 */
public class Sprintf {

    private Sprintf() {
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
                                                            StringUtils.getStringValue(ref, null)));
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
        return StringUtils.fromString(result.toString());
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
}
