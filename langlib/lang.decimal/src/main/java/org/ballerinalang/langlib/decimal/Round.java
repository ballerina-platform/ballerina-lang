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

package org.ballerinalang.langlib.decimal;

import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BDecimal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Native implementation of lang.decimal:round(decimal, int).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.decimal", functionName = "round",
//        args = {@Argument(name = "x", type = TypeKind.DECIMAL)},
//        returnType = {@ReturnType(type = TypeKind.DECIMAL)},
//        isPublic = true
//)
public class Round {
    public static BDecimal round(BDecimal x, long fractionDigits) {
        // Ballerina uses MathContext.DECIMAL128 format and precision is 34
//        BigDecimal newDecimal = new BigDecimal(x.value().doubleValue(), MathContext.DECIMAL128);
        BigDecimal value = x.value();
        BigDecimal scaledDecimal;
        if (fractionDigits >= Integer.MAX_VALUE) {
            scaledDecimal = x.value().setScale(Integer.MAX_VALUE, RoundingMode.HALF_EVEN);
        } else if (fractionDigits <= Integer.MIN_VALUE) {
            scaledDecimal = x.value().setScale(Integer.MIN_VALUE, RoundingMode.HALF_EVEN);
        } else if (fractionDigits == 0) {
            if (value.compareTo(RuntimeConstants.BINT_MAX_VALUE_BIG_DECIMAL_RANGE_MAX) < 0 &&
                    value.compareTo(RuntimeConstants.BINT_MIN_VALUE_BIG_DECIMAL_RANGE_MIN) > 0) {
                long intValue = x.intValue();
                scaledDecimal = new BigDecimal(intValue, MathContext.DECIMAL128);
            } else {
                scaledDecimal = x.value().setScale(0, RoundingMode.HALF_EVEN);
            }
        } else {
            int toIntExact = Math.toIntExact(fractionDigits); // assume no integer overflow due to
            // previous conditions
            scaledDecimal = x.value().setScale(toIntExact, RoundingMode.HALF_EVEN);
        }
        return ValueCreator.createDecimalValue(scaledDecimal);
    }
}
