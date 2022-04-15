/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.langlib.floatingpoint;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.ErrorUtils;
import io.ballerina.runtime.internal.FloatUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Native implementation of lang.float:toExpString(float, int?).
 *
 * @since 2201.1.0
 */
public class ToExpString {

    public static BString toExpString(double x, Object fractionDigits) {
        // If `x` is NaN or infinite, the result will be the same as `value:toString`.
        BString str = FloatUtils.getBStringIfInfiniteOrNaN(x);
        if (str != null) {
            return str;
        }

        long noOfFractionDigits;

        // If fractionalDigits is `()`, use the minimum number of digits required to accurately represent the value.
        double xAbsValue = Math.abs(x);
        if (fractionDigits == null) {
            if (xAbsValue == 0) {
                noOfFractionDigits = 1;
            } else {
                int pow;
                BigDecimal xInBigDecimal = BigDecimal.valueOf(x);
                if (xAbsValue < 1) {
                    pow = calculatePowerForFloatLessThanOne(xAbsValue);
                    noOfFractionDigits = xInBigDecimal.scaleByPowerOfTen(pow).scale();
                } else {
                    pow = calculatePowerForFloatGreaterThanOrEqualToOne(xAbsValue);
                    noOfFractionDigits = xInBigDecimal.scale() + pow;
                }
            }
        } else {
            noOfFractionDigits = (long) fractionDigits;
        }

        // Panic if noOfFractionDigits < 0.
        if (noOfFractionDigits < 0) {
            throw ErrorUtils.createInvalidFractionDigitsError();
        }

        // Handle very large int values since they might cause overflows.
        if (FloatUtils.checkFractionDigitsWithinRange(noOfFractionDigits)) {
            // Maximum value a double can represent is around 1.7*10^308.
            noOfFractionDigits = 308;
        }

        int exponent = (int) noOfFractionDigits;

        int tens = 0;
        if (xAbsValue != 0 && xAbsValue < 1) {
            tens = calculatePowerForFloatLessThanOne(xAbsValue);
        }

        BigDecimal numberBigDecimal = new BigDecimal(x);
        if (xAbsValue != 0 && xAbsValue < 1) {
            numberBigDecimal  = numberBigDecimal.setScale(exponent + tens, RoundingMode.HALF_EVEN);
        } else {
            numberBigDecimal  = numberBigDecimal.setScale(exponent, RoundingMode.HALF_EVEN);
        }

        return getScientificNotation(numberBigDecimal, exponent);
    }

    public static int calculatePowerForFloatLessThanOne(double xAbsValue) {
        int pow = 0;
        while (xAbsValue < 1) {
            xAbsValue = xAbsValue * Math.pow(10, 1);
            pow++;
        }
        return pow;
    }

    public static int calculatePowerForFloatGreaterThanOrEqualToOne(double xAbsValue) {
        int pow = 0;
        while (xAbsValue > 10) {
            xAbsValue = xAbsValue / Math.pow(10, 1);
            pow++;
        }
        return pow;
    }

    public static BString getScientificNotation(BigDecimal numberBigDecimal, int exp) {
        String power = "0".repeat(exp);
        DecimalFormat decimalFormat = new DecimalFormat("0." + power + "E0");
        String res = decimalFormat.format(numberBigDecimal).toLowerCase();

        int indexOfExp = res.lastIndexOf("e");
        String coefficient = res.substring(0, indexOfExp);
        int idxOfDecimalPoint = coefficient.lastIndexOf(".");
        // If there are no digits following the decimal point, remove the decimal point from the string.
        if (idxOfDecimalPoint == coefficient.length() - 1) {
            coefficient = res.substring(0, idxOfDecimalPoint);
        }

        String exponent = res.substring(indexOfExp + 1);
        int p = Integer.parseInt(exponent);
        if (p >= 0) {
            exponent = "e+" + exponent;
        } else {
            exponent = "e" + exponent;
        }

        String notation = coefficient + exponent;

        return StringUtils.fromString(notation);
    }
}
