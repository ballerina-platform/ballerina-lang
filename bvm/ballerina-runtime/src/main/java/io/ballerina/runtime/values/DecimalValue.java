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

package io.ballerina.runtime.values;

import io.ballerina.runtime.DecimalValueKind;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.internal.ErrorUtils;
import io.ballerina.runtime.util.BLangConstants;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
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

    private static final DecimalValue POSITIVE_INF =
            new DecimalValue("9.999999999999999999999999999999999E6144", DecimalValueKind.POSITIVE_INFINITY);

    private static final DecimalValue NEGATIVE_INF =
            new DecimalValue("-9.999999999999999999999999999999999E6144", DecimalValueKind.NEGATIVE_INFINITY);

    private static final DecimalValue NaN = new DecimalValue("-1", DecimalValueKind.NOT_A_NUMBER);

    // Variable used to track the kind of a decimal value.
    @Deprecated
    public DecimalValueKind valueKind = DecimalValueKind.OTHER;

    private BigDecimal value;

    public DecimalValue(BigDecimal value) {
        this.value = value;
        if (!this.booleanValue()) {
            this.valueKind = DecimalValueKind.ZERO;
        }
    }

    public DecimalValue(String value) {
        // Check whether the number provided is a hexadecimal value.
        if (isHexValueString(value)) {
            this.value = hexToDecimalFloatingPointNumber(value);
        } else {
            this.value = new BigDecimal(value, MathContext.DECIMAL128);
        }
        if (!this.booleanValue()) {
            this.valueKind = DecimalValueKind.ZERO;
        }
    }

    public DecimalValue(String value, DecimalValueKind valueKind) {
        this(value);
        this.valueKind = valueKind;
    }

    private static boolean isHexValueString(String value) {
        String upperCaseValue = value.toUpperCase();
        return upperCaseValue.startsWith("0X") || upperCaseValue.startsWith("-0X");
    }

    /**
     * Method used to convert the hexadecimal number to decimal floating point number.
     * BigDecimal does not support hexadecimal numbers. Hence, we need to convert the hexadecimal number to a
     * decimal floating point number before passing the string value to the BigDecimal constructor.
     *
     * @param value Hexadecimal string value that needs to be converted.
     * @return BigDecimal corresponds to the hexadecimal number provided.
     */
    private static BigDecimal hexToDecimalFloatingPointNumber(String value) {
        String upperCaseValue = value.toUpperCase();
        // Remove the hexadecimal indicator prefix.
        String hexValue = upperCaseValue.replace("0X", "");
        if (!hexValue.contains("P")) {
            hexValue = hexValue.concat("P0");
        }
        // Isolate the binary exponent and the number.
        String[] splitAtExponent = hexValue.split("P");
        int binaryExponent = Integer.parseInt(splitAtExponent[1]);
        String numberWithoutExp = splitAtExponent[0];
        String intComponent;

        // Check whether the hex number has a decimal part.
        // If there is a decimal part, turn the hex floating point number to a whole number by multiplying it by a
        // power of 16.
        // i.e: 23FA2.123 = 23FA2123 * 16^(-3)
        if (numberWithoutExp.contains(".")) {
            String[] numberComponents = numberWithoutExp.split("\\.");
            intComponent = numberComponents[0];
            String decimalComponent = numberComponents[1];
            // Change the base of the hex power to 2 and calculate the binary exponent.
            // i.e: 23FA2123 * 16^(-3) = 23FA2123 * (2^4)^(-3) = 23FA2123 * 2^(-12)
            binaryExponent += 4 * (-1) * decimalComponent.length();
            intComponent = intComponent.concat(decimalComponent);
        } else {
            intComponent = numberWithoutExp;
        }

        BigDecimal exponentValue;
        // Find the value corresponding to the binary exponent.
        if (binaryExponent >= 0) {
            exponentValue = new BigDecimal(2).pow(binaryExponent);
        } else {
            //If negative exponent e, then the corresponding value equals to (1 / 2^(-e)).
            exponentValue = BigDecimal.ONE.divide(new BigDecimal(2).pow(-binaryExponent), MathContext.DECIMAL128);
        }
        // Convert the hexadecimal whole number(without exponent) to decimal big integer.
        BigInteger hexEquivalentNumber = new BigInteger(intComponent, 16);

        // Calculate and return the final decimal floating point number equivalent to the hex number provided.
        return new BigDecimal(hexEquivalentNumber).multiply(exponentValue, MathContext.DECIMAL128);
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
        switch (valueKind) {
            case NOT_A_NUMBER:
                throw ErrorUtils.createNumericConversionError(NaN, PredefinedTypes.TYPE_INT);
            case NEGATIVE_INFINITY:
                throw ErrorUtils.createNumericConversionError(NEGATIVE_INF, PredefinedTypes.TYPE_INT);
            case POSITIVE_INFINITY:
                throw ErrorUtils.createNumericConversionError(POSITIVE_INF, PredefinedTypes.TYPE_INT);
        }

        if (!isDecimalWithinIntRange(value)) {
            throw ErrorUtils.createNumericConversionError(this.stringValue(null), PredefinedTypes.TYPE_DECIMAL,
                                                          PredefinedTypes.TYPE_INT);
        }
        return (long) Math.rint(value.doubleValue());
    }

    /**
     * Check the given value is in int range.
     * @param decimalValue value to be checked
     * @return true if the value is in int range
     */
    public static boolean isDecimalWithinIntRange(BigDecimal decimalValue) {
        return decimalValue.compareTo(BLangConstants.BINT_MAX_VALUE_BIG_DECIMAL_RANGE_MAX) < 0 &&
               decimalValue.compareTo(BLangConstants.BINT_MIN_VALUE_BIG_DECIMAL_RANGE_MIN) > 0;
    }

    /**
     * Get the byte value.
     * May result in a {@code ErrorValue}
     * @return the byte value
     */
    public int byteValue() {
        switch (valueKind) {
            case NOT_A_NUMBER:
                throw ErrorUtils.createNumericConversionError(NaN, PredefinedTypes.TYPE_BYTE);
            case NEGATIVE_INFINITY:
                throw ErrorUtils.createNumericConversionError(NEGATIVE_INF, PredefinedTypes.TYPE_BYTE);
            case POSITIVE_INFINITY:
                throw ErrorUtils.createNumericConversionError(POSITIVE_INF, PredefinedTypes.TYPE_BYTE);
        }

        int intVal = (int) Math.rint(this.value.doubleValue());
        if (!isByteLiteral(intVal)) {
            throw ErrorUtils.createNumericConversionError(value, PredefinedTypes.TYPE_DECIMAL,
                                                          PredefinedTypes.TYPE_BYTE);
        }
        return intVal;
    }

    private static boolean isByteLiteral(long longValue) {
        return (longValue >= BLangConstants.BBYTE_MIN_VALUE && longValue <= BLangConstants.BBYTE_MAX_VALUE);
    }

    /**
     * Get the float value.
     * @return the double value
     */
    public double floatValue() {
        if (this.valueKind == DecimalValueKind.NOT_A_NUMBER) {
            return Double.NaN;
        }
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
        switch (this.valueKind) {
            case ZERO:
                return augend;
            case POSITIVE_INFINITY:
                if (augend.valueKind == DecimalValueKind.NEGATIVE_INFINITY ||
                        augend.valueKind == DecimalValueKind.NOT_A_NUMBER) {
                    return NaN;
                }
                return POSITIVE_INF;
            case NEGATIVE_INFINITY:
                if (augend.valueKind == DecimalValueKind.POSITIVE_INFINITY ||
                        augend.valueKind == DecimalValueKind.NOT_A_NUMBER) {
                    return NaN;
                }
                return NEGATIVE_INF;
            case NOT_A_NUMBER:
                return NaN;
            default:
                if (augend.valueKind == DecimalValueKind.ZERO) {
                    return this;
                }
                if (augend.valueKind == DecimalValueKind.OTHER) {
                    return new DecimalValue(this.decimalValue().add(augend.decimalValue(), MathContext.DECIMAL128));
                }
                return augend;
        }
    }

    /**
     * Returns a decimal whose value is {@code (this - subtrahend)}.
     * @param subtrahend value to be subtracted
     * @return value after subtraction
     */
    public DecimalValue subtract(DecimalValue subtrahend) {
        switch (this.valueKind) {
            case ZERO:
                if (subtrahend.valueKind == DecimalValueKind.ZERO ||
                        subtrahend.valueKind == DecimalValueKind.NOT_A_NUMBER) {
                    return subtrahend;
                }
                return subtrahend.negate();
            case POSITIVE_INFINITY:
                if (subtrahend.valueKind == DecimalValueKind.POSITIVE_INFINITY ||
                        subtrahend.valueKind == DecimalValueKind.NOT_A_NUMBER) {
                    return NaN;
                }
                return POSITIVE_INF;
            case NEGATIVE_INFINITY:
                if (subtrahend.valueKind == DecimalValueKind.NEGATIVE_INFINITY ||
                        subtrahend.valueKind == DecimalValueKind.NOT_A_NUMBER) {
                    return NaN;
                }
                return NEGATIVE_INF;
            case NOT_A_NUMBER:
                return NaN;
            default:
                if (subtrahend.valueKind == DecimalValueKind.ZERO) {
                    return this;
                }
                if (subtrahend.valueKind == DecimalValueKind.OTHER) {
                    return new DecimalValue(this.decimalValue().subtract(subtrahend.decimalValue(),
                            MathContext.DECIMAL128));
                }
                return subtrahend.negate();
        }
    }

    /**
     * Returns a decimal whose value is <tt>(this &times;
     * multiplicand)</tt>.
     * @param multiplicand value to be multiplied
     * @return value after multiplication
     */
    public DecimalValue multiply(DecimalValue multiplicand) {
        switch (this.valueKind) {
            case ZERO:
                if (multiplicand.valueKind == DecimalValueKind.ZERO ||
                        multiplicand.valueKind == DecimalValueKind.OTHER) {
                    return this;
                }
                return NaN;
            case POSITIVE_INFINITY:
                if (multiplicand.valueKind == DecimalValueKind.ZERO ||
                        multiplicand.valueKind == DecimalValueKind.NOT_A_NUMBER) {
                    return NaN;
                }
                if (multiplicand.decimalValue().compareTo(BigDecimal.ZERO) > 0) {
                    return POSITIVE_INF;
                }
                return NEGATIVE_INF;
            case NEGATIVE_INFINITY:
                if (multiplicand.valueKind == DecimalValueKind.ZERO ||
                        multiplicand.valueKind == DecimalValueKind.NOT_A_NUMBER) {
                    return NaN;
                }
                if (multiplicand.decimalValue().compareTo(BigDecimal.ZERO) > 0) {
                    return NEGATIVE_INF;
                }
                return POSITIVE_INF;
            case NOT_A_NUMBER:
                return NaN;
            default:
                if (multiplicand.valueKind == DecimalValueKind.OTHER) {
                    return new DecimalValue(this.decimalValue().multiply(multiplicand.decimalValue(),
                            MathContext.DECIMAL128));
                }
                if (this.decimalValue().compareTo(BigDecimal.ZERO) > 0) {
                    return multiplicand;
                }
                return multiplicand.negate();
        }
    }

    /**
     * Returns a decimal whose value is {@code (this /
     * divisor)}.
     * @param divisor value by which this decimal is to be divided
     * @return value after division
     */
    public DecimalValue divide(DecimalValue divisor) {
        switch (this.valueKind) {
            case ZERO:
                if (divisor.valueKind == DecimalValueKind.ZERO || divisor.valueKind == DecimalValueKind.NOT_A_NUMBER) {
                    return NaN;
                }
                return this;
            case POSITIVE_INFINITY:
                if (divisor.valueKind == DecimalValueKind.ZERO ||
                        (divisor.valueKind == DecimalValueKind.OTHER &&
                                divisor.decimalValue().compareTo(BigDecimal.ZERO) > 0)) {
                    return POSITIVE_INF;
                }
                if (divisor.valueKind == DecimalValueKind.OTHER &&
                        divisor.decimalValue().compareTo(BigDecimal.ZERO) < 0) {
                    return NEGATIVE_INF;
                }
                return NaN;
            case NEGATIVE_INFINITY:
                if (divisor.valueKind == DecimalValueKind.ZERO ||
                        (divisor.valueKind == DecimalValueKind.OTHER &&
                                divisor.decimalValue().compareTo(BigDecimal.ZERO) > 0)) {
                    return NEGATIVE_INF;
                }
                if (divisor.valueKind == DecimalValueKind.OTHER &&
                        divisor.decimalValue().compareTo(BigDecimal.ZERO) < 0) {
                    return POSITIVE_INF;
                }
                return NaN;
            case NOT_A_NUMBER:
                return NaN;
            default:
                if (divisor.valueKind == DecimalValueKind.OTHER) {
                    return new DecimalValue(this.decimalValue().divide(divisor.decimalValue(), MathContext.DECIMAL128));
                }
                if (divisor.valueKind == DecimalValueKind.POSITIVE_INFINITY ||
                        divisor.valueKind == DecimalValueKind.NEGATIVE_INFINITY) {
                    return new DecimalValue(BigDecimal.ZERO);
                }
                if (divisor.valueKind == DecimalValueKind.NOT_A_NUMBER) {
                    return NaN;
                }
                return this.decimalValue().compareTo(BigDecimal.ZERO) > 0 ? POSITIVE_INF : NEGATIVE_INF;
        }
    }

    /**
     * Returns a decimal whose value is {@code (this %
     * divisor)}.
     * @param divisor value by which this decimal is to be divided
     * @return {@code this % divisor}
     */
    public DecimalValue remainder(DecimalValue divisor) {
        switch (this.valueKind) {
            case ZERO:
            case OTHER:
                if (divisor.valueKind == DecimalValueKind.OTHER) {
                    return new DecimalValue(this.decimalValue().remainder(divisor.decimalValue(),
                                                                          MathContext.DECIMAL128));
                }
                if (divisor.valueKind == DecimalValueKind.ZERO || divisor.valueKind == DecimalValueKind.NOT_A_NUMBER) {
                    return NaN;
                }
                return this;
            default:
                return NaN;
        }
    }

    /**
     * Returns a decimal whose value is {@code (-this)}.
     * @return {@code -this}
     */
    public DecimalValue negate() {
        switch (this.valueKind) {
            case OTHER:
                return new DecimalValue(this.decimalValue().negate());
            case POSITIVE_INFINITY:
                return NEGATIVE_INF;
            case NEGATIVE_INFINITY:
                return POSITIVE_INF;
            default:
                return this;
        }
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
        return new DecimalValue(new BigDecimal(value, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_EVEN));
    }

    /**
     * Returns decimal of given long value.
     * @param value long value
     * @return decimal value
     */
    public static DecimalValue valueOf(long value) {
        return new DecimalValue(new BigDecimal(value, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_EVEN));
    }

    /**
     * Returns decimal of given double value.
     * @param value double value
     * @return decimal value
     */
    public static DecimalValue valueOf(double value) {
        return new DecimalValue(new BigDecimal(value, MathContext.DECIMAL128));
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
