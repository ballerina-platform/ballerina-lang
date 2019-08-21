/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.utils;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Perform deep modification of the value so it will be look like target type.
 *
 * @since 0.990.4
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "utils",
        functionName = "stamp",
        args = {@Argument(name = "convertType", type = TypeKind.TYPEDESC),
                @Argument(name = "value", type = TypeKind.ANY)},
        returnType = { @ReturnType(type = TypeKind.ANYDATA), @ReturnType(type = TypeKind.ERROR) }
)
public class Stamp {

    public static Object stamp(Strand strand, TypedescValue typedescValue, Object valueToBeStamped) {
        org.ballerinalang.jvm.types.BType stampType = typedescValue.getDescribingType();
        org.ballerinalang.jvm.types.BType targetType;
        if (stampType.getTag() ==  org.ballerinalang.jvm.types.TypeTags.UNION_TAG) {
            List<org.ballerinalang.jvm.types.BType> memberTypes
                    = new ArrayList<>(((org.ballerinalang.jvm.types.BUnionType) stampType).getMemberTypes());
            targetType = new org.ballerinalang.jvm.types.BUnionType(memberTypes);

            Predicate<org.ballerinalang.jvm.types.BType> errorPredicate = e -> e.getTag() == TypeTags.ERROR_TAG;
            ((org.ballerinalang.jvm.types.BUnionType) targetType).getMemberTypes().removeIf(errorPredicate);

            if (((org.ballerinalang.jvm.types.BUnionType) targetType).getMemberTypes().size() == 1) {
                targetType = ((org.ballerinalang.jvm.types.BUnionType) stampType).getMemberTypes().get(0);
            }
        } else {
            targetType = stampType;
        }

        if (valueToBeStamped == null) {
            if (targetType.getTag() == TypeTags.JSON_TAG) {
                return null;
            }
            return BallerinaErrors
                    .createError(org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.STAMP_ERROR,
                                 org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper
                                         .getErrorMessage(org.ballerinalang.jvm.util.exceptions.RuntimeErrors
                                                                  .CANNOT_STAMP_NULL, stampType));
        }
        if (!TypeChecker.checkIsLikeType(valueToBeStamped, targetType)) {
            return BallerinaErrors
                    .createError(org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.STAMP_ERROR,
                                 org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper
                                         .getErrorMessage(org.ballerinalang.jvm.util.exceptions.RuntimeErrors
                                                                  .INCOMPATIBLE_STAMP_OPERATION,
                                                          TypeChecker.getType(valueToBeStamped), targetType));
        }
        try {
            if (valueToBeStamped instanceof RefValue) {
                ((RefValue) valueToBeStamped).stamp(targetType, new ArrayList<>());
            }
        } catch (org.ballerinalang.jvm.util.exceptions.BallerinaException e) {
            throw BallerinaErrors.createError(org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.STAMP_ERROR,
                                               e.getDetail());
        }
        return valueToBeStamped;
    }
}
