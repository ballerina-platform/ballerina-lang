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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.TypeConverter;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;

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
public class SimpleValueConvert extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context ctx) {
        BType targetType = ((BTypeDescValue) ctx.getNullableRefArgument(0)).value();
        BValue inputValue = ctx.getNullableRefArgument(1);
        if (inputValue == null) {
            ctx.setReturnValues(BLangVMErrors.createError(ctx.getStrand(), BallerinaErrorReasons.CONVERSION_ERROR,
                                                          BLangExceptionHelper
                                                                  .getErrorMessage(RuntimeErrors.CANNOT_CONVERT_NULL,
                                                                                   targetType)));
            return;
        }
        try {
            if (BTypes.isValueType(inputValue.getType())) {
                ctx.setReturnValues(convertValueTypes(targetType, inputValue));
                return;
            }
        } catch (RuntimeException e) {
            ctx.setReturnValues(BLangVMErrors
                                        .createError(ctx.getStrand(), BallerinaErrorReasons.CONVERSION_ERROR,
                                                     BLangExceptionHelper.getErrorMessage(
                                                             RuntimeErrors.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                                             inputValue.getType(), inputValue, targetType)));
            return;
        }
        // Todo: ToString required to be handle with different built in method since it is not covered by convert
        // function.
        try {
            if (targetType.getTag() == TypeTags.STRING_TAG) {
                ctx.setReturnValues(new BString(inputValue.stringValue()));
                return;
            }
            ctx.setReturnValues(BLangVMErrors.createError(ctx.getStrand(), BallerinaErrorReasons.CONVERSION_ERROR,
                                                          BLangExceptionHelper.getErrorMessage(
                                                                  RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION,
                                                                  inputValue.getType(), targetType)));
        } catch (RuntimeException e) {
            ctx.setReturnValues(BLangVMErrors
                                        .createError(ctx.getStrand(), BallerinaErrorReasons.CONVERSION_ERROR,
                                                     BLangExceptionHelper.getErrorMessage(
                                                             RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION,
                                                             inputValue.getType(), targetType)));
        }
    }

    public static Object simpleValueConvert(Strand strand, TypedescValue typedescValue, Object inputValue) {
        org.ballerinalang.jvm.types.BType targetType = typedescValue.getDescribingType();
        if (inputValue == null && targetType.getTag() != TypeTags.STRING_TAG) {
            return BallerinaErrors
                    .createError(org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.CONVERSION_ERROR,
                                 org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper
                                         .getErrorMessage(org.ballerinalang.jvm.util.exceptions.RuntimeErrors
                                                                  .CANNOT_CONVERT_NULL, targetType));
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
    
    private BValue convertValueTypes(BType targetType, BValue inputValue) {
        switch (targetType.getTag()) {
            case TypeTags.INT_TAG:
                return new BInteger(((BValueType) inputValue).intValue());
            case TypeTags.FLOAT_TAG:
                return new BFloat(((BValueType) inputValue).floatValue());
            case TypeTags.DECIMAL_TAG:
                return new BDecimal(((BValueType) inputValue).decimalValue());
            case TypeTags.STRING_TAG:
                return new BString(inputValue.stringValue());
            case TypeTags.BOOLEAN_TAG:
                return new BBoolean(((BValueType) inputValue).booleanValue());
            case TypeTags.BYTE_TAG:
                return new BByte(((BValueType) inputValue).byteValue());
            default:
                throw new BallerinaException(BallerinaErrorReasons.CONVERSION_ERROR,
                                             BLangExceptionHelper.getErrorMessage(
                                                     RuntimeErrors.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                                     inputValue.getType(), inputValue, targetType));
        }
    }
}
