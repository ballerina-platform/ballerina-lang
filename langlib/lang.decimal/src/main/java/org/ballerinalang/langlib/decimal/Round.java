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

        BigDecimal value = x.value();
        int scale = value.scale();
        if (fractionDigits > 0) {
            // scale is larger than current scale no change and will only add trailing zeros
            if (fractionDigits >= scale) {
                return x;
            }
        } else if (fractionDigits < 0) {
            int precision = value.precision();
            // negative scale is smaller than number of digits in integer-part result will be zero
            if (Math.abs(fractionDigits) > (precision - scale)) {
                BigDecimal scaledDecimal = new BigDecimal(0, MathContext.DECIMAL128);
                return ValueCreator.createDecimalValue(scaledDecimal);
            }
        }
        int toIntExact = Math.toIntExact(fractionDigits); // Now no under/overflow due to previous conditions
        BigDecimal scaledDecimal = value.setScale(toIntExact, RoundingMode.HALF_EVEN);
        return ValueCreator.createDecimalValue(scaledDecimal);
    }
}
