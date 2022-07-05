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

/**
 * Native implementation of lang.float:toFixedString(float, int?).
 *
 * @since 2201.1.0
 */
public class ToFixedString {

    public static BString toFixedString(double x, Object fractionDigits) {
        // If `x` is NaN or infinite, the result will be the same as `value:toString`.
        // If fractionalDigits is `()`, use the minimum number of digits required to accurately represent the value.
        BString res = FloatUtils.getBStringIfInfiniteOrNaN(x);
        if (res != null) {
            return res;
        }

        if (fractionDigits == null) {
            return StringUtils.fromString(BigDecimal.valueOf(x).toPlainString());
        }

        long noOfFractionDigits = (long) fractionDigits;

        // Panic if noOfFractionDigits < 0.
        if (noOfFractionDigits < 0) {
            throw ErrorUtils.createInvalidFractionDigitsError();
        }

        // Handle very large int values since they might cause overflows.
        if (FloatUtils.checkFractionDigitsWithinRange(noOfFractionDigits)) {
            return StringUtils.fromString(BigDecimal.valueOf(x).toPlainString());
        }

        int fracDigits = (int) noOfFractionDigits;

        BigDecimal numberBigDecimal = new BigDecimal(x);
        numberBigDecimal  = numberBigDecimal.setScale(fracDigits, RoundingMode.HALF_EVEN);
        return StringUtils.fromString(numberBigDecimal.toPlainString());
    }
}
