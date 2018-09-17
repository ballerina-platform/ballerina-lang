/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.RuntimeErrors;

import java.util.IllegalFormatConversionException;

/**
 * Extern function ballerina/io#sprintf.
 *
 * @since 0.964.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "sprintf",
        args = {@Argument(name = "format", type = TypeKind.STRING),
                @Argument(name = "args", type = TypeKind.ARRAY)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)

/*
 * sprintf accept a format specifier and a list of arguments in an array and returns a formatted
 * string. Examples:
 *      sprintf("%s is awesome!", ["Ballerina"]) -> "Ballerina is awesome!"
 *      sprintf("%10.2f", [12.5678]) -> "     12.57"
 */
public class Sprintf extends BlockingNativeCallableUnit {
    @Override
    public final void execute(final Context context) {
        String format = context.getStringArgument(0);
        BRefValueArray args = (BRefValueArray) context.getRefArgument(0);
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
            if (format.charAt(i) == '%' && ((i + 1) < format.length())) {

                // skip % character
                j = i + 1;

                if (k >= args.size()) {
                    // there's not enough arguments
                    throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.NOT_ENOUGH_FORMAT_ARGUMENTS);
                }
                StringBuilder padding = new StringBuilder();
                while (Character.isDigit(format.charAt(j)) || format.charAt(j) == '.') {
                    padding.append(format.charAt(j));
                    j += 1;
                }
                try {
                    char formatSpecifier = format.charAt(j);
                    switch (formatSpecifier) {
                        case 'b':
                        case 'B':
                        case 'd':
                        case 'f':
                            result.append(String.format("%" + padding + formatSpecifier, args.get(k).value()));
                            break;
                        case 'x':
                        case 'X':
                            formatHexString(args, result, k, padding, formatSpecifier);
                            break;
                        case 's':
                            result.append(String.format("%" + padding + "s", args.get(k).stringValue()));
                            break;
                        case '%':
                            result.append("%");
                            break;
                        default:
                            // format string not supported
                            throw BLangExceptionHelper.getRuntimeException(
                                    RuntimeErrors.INVALID_FORMAT_SPECIFIER, format.charAt(j));
                    }
                } catch (IllegalFormatConversionException e) {
                    throw BLangExceptionHelper.getRuntimeException(
                            RuntimeErrors.ILLEGAL_FORMAT_CONVERSION, format.charAt(j) + " != " + args.get(k).getType());
                }
                if (format.charAt(j) == '%') {
                    // special case %%, don't count as a format specifier
                    i++;
                } else {
                    k++;
                    i = j;
                }
                continue;
            }
            // no match, copy and continue
            result.append(format.charAt(i));
        }
        context.setReturnValues(new BString(result.toString()));
    }

    private void formatHexString(BRefValueArray args, StringBuilder result, int k, StringBuilder padding, char x) {
        BRefType ref = args.get(k);
        if (TypeTags.ARRAY_TAG == ref.getType().getTag() &&
                TypeTags.BYTE_TAG == ((BArrayType) ref.getType()).getElementType().getTag()) {
            BByteArray byteArray = ((BByteArray) ref);
            for (int i = 0; i < byteArray.size(); i++) {
                result.append(String.format("%" + padding + x, byteArray.get(i)));
            }
        } else {
            result.append(String.format("%" + padding + x, args.get(k).value()));
        }
    }
}
