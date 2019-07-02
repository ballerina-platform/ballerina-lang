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
import org.ballerinalang.bre.bvm.BVM;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.JSONUtils;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * Perform deep modification of the clone of input value so it will be look like target type. 
 *
 * @since 0.990.4
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "utils",
        functionName = "convert",
        args = {@Argument(name = "convertType", type = TypeKind.TYPEDESC),
                @Argument(name = "value", type = TypeKind.ANY)},
        returnType = { @ReturnType(type = TypeKind.ANYDATA), @ReturnType(type = TypeKind.ERROR) }
)
public class Convert extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context ctx) {
        BType convertType = ((BTypeDescValue) ctx.getNullableRefArgument(0)).value();
        BValue inputValue = ctx.getNullableRefArgument(1);
        BValue convertedValue;
        BType targetType;
        if (convertType.getTag() == TypeTags.UNION_TAG) {
            List<BType> memberTypes = new ArrayList<>(((BUnionType) convertType).getMemberTypes());
            targetType = new BUnionType(memberTypes);

            Predicate<BType> errorPredicate = e -> e.getTag() == TypeTags.ERROR_TAG;
            ((BUnionType) targetType).getMemberTypes().removeIf(errorPredicate);

            if (((BUnionType) targetType).getMemberTypes().size() == 1) {
                targetType = ((BUnionType) convertType).getMemberTypes().get(0);
            }
        } else {
            targetType = convertType;
        }
        if (inputValue == null) {
            if (targetType.getTag() == TypeTags.JSON_TAG) {
                ctx.setReturnValues((BValue) null);
                return;
            }
            ctx.setReturnValues(BLangVMErrors.createError(ctx.getStrand(), BallerinaErrorReasons.CONVERSION_ERROR,
                                                          BLangExceptionHelper
                                                                  .getErrorMessage(RuntimeErrors.CANNOT_CONVERT_NULL,
                                                                                   convertType)));
            return;
        }
        if (!BVM.checkIsLikeType(inputValue, targetType)) {
            ctx.setReturnValues(BLangVMErrors.createError(ctx.getStrand(), BallerinaErrorReasons.CONVERSION_ERROR,
                                                          BLangExceptionHelper.getErrorMessage(
                                                                  RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION,
                                                                  inputValue.getType(), targetType)));
            return;
        }
        try {
            convertedValue = inputValue.copy(new HashMap<>());
            convertedValue.stamp(targetType, new ArrayList<>());
        } catch (BallerinaException e) {
            ctx.setReturnValues(BLangVMErrors.createError(ctx.getStrand(), BallerinaErrorReasons.CONVERSION_ERROR,
                                                          e.getDetail()));
            return;
        }
        ctx.setReturnValues(convertedValue);
    }

    public static Object convert(Strand strand, TypedescValue typeDescValue, Object inputValue) {
        org.ballerinalang.jvm.types.BType convertType = typeDescValue.getDescribingType();
        RefValue convertedValue;
        org.ballerinalang.jvm.types.BType targetType;
        if (convertType.getTag() == org.ballerinalang.jvm.types.TypeTags.UNION_TAG) {
            List<org.ballerinalang.jvm.types.BType> memberTypes
                    = new ArrayList<>(((org.ballerinalang.jvm.types.BUnionType) convertType).getMemberTypes());
            targetType = new org.ballerinalang.jvm.types.BUnionType(memberTypes);

            Predicate<org.ballerinalang.jvm.types.BType> errorPredicate = e -> e.getTag() == TypeTags.ERROR_TAG;
            ((org.ballerinalang.jvm.types.BUnionType) targetType).getMemberTypes().removeIf(errorPredicate);

            if (((org.ballerinalang.jvm.types.BUnionType) targetType).getMemberTypes().size() == 1) {
                targetType = ((org.ballerinalang.jvm.types.BUnionType) convertType).getMemberTypes().get(0);
            }
        } else {
            targetType = convertType;
        }

        if (inputValue == null) {
            if (targetType.getTag() == TypeTags.JSON_TAG) {
                return null;
            }
            return BallerinaErrors
                    .createError(org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.CONVERSION_ERROR,
                                 org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper
                                         .getErrorMessage(org.ballerinalang.jvm.util.exceptions.RuntimeErrors
                                                                  .CANNOT_CONVERT_NULL, convertType));
        }

        org.ballerinalang.jvm.types.BType inputValType = TypeChecker.getType(inputValue);
        if (TypeChecker.checkIsLikeType(inputValue, targetType)) {

            // if input value is a value-type, return as is.
            if (inputValType.getTag() <= TypeTags.BOOLEAN_TAG) {
                return inputValue;
            }

            try {
                RefValue refValue = (RefValue) inputValue;
                convertedValue = (RefValue) refValue.copy(new HashMap<>());
                convertedValue.stamp(targetType, new ArrayList<>());
                return convertedValue;
            } catch (org.ballerinalang.jvm.util.exceptions.BallerinaException e) {
                throw BallerinaErrors.createError(
                        org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.CONVERSION_ERROR, e.getDetail());
            }
        }

        if (inputValType.getTag() == TypeTags.TABLE_TAG) {
            switch (targetType.getTag()) {
                case TypeTags.JSON_TAG:
                    return JSONUtils.toJSON((TableValue) inputValue);
                case TypeTags.XML_TAG:
                    // TODO:
                default:
                    break;
            }
        }
        return BallerinaErrors.createConversionError(inputValue, targetType);
    }
}
