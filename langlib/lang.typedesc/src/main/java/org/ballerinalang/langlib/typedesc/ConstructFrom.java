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
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private static final String AMBIGUOUS_TARGET = "ambiguous target type";

    public static Object constructFrom(Strand strand, TypedescValue t, Object v) {
        BType describingType = t.getDescribingType();
        // typedesc<json>.constructFrom like usage
        if (describingType.getTag() == TypeTags.TYPEDESC_TAG) {
            return convert(((BTypedescType) t.getDescribingType()).getConstraint(), v);
        }
        // json.constructFrom like usage
        return convert(describingType, v);
    }

    public static Object convert(BType convertType, Object inputValue) {
        if (inputValue == null) {
            if (convertType.isNilable()) {
                return null;
            }
            return BallerinaErrors
                    .createError(org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.CONVERSION_ERROR,
                                 org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper
                                         .getErrorMessage(org.ballerinalang.jvm.util.exceptions.RuntimeErrors
                                                                  .CANNOT_CONVERT_NIL, convertType));
        }

        BType inputValType = TypeChecker.getType(inputValue);

        List<BType> convertibleTypes = getConvertibleTypes(inputValue, convertType);

        if (convertibleTypes.size() == 0) {
            // This would not work when the target is a union, but this is OK since table to JSON/XML conversion
            // uses this method temporarily.
            if (inputValType.getTag() == TypeTags.TABLE_TAG) {
                switch (convertType.getTag()) {
                    case TypeTags.JSON_TAG:
                        return JSONUtils.toJSON((TableValue) inputValue);
                    case TypeTags.XML_TAG:
                        return XMLFactory.tableToXML((TableValue) inputValue);
                    default:
                        break;
                }
            }

            return BallerinaErrors.createConversionError(inputValue, convertType);
        } else if (convertibleTypes.size() > 1) {
            return BallerinaErrors.createConversionError(inputValue, convertType, AMBIGUOUS_TARGET);
        }

        // if input value is a value-type, return as is.
        if (inputValType.getTag() < TypeTags.JSON_TAG) {
            return inputValue;
        }

        try {
            RefValue refValue = (RefValue) inputValue;
            RefValue convertedValue = (RefValue) refValue.copy(new HashMap<>());
            convertedValue.stamp(convertibleTypes.get(0), new ArrayList<>());
            return convertedValue;
        } catch (org.ballerinalang.jvm.util.exceptions.BallerinaException e) {
            return BallerinaErrors.createError(
                    org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.CONVERSION_ERROR, e.getDetail());
        }
    }

    private static List<BType> getConvertibleTypes(Object inputValue, BType targetType) {
        List<BType> convertibleTypes = new ArrayList<>();

        int targetTypeTag = targetType.getTag();

        switch (targetTypeTag) {
            case TypeTags.UNION_TAG:
                for (BType memType : ((BUnionType) targetType).getMemberTypes()) {
                    convertibleTypes.addAll(getConvertibleTypes(inputValue, memType));
                }
                break;
            case TypeTags.RECORD_TYPE_TAG:
                // TODO: 8/13/19 impl against def value
            default:
                if (TypeChecker.checkIsLikeType(inputValue, targetType)) {
                    convertibleTypes.add(targetType);
                }
        }
        return convertibleTypes;
    }
}
