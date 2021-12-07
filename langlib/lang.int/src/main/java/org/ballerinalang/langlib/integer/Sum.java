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

package org.ballerinalang.langlib.integer;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import static io.ballerina.runtime.api.constants.RuntimeConstants.INT_LANG_LIB;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Native implementation of lang.int:sum(int...).
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.int", functionName = "sum",
//        args = {@Argument(name = "ns", type = TypeKind.ARRAY)},
//        returnType = {@ReturnType(type = TypeKind.INT)},
//        isPublic = true
//)
public class Sum {

    public static long sum(long[] ns) {
        long sum = 0;
        for (long n : ns) {
            try {
                sum = Math.addExact(sum, n);
            } catch (ArithmeticException e) {
                throw ErrorCreator.createError(getModulePrefixedReason(INT_LANG_LIB,
                        BallerinaErrorReasons.NUMBER_OVERFLOW_ERROR_IDENTIFIER),
                        BLangExceptionHelper.getErrorDetails(RuntimeErrors.INT_RANGE_OVERFLOW_ERROR));
            }
        }
        return sum;
    }
}
