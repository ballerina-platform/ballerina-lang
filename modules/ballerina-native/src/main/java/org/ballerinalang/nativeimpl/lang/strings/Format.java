/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.lang.strings;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.*;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.RuntimeErrors;

@BallerinaFunction(
        packageName = "ballerina.lang.strings",
        functionName = "format",
        args = {@Argument(name = "format", type = TypeEnum.STRING),
                @Argument(name= "args", type = TypeEnum.ARRAY)
        },
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Returns a formatted string using the specified format string and arguments") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "format",
        value = "format string") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "args",
        value = "list of arguments for the format string") })
public class Format extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String format = getStringArgument(context, 0);
        BRefValueArray args = (BRefValueArray) getRefArgument(context, 0);
        StringBuffer result = new StringBuffer();

        /* Special chars in case additional formatting is required later
         *
         * primitive built-in types
         * boolean         b
         * int             d
         * float           f
         * string          s
         *
         * complex built-in types
         * message         m
         * exception       e
         * map             p
         * xml             x
         * xmlDocument     X
         * json            j
         * datatable       t
         *
         * user defined types
         * struct          r
         * arrays          a
         */

        // i keeps index for checking %
        // j reads format specifier to apply
        // k records number of format specifiers seen so far, used to read respective array element
        for (int i = 0, j = 0, k = 0; i < format.length(); i++) {
            if (format.charAt(i) == '%' && ((i+1) < format.length())) {

                // skip % character
                j = i + 1;

                if (k >= args.size()) {
                    // there's not enough arguments
                    throw BLangExceptionHelper.getRuntimeException(
                            RuntimeErrors.NOT_ENOUGH_FORMAT_ARGUMENTS);
                }
                switch (format.charAt(j)) {
                    case 'b': result.append(String.format("%b", args.get(k).value()));          break;
                    case 'd': result.append(String.format("%d", args.get(k).value()));          break;
                    case 'f': result.append(String.format("%f", args.get(k).value()));          break;
                    case 'm':
                    case 's':
                    case 'e':
                    case 'p':
                    case 'x':
                    case 'X':
                    case 'j':
                    case 't':
                    case 'a': result.append(String.format("%s", args.get(k).stringValue()));    break;
                    case '%': result.append("%"); break;
                    default:
                        // format string not supported
                        throw BLangExceptionHelper.getRuntimeException(
                                RuntimeErrors.INVALID_FORMAT_SPECIFIER, format.charAt(j));
                }
                if (format.charAt(j) == '%') {
                    // special case %%, don't count as a format specifier
                    i++;
                } else {
                    k++;
                    i++;
                }
                continue;
            }
            // no match, copy and continue
            result.append(format.charAt(i));
        }

        return getBValues(new BString(result.toString()));
    }
}
