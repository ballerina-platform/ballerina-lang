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
public class Round {

    // Maximum number of significant digits of a decimal128 value.
    private static final int MAX_PRECISION = MathContext.DECIMAL128.getPrecision();
    // Largest scale (i.e. negated smallest exponent) of a decimal128 value.
    private static final int MAX_SCALE = 6176;
    // Smallest scale (i.e. negated largest exponent) of a decimal128 value.
    private static final int MIN_SCALE = -6111;

    private Round() {
    }

    public static BDecimal round(BDecimal x, long fractionDigits) {

        BigDecimal value = x.value();
        int scale = value.scale();
        int precision = value.precision();
        long targetScale = fractionDigits;
        if (fractionDigits > scale) {
            // Increasing the scale only adds trailing zeros, so the result is exact. The exponent
            // `-fractionDigits` cannot be represented when it needs more digits than decimal128
            // allows, so use the largest scale that can be represented.
            long maxScale = Math.min((long) scale + MAX_PRECISION - precision, MAX_SCALE);
            targetScale = Math.min(fractionDigits, Math.max(scale, maxScale));
        } else if (fractionDigits < 0 && fractionDigits < (long) scale - precision) {
            // Rounding position is above the most significant digit, so the result is zero. Written
            // without negating fractionDigits, which would overflow for Long.MIN_VALUE.
            // The zero still gets exponent `-fractionDigits`, limited to what decimal128 can represent.
            int zeroScale = (int) Math.max(fractionDigits, MIN_SCALE);
            return ValueCreator.createDecimalValue(BigDecimal.ZERO.setScale(zeroScale));
        }
        // The checks above bound targetScale to the int range, so toIntExact cannot overflow.
        BigDecimal scaledDecimal = value.setScale(Math.toIntExact(targetScale), RoundingMode.HALF_EVEN);
        return ValueCreator.createDecimalValue(scaledDecimal);
    }
}
