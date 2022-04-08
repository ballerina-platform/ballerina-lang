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

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static io.ballerina.runtime.api.constants.RuntimeConstants.FLOAT_LANG_LIB;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Native implementation of lang.float:toFixedString(float, int?).
 *
 * @since 2.0.0
 */
public class ToFixedString {
    public static BString toFixedString(double x, Object fractionDigits) {
        // If `x` is NaN or infinite, the result will be the same as `value:toString`.
        // If fractionalDigits is `()`, use the minimum number of digits required to accurately represent the value.
        if (Double.isInfinite(x) || Double.isNaN(x) || fractionDigits == null) {
            return StringUtils.fromString(StringUtils.getStringValue(x, null));
        }

        long noOfFractionDigits = (long) fractionDigits;

        // Panic if noOfFractionDigits < 0.
        if (noOfFractionDigits < 0) {
            throw ErrorCreator.createError(getModulePrefixedReason(FLOAT_LANG_LIB,
                    BallerinaErrorReasons.INVALID_FRACTION_DIGITS_ERROR),
                    BLangExceptionHelper.getErrorDetails(RuntimeErrors.INVALID_FRACTION_DIGITS));
        }

        if (noOfFractionDigits > Integer.MAX_VALUE) {
            return StringUtils.fromString(StringUtils.getStringValue(x, null));
        }

        int fracDig = (int) noOfFractionDigits;

        BigDecimal numberBigDecimal = new BigDecimal(x);
        numberBigDecimal  = numberBigDecimal.setScale(fracDig, RoundingMode.HALF_EVEN);
        return StringUtils.fromString(numberBigDecimal.toPlainString());
    }
}
