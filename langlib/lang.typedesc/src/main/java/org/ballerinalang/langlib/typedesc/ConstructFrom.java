/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.typedesc;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.JSONUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypedescType;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * Extern function lang.typedesc:constructFrom.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.typedesc", functionName = "constructFrom",
        args = {
                @Argument(name = "t", type = TypeKind.TYPEDESC),
                @Argument(name = "v", type = TypeKind.ANYDATA)
        },
        returnType = {
                @ReturnType(type = TypeKind.ANYDATA),
                @ReturnType(type = TypeKind.ERROR)
        },
        isPublic = true
)
public class ConstructFrom {

    public static Object constructFrom(Strand strand, TypedescValue t, Object v) {
        BType describingType = t.getDescribingType();
        // typedesc<json>.constructFrom like usage
        if (describingType.getTag() == org.ballerinalang.jvm.types.TypeTags.TYPEDESC_TAG) {
            return convert(((BTypedescType) t.getDescribingType()).getConstraint(), v);
        }
        // json.constructFrom like usage
        return convert(describingType, v);
    }

    public static Object convert(BType convertType, Object inputValue) {
        RefValue convertedValue;
        org.ballerinalang.jvm.types.BType targetType;
        if (convertType.getTag() == org.ballerinalang.jvm.types.TypeTags.UNION_TAG) {
            List<BType> memberTypes
                    = new ArrayList<>(((org.ballerinalang.jvm.types.BUnionType) convertType).getMemberTypes());
            targetType = new org.ballerinalang.jvm.types.BUnionType(memberTypes);

            Predicate<BType> errorPredicate = e -> e.getTag() == TypeTags.ERROR_TAG;
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
                    return XMLFactory.tableToXML((TableValue) inputValue);
                default:
                    break;
            }
        }
        return BallerinaErrors.createConversionError(inputValue, targetType);
    }
}
