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

    private static final int MAX_PRECISION = MathContext.DECIMAL128.getPrecision();
    // decimal128 minimum exponent is -6176
    private static final int MAX_SCALE = 6176;

    private Round() {
    }

    public static BDecimal round(BDecimal x, long fractionDigits) {

        BigDecimal value = x.value();
        int scale = value.scale();
        int precision = value.precision();
        long targetScale = fractionDigits;
        if (fractionDigits > scale) {
            // Cap the scale so the result fits in decimal128
            long maxScale = Math.min((long) scale + MAX_PRECISION - precision, MAX_SCALE);
            targetScale = Math.min(fractionDigits, Math.max(scale, maxScale));
        } else if (fractionDigits < 0 && fractionDigits < (long) scale - precision) {
            // Rounding above the most significant digit gives zero
            return ValueCreator.createDecimalValue(BigDecimal.ZERO);
        }
        BigDecimal scaledDecimal = value.setScale(Math.toIntExact(targetScale), RoundingMode.HALF_EVEN);
        if (scaledDecimal.signum() == 0) {
            // Zero result has exponent 0
            scaledDecimal = BigDecimal.ZERO;
        }
        return ValueCreator.createDecimalValue(scaledDecimal);
    }
}
