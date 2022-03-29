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

package org.ballerinalang.langlib.floatingpoint;

import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static io.ballerina.runtime.api.constants.RuntimeConstants.FLOAT_LANG_LIB;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.NEGATIVE_FRACTION_DIGITS;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Native implementation of lang.float:round(float, int).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.float", functionName = "round",
//        args = {@Argument(name = "x", type = TypeKind.FLOAT)},
//        returnType = {@ReturnType(type = TypeKind.FLOAT)},
//        isPublic = true
//)
public class Round {

    public static double round(double x, long fractionDigits) {
        if (isSpecialValue(x)) {
            return x;
        }
        if (fractionDigits < 0) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(FLOAT_LANG_LIB, NEGATIVE_FRACTION_DIGITS),
                    RuntimeErrors.FRACTION_DIGITS_NOT_NEGATIVE);
        }
        if (fractionDigits == 0) {
            return Math.rint(x);
        }
        if (fractionDigits > Integer.MAX_VALUE) {
            // As per IEEE, exponent of double value lies from -308 to 307.
            // Also, the maximum decimal digits that can have in double value are 15 or 16.
            // Therefore, if `fractionDigits` is very large, `x` will not be changed.
            return x;
        }
        // Down cast can be done safely because of above condition.
        int fractionDigitsAsInt = (int) fractionDigits;
        BigDecimal xInBigDecimal = BigDecimal.valueOf(x);
        BigDecimal xTmp = xInBigDecimal;
        int digitsTmp = fractionDigitsAsInt;
        while (digitsTmp-- > 0) {
            if (xTmp.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                return x;
            }
            xTmp = xTmp.multiply(BigDecimal.TEN);
        }
        return xInBigDecimal.setScale(fractionDigitsAsInt, RoundingMode.HALF_EVEN).doubleValue();
    }

    private static boolean isSpecialValue(double x) {
        return Double.isInfinite(x) || Double.isNaN(x) || x == 0.0d;
    }
}
