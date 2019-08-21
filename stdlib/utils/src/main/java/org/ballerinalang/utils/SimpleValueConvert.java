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

package org.ballerinalang.utils;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.TypeConverter;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Perform deep modification of the clone of input value so it will be look like target type.
 *
 * @since 0.990.4
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "utils",
        functionName = "simpleValueConvert",
        args = {@Argument(name = "convertType", type = TypeKind.TYPEDESC),
                @Argument(name = "value", type = TypeKind.ANY)},
        returnType = { @ReturnType(type = TypeKind.ANYDATA), @ReturnType(type = TypeKind.ERROR) }
)
public class SimpleValueConvert {

    public static Object simpleValueConvert(Strand strand, TypedescValue typedescValue, Object inputValue) {
        org.ballerinalang.jvm.types.BType targetType = typedescValue.getDescribingType();
        if (inputValue == null && targetType.getTag() != TypeTags.STRING_TAG) {
            return BallerinaErrors
                    .createError(org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.CONVERSION_ERROR,
                                 org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper
                                         .getErrorMessage(org.ballerinalang.jvm.util.exceptions.RuntimeErrors
                                                                  .CANNOT_CONVERT_NIL, targetType));
        }
        try {
            if (org.ballerinalang.jvm.types.BTypes.isValueType(TypeChecker.getType(inputValue))) {
                return TypeConverter.convertValues(targetType, inputValue);
            }
        } catch (ErrorValue e) {
            return e;
        }
        // Todo: ToString required to be handle with different built in method since it is not covered by convert
        // function.
        try {
            if (targetType.getTag() == org.ballerinalang.jvm.types.TypeTags.STRING_TAG) {
                if (inputValue == null) {
                    return "()";
                }
                return ((RefValue) inputValue).stringValue();
            }
            return BallerinaErrors.createConversionError(inputValue, targetType);
        } catch (RuntimeException e) {
            return BallerinaErrors.createConversionError(inputValue, targetType);
        }
    }
}
