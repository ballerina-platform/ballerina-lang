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

package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.internal.DecimalValueKind;
import io.ballerina.runtime.internal.ErrorUtils;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.errors.ErrorReasons;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

/**
 * <p>
 * The {@link DecimalValue} represents a decimal value in Ballerina.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p> 
 * @since 0.995.0
 */
public class DecimalValue implements SimpleValue, BDecimal {

    private static final String INF_STRING = "Infinity";
    private static final String NEG_INF_STRING = "-" + INF_STRING;
    private static final String NAN = "NaN";
    private static final BigDecimal DECIMAL_MAX =
            new BigDecimal("9.999999999999999999999999999999999e6144", MathContext.DECIMAL128);
    private static final BigDecimal DECIMAL_MIN =
            new BigDecimal("-9.999999999999999999999999999999999e6144", MathContext.DECIMAL128);
    private static final BigDecimal MIN_DECIMAL_MAGNITUDE =
            new BigDecimal("1.000000000000000000000000000000000e-6143", MathContext.DECIMAL128);

    // Variable used to track the kind of a decimal value.
    @Deprecated
    public DecimalValueKind valueKind = DecimalValueKind.OTHER;

    private final BigDecimal value;

    public DecimalValue(BigDecimal value) {
        this.value = getValidDecimalValue(value);
        if (!this.booleanValue()) {
            this.valueKind = DecimalValueKind.ZERO;
        }
    }

    public DecimalValue(String value) {
        // Check whether the number provided is a hexadecimal value.
        BigDecimal bd;
        try {
            bd = new BigDecimal(value, MathContext.DECIMAL128);
        } catch (NumberFormatException exception) {
            String message = exception.getMessage();
            if ((message != null) && (message.equals("Too many nonzero exponent digits.") ||
                    message.equals("Exponent overflow."))) {
                throw ErrorCreator.createError(ErrorReasons.LARGE_EXPONENT_ERROR,
                        ErrorHelper.getErrorDetails(ErrorCodes.LARGE_EXPONENTS_IN_DECIMAL, value));
            }
            throw exception;
        }
        this.value = getValidDecimalValue(bd);

        if (!this.booleanValue()) {
            this.valueKind = DecimalValueKind.ZERO;
        }
    }

    public DecimalValue(String value, DecimalValueKind valueKind) {
        this(value);
        this.valueKind = valueKind;
    }

    private static BigDecimal getValidDecimalValue(BigDecimal bd) {
        if (bd.compareTo(DECIMAL_MAX) > 0 || bd.compareTo(DECIMAL_MIN) < 0) {
            throw ErrorCreator.createError(ErrorReasons.NUMBER_OVERFLOW,
                    ErrorHelper.getErrorDetails(ErrorCodes.DECIMAL_VALUE_OUT_OF_RANGE));
        } else if (bd.abs(MathContext.DECIMAL128).compareTo(MIN_DECIMAL_MAGNITUDE) < 0 &&
                bd.abs(MathContext.DECIMAL128).compareTo(BigDecimal.ZERO) > 0) {
            return BigDecimal.ZERO;
        }
        return bd;
    }

    /**
     * Get value of the decimal.
     * @return the value
     */
    public BigDecimal decimalValue() {
        return this.value;
    }

    /**
     * Get the int value of the decimal.
     * May result in a {@code ErrorValue}
     * @return the integer value
     */
    public long intValue() {

        if (!isDecimalWithinIntRange(this)) {
            throw ErrorUtils.createNumericConversionError(this.stringValue(null), PredefinedTypes.TYPE_DECIMAL,
                                                          PredefinedTypes.TYPE_INT);
        }
        return value.setScale(0, RoundingMode.HALF_EVEN).longValue();
    }

    /**
     * Check the given value is in int range.
     * @param decimalValue value to be checked
     * @return true if the value is in int range
     */
    public static boolean isDecimalWithinIntRange(DecimalValue decimalValue) {
        BigDecimal value = decimalValue.value;
        return value.compareTo(RuntimeConstants.BINT_MAX_VALUE_BIG_DECIMAL_RANGE_MAX) < 0 &&
               value.compareTo(RuntimeConstants.BINT_MIN_VALUE_BIG_DECIMAL_RANGE_MIN) > 0;
    }

    /**
     * Get the byte value.
     * May result in a {@code ErrorValue}
     * @return the byte value
     */
    public int byteValue() {

        int intVal = (int) Math.rint(this.value.doubleValue());
        if (!isByteLiteral(intVal)) {
            throw ErrorUtils.createNumericConversionError(value, PredefinedTypes.TYPE_DECIMAL,
                                                          PredefinedTypes.TYPE_BYTE);
        }
        return intVal;
    }

