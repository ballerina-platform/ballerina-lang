/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons;

/**
 * Common utility methods used for arithmetic operations.
 *
 * @since 1.0
 */
public class MathUtils {

    private static final BString DIVIDE_BY_ZERO_ERROR = StringUtils.fromString(" / by zero");

    private static final BString INT_RANGE_OVERFLOW_ERROR = StringUtils.fromString(" int range overflow");

    private static void throwIntRangeOverflowError() {
        throw ErrorCreator.createError(BallerinaErrorReasons.NUMBER_OVERFLOW, INT_RANGE_OVERFLOW_ERROR);
    }

    public static long divide(long numerator, long denominator) {
        try {
            if (numerator == Long.MIN_VALUE && denominator == -1) {
                // a panic will occur on division by zero or overflow,
                // which happens if the first operand is -2^63 and the second operand is -1
                throwIntRangeOverflowError();
            }
            return numerator / denominator;
        } catch (ArithmeticException e) {
            if (denominator == 0) {
                throw ErrorCreator.createError(BallerinaErrorReasons.DIVISION_BY_ZERO_ERROR, DIVIDE_BY_ZERO_ERROR);
            } else {
                throw ErrorCreator.createError(BallerinaErrorReasons.ARITHMETIC_OPERATION_ERROR,
                                               StringUtils.fromString(e.getMessage()));
            }
        }
    }

    public static long remainder(long numerator, long denominator) {
        try {
            return numerator % denominator;
        } catch (ArithmeticException e) {
            if (denominator == 0) {
                throw ErrorCreator.createError(BallerinaErrorReasons.DIVISION_BY_ZERO_ERROR, DIVIDE_BY_ZERO_ERROR);
            } else {
                throw ErrorCreator.createError(BallerinaErrorReasons.ARITHMETIC_OPERATION_ERROR,
                                               StringUtils.fromString(e.getMessage()));
            }
        }
    }

    public static long addExact(long num1, long num2) {
        long result = num1 + num2;
        // an overflow has happened on addition if the sign of the result is different from BOTH operands' sign
        if (((num1 ^ result) & (num2 ^ result)) < 0) {
            throwIntRangeOverflowError();
        }
        return result;
    }

    public static long subtractExact(long num1, long num2) {
        long result = num1 - num2;
        // an overflow has happened on subtraction when both operands have the same sign AND
        // if the sign of the result is different from the first operand's sign
        if (((num1 ^ num2) & (num1 ^ result)) < 0) {
            throwIntRangeOverflowError();
        }
        return result;
    }

    public static long multiplyExact(long num1, long num2) {
        long result = num1 * num2;
        if ((num1 != 0 && result / num1 != num2) || (num2 != 0 && result / num2 != num1)) {
            throwIntRangeOverflowError();
        }
        return result;
    }
}
