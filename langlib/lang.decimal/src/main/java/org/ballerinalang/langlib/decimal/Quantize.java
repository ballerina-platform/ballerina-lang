/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Native implementation of lang.decimal:quantize(decimal, decimal).
 *
 * @since 2201.1.0
 */
public class Quantize {

    //  According to IEEE 754-2008 the maximum number of digits in the significand(precision) of
    //  128-bit decimal (radix 10) floating-point number equals 34.
    private static final int MAX_DIGITS_IN_SIGNIFICAND = 34;

    public static BDecimal quantize(BDecimal x, BDecimal y) {
        BigDecimal quantizeValue = x.value().setScale(y.value().scale(), RoundingMode.HALF_EVEN);
        if (quantizeValue.precision() > MAX_DIGITS_IN_SIGNIFICAND) {
            throw ErrorCreator.createError(BallerinaErrorReasons.QUANTIZE_ERROR);
        }
        return ValueCreator.createDecimalValue(quantizeValue);
    }
}