    private static boolean isByteLiteral(long longValue) {
        return (longValue >= RuntimeConstants.BBYTE_MIN_VALUE && longValue <= RuntimeConstants.BBYTE_MAX_VALUE);
    }

    /**
     * Get the float value.
     * @return the double value
     */
    public double floatValue() {
        return value.doubleValue();
    }

    /**
     * Check the given value represents true or false.
     * @return true if the value is non zero
     */
    public boolean booleanValue() {
        return value.compareTo(BigDecimal.ZERO) != 0;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return this;
    }

    /**
     * Get the string value.
     * @return string value
     * @param parent The link to the parent node
     */
    public String stringValue(BLink parent) {
        if (this.valueKind != DecimalValueKind.OTHER) {
            return this.valueKind.getValue();
        }
        return value.toString();
    }

    /**
     * Get the string value in expression style.
     * @return string value in expression style
     * @param parent The link to the parent node
     */
    public String expressionStringValue(BLink parent) {
        if (this.valueKind != DecimalValueKind.OTHER) {
            return this.valueKind.getValue() + "d";
        }
        return value.toString() + "d";
    }

    /**
     * Get the  {@code BigDecimal} value.
     * @return the decimal value
     */
    public BigDecimal value() {
        return this.value;
    }

    /**
     * Get the {@code BType} of the value.
     * @return the type
     */
    public Type getType() {
        return PredefinedTypes.TYPE_DECIMAL;
    }

    //========================= Mathematical operations supported ===============================

    /**
     * Returns a {decimal whose value is {@code (this + augend)}.
     * @param augend value to be added.
     * @return new value
     */
    public DecimalValue add(DecimalValue augend) {
        if (this.valueKind == DecimalValueKind.ZERO) {
            return augend;
        }
        if (augend.valueKind == DecimalValueKind.ZERO) {
            return this;
        }
        return new DecimalValue(this.decimalValue().add(augend.decimalValue(), MathContext.DECIMAL128));
    }

    /**
     * Returns a decimal whose value is {@code (this - subtrahend)}.
     * @param subtrahend value to be subtracted
     * @return value after subtraction
     */
    public DecimalValue subtract(DecimalValue subtrahend) {

        if (this.valueKind == DecimalValueKind.ZERO) {
            if (subtrahend.valueKind == DecimalValueKind.ZERO) {
                return subtrahend;
            }
            return subtrahend.negate();
        }
        if (subtrahend.valueKind == DecimalValueKind.ZERO) {
            return this;
        }
        return new DecimalValue(this.decimalValue().subtract(subtrahend.decimalValue(),
                MathContext.DECIMAL128));
    }

    /**
     * Returns a decimal whose value is <tt>(this &times;
     * multiplicand)</tt>.
     * @param multiplicand value to be multiplied
     * @return value after multiplication
     */
    public DecimalValue multiply(DecimalValue multiplicand) {

        if (this.valueKind == DecimalValueKind.ZERO) {
            return this;
        }
        if (multiplicand.valueKind == DecimalValueKind.OTHER) {
            return new DecimalValue(this.decimalValue().multiply(multiplicand.decimalValue(),
                    MathContext.DECIMAL128));
        }
        return multiplicand;
    }

    /**
     * Returns a decimal whose value is {@code (this /
     * divisor)}.
     * @param divisor value by which this decimal is to be divided
     * @return value after division
     */
    public DecimalValue divide(DecimalValue divisor) {

        if (this.valueKind == DecimalValueKind.ZERO) {
            if (divisor.valueKind == DecimalValueKind.ZERO) {
                throw ErrorUtils.createInvalidDecimalError(NAN);
            }
            return this;
        }
        if (divisor.valueKind == DecimalValueKind.OTHER) {
            return new DecimalValue(this.decimalValue().divide(divisor.decimalValue(), MathContext.DECIMAL128));
        }
        if (this.decimalValue().compareTo(BigDecimal.ZERO) > 0) {
            throw ErrorUtils.createInvalidDecimalError(INF_STRING);
        } else {
            throw ErrorUtils.createInvalidDecimalError(NEG_INF_STRING);
        }
    }

