/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.api.values;

import io.ballerina.runtime.DecimalValueKind;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.values.DecimalValue;

import java.math.BigDecimal;

/**
 * <p>
 * The {@link BDecimal} represents a decimal value in Ballerina.
 * </p>
 *
 * @since 1.1.0
 */
public interface BDecimal {

    /**
     * Returns {@code BigDecimal} value.
     *
     * @return {@code BigDecimal} value
     */
    BigDecimal decimalValue();

    /**
     * Returns int value.
     * May result in a {@code ErrorValue}
     *
     * @return integer representation of the decimal
     */
    long intValue();

    /**
     * Returns the byte value.
     * May result in a {@code ErrorValue}
     *
     * @return the byte value
     */
    int byteValue();

    /**
     * Returns the float value.
     *
     * @return the double value
     */
    double floatValue();

    /**
     * Check the given value represents true or false.
     *
     * @return true if the value is non zero
     */
    boolean booleanValue();

    /**
     * Returns the  {@code BigDecimal} value.
     *
     * @return the decimal value
     */
    BigDecimal value();

    /**
     * Returns the {@code BType} of the value.
     *
     * @return the type
     */
    Type getType();


    /**
     * Returns a {decimal whose value is {@code (this + augend)}.
     *
     * @param augend value to be added.
     * @return new value
     */
    BDecimal add(BDecimal augend);

    /**
     * Returns a decimal whose value is {@code (this - subtrahend)}.
     *
     * @param subtrahend value to be subtracted
     * @return value after subtraction
     */
    BDecimal subtract(BDecimal subtrahend);

    /**
     * Returns a decimal whose value is <tt>(this &times;
     * multiplicand)</tt>.
     *
     * @param multiplicand value to be multiplied
     * @return value after multiplication
     */
    BDecimal multiply(BDecimal multiplicand);

    /**
     * Returns a decimal whose value is {@code (this /
     * divisor)}.
     *
     * @param divisor value by which this decimal is to be divided
     * @return value after division
     */
    BDecimal divide(BDecimal divisor);

    /**
     * Returns a decimal whose value is {@code (this %
     * divisor)}.
     *
     * @param divisor value by which this decimal is to be divided
     * @return {@code this % divisor}
     */
    BDecimal remainder(BDecimal divisor);

    /**
     * Returns a decimal whose value is {@code (-this)}.
     *
     * @return {@code -this}
     */
    BDecimal negate();

    /**
     * Returns value kind of {@code (-this)}.
     *
     * @return value kind
     */
    DecimalValueKind getValueKind();

    /**
     * Returns decimal of given int value.
     *
     * @param value integer value
     * @return decimal value
     */
    static BDecimal valueOf(int value) {
        return DecimalValue.valueOf(value);
    }

    /**
     * Returns decimal of given long value.
     *
     * @param value long value
     * @return decimal value
     */
    static BDecimal valueOf(long value) {
        return DecimalValue.valueOf(value);
    }

    /**
     * Returns decimal of given double value.
     *
     * @param value double value
     * @return decimal value
     */
    static BDecimal valueOf(double value) {
        return DecimalValue.valueOf(value);
    }

    /**
     * Returns decimal of given boolean value.
     *
     * @param value boolean value
     * @return decimal value
     */
    static BDecimal valueOf(boolean value) {
        return DecimalValue.valueOf(value);
    }
}
