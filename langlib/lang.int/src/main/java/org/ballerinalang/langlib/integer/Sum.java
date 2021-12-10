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

import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.MathUtils;
import io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons;

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
        BString errorMsg = getModulePrefixedReason(INT_LANG_LIB,
                BallerinaErrorReasons.NUMBER_OVERFLOW_ERROR_IDENTIFIER);
        for (long n : ns) {
            sum = MathUtils.addExact(sum, n, errorMsg);
        }
        return sum;
    }
}