    /**
     * Returns a decimal whose value is {@code (this %
     * divisor)}.
     * @param divisor value by which this decimal is to be divided
     * @return {@code this % divisor}
     */
    public DecimalValue remainder(DecimalValue divisor) {
        if (divisor.valueKind == DecimalValueKind.OTHER) {
            return new DecimalValue(this.decimalValue().remainder(divisor.decimalValue(),
                    MathContext.DECIMAL128));
        }
        throw ErrorUtils.createInvalidDecimalError(NAN);
    }

    /**
     * Returns a decimal whose value is {@code (-this)}.
     * @return {@code -this}
     */
    public DecimalValue negate() {
        if (this.valueKind == DecimalValueKind.OTHER) {
            return new DecimalValue(this.decimalValue().negate());
        }
        return this;
    }

    @Override
    public BDecimal add(BDecimal augend) {
        return add((DecimalValue) augend);
    }

    @Override
    public BDecimal subtract(BDecimal subtrahend) {
        return subtract((DecimalValue) subtrahend);
    }

    @Override
    public BDecimal multiply(BDecimal multiplicand) {
        return multiply((DecimalValue) multiplicand);
    }

    @Override
    public BDecimal divide(BDecimal divisor) {
        return divide((DecimalValue) divisor);
    }

    @Override
    public BDecimal remainder(BDecimal divisor) {
        return remainder((DecimalValue) divisor);
    }

    /**
     * Returns value kind of {@code (-this)}.
     * @return value kind
     */
    public DecimalValueKind getValueKind() {
        return valueKind;
    }

    //===========================================================================================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        DecimalValue bDecimal = (DecimalValue) obj;
        return ((value.compareTo(bDecimal.value) == 0) && (this.valueKind == bDecimal.valueKind));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Get the string value.
     * @return string value
     */
    @Override
    public String toString() {
        return this.stringValue(null);
    }

    /**
     * Returns decimal of given int value.
     * @param value integer value
     * @return decimal value
     */
    public static DecimalValue valueOf(int value) {
        return new DecimalValue(new BigDecimal(value, MathContext.DECIMAL128).setScale(1, RoundingMode.HALF_EVEN));
    }

    /**
     * Returns decimal of given long value.
     * @param value long value
     * @return decimal value
     */
    public static DecimalValue valueOf(long value) {
        return new DecimalValue(new BigDecimal(value, MathContext.DECIMAL128).setScale(1, RoundingMode.HALF_EVEN));
    }

    /**
     * Returns decimal of given double value.
     * @param value double value
     * @return decimal value
     */
    public static DecimalValue valueOf(double value) {
        if (Double.isNaN(value)) {
            throw ErrorUtils.createInvalidDecimalError(NAN);
        }
        if (value == Double.POSITIVE_INFINITY) {
            throw ErrorUtils.createInvalidDecimalError(INF_STRING);
        }
        if (value == Double.NEGATIVE_INFINITY) {
            throw ErrorUtils.createInvalidDecimalError(NEG_INF_STRING);
        }
        return new DecimalValue(BigDecimal.valueOf(value));
    }

    /**
     * Returns decimal of given boolean value.
     * @param value boolean value
     * @return decimal value
     */
    public static DecimalValue valueOf(boolean value) {
        return new DecimalValue(value ? BigDecimal.ONE.setScale(1, BigDecimal.ROUND_HALF_EVEN) :
                                        BigDecimal.ZERO.setScale(1, BigDecimal.ROUND_HALF_EVEN));
    }

    public static DecimalValue valueOfJ(byte value) {
        return new DecimalValue(new BigDecimal(value, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_EVEN));
    }

    public static DecimalValue valueOfJ(char value) {
        return new DecimalValue(new BigDecimal(value, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_EVEN));
    }

    public static DecimalValue valueOfJ(short value) {
        return new DecimalValue(new BigDecimal(value, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_EVEN));
    }

    public static DecimalValue valueOfJ(int value) {
        return new DecimalValue(new BigDecimal(value, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_EVEN));
    }

    public static DecimalValue valueOfJ(long value) {
        return new DecimalValue(new BigDecimal(value, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_EVEN));
    }

    public static DecimalValue valueOfJ(float value) {
        return new DecimalValue(new BigDecimal(value, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_EVEN));
    }

    public static DecimalValue valueOfJ(double value) {
        return new DecimalValue(new BigDecimal(value, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_EVEN));
    }

    public static DecimalValue valueOfJ(BigDecimal value) {
        // TODO check whether we need to create a new BigDecimal again(or use the same value)
        return new DecimalValue(new BigDecimal(value.toString(), MathContext.DECIMAL128)
                .setScale(1, BigDecimal.ROUND_HALF_EVEN));
    }
}
