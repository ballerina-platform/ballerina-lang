/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.core.model.values;

import org.ballerinalang.core.model.types.BType;
import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.util.DecimalValueKind;
import org.ballerinalang.core.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.core.util.exceptions.BallerinaException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Map;


/**
 * The {@code BDecimal} represents a decimal value in Ballerina.
 *
 * @since 0.985.0
 */
public final class BDecimal extends BValueType implements BRefType<BigDecimal> {

    private static final BDecimal POSITIVE_INF =
            new BDecimal("9.999999999999999999999999999999999E6144", DecimalValueKind.POSITIVE_INFINITY);

    private static final BDecimal NEGATIVE_INF =
            new BDecimal("-9.999999999999999999999999999999999E6144", DecimalValueKind.NEGATIVE_INFINITY);

    private static final BDecimal NaN = new BDecimal("-1", DecimalValueKind.NOT_A_NUMBER);

    // Variable used to track the kind of a decimal value.
    public DecimalValueKind valueKind = DecimalValueKind.OTHER;

    private BigDecimal value;

    public BDecimal(BigDecimal value) {
        this.value = value;
        if (!this.booleanValue()) {
            this.valueKind = DecimalValueKind.ZERO;
        }
    }

    public BDecimal(String value) {
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

    public BDecimal(String value, DecimalValueKind valueKind) {
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

    @Override
    public BigDecimal decimalValue() {
        return this.value;
    }

    @Override
    public long intValue() {
        switch (valueKind) {
            case NOT_A_NUMBER:
                throw new BallerinaException(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                                             "'decimal' value '" + NaN + "' cannot be converted to 'int'");
            case NEGATIVE_INFINITY:
                throw new BallerinaException(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                                             "'decimal' value '" + NEGATIVE_INF + "' cannot be converted to 'int'");
            case POSITIVE_INFINITY:
                throw new BallerinaException(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                                             "'decimal' value '" + POSITIVE_INF + "' cannot be converted to 'int'");
        }

        return Math.round(value.doubleValue());
    }

    @Override
    public long byteValue() {
        switch (valueKind) {
            case NOT_A_NUMBER:
                throw new BallerinaException(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                                             "'decimal' value '" + NaN + "' cannot be converted to 'byte'");
            case NEGATIVE_INFINITY:
                throw new BallerinaException(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                                             "'decimal' value '" + NEGATIVE_INF + "' cannot be converted to 'byte'");
            case POSITIVE_INFINITY:
                throw new BallerinaException(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                                             "'decimal' value '" + POSITIVE_INF + "' cannot be converted to 'byte'");
        }

        return Math.round(this.value.doubleValue());
    }

    @Override
    public double floatValue() {
        if (this.valueKind == DecimalValueKind.NOT_A_NUMBER) {
            return Double.NaN;
        }
        return value.doubleValue();
    }

    @Override
    public boolean booleanValue() {
        return value.compareTo(BigDecimal.ZERO) != 0;
    }

    @Override
    public void setType(BType type) {

    }

    @Override
    public String stringValue() {
        if (this.valueKind != DecimalValueKind.OTHER) {
            return this.valueKind.getValue();
        }
        return value.toString();
    }

    @Override
    public BigDecimal value() {
        return this.value;
    }

    @Override
    public BType getType() {
        return BTypes.typeDecimal;
    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        return this;
    }

    //========================= Mathematical operations supported ===============================

    public BDecimal add(BDecimal augend) {
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
                    return new BDecimal(this.decimalValue().add(augend.decimalValue(), MathContext.DECIMAL128));
                }
                return augend;
        }
    }

    public BDecimal subtract(BDecimal subtrahend) {
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
                    return new BDecimal(this.decimalValue().subtract(subtrahend.decimalValue(),
                            MathContext.DECIMAL128));
                }
                return subtrahend.negate();
        }
    }

    public BDecimal multiply(BDecimal multiplicand) {
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
                    return new BDecimal(this.decimalValue().multiply(multiplicand.decimalValue(),
                            MathContext.DECIMAL128));
                }
                if (this.decimalValue().compareTo(BigDecimal.ZERO) > 0) {
                    return multiplicand;
                }
                return multiplicand.negate();
        }
    }

    public BDecimal divide(BDecimal divisor) {
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
                    return new BDecimal(this.decimalValue().divide(divisor.decimalValue(), MathContext.DECIMAL128));
                }
                if (divisor.valueKind == DecimalValueKind.POSITIVE_INFINITY ||
                        divisor.valueKind == DecimalValueKind.NEGATIVE_INFINITY) {
                    return new BDecimal(BigDecimal.ZERO);
                }
                if (divisor.valueKind == DecimalValueKind.NOT_A_NUMBER) {
                    return NaN;
                }
                return this.decimalValue().compareTo(BigDecimal.ZERO) > 0 ? POSITIVE_INF : NEGATIVE_INF;
        }
    }

    public BDecimal remainder(BDecimal divisor) {
        switch (this.valueKind) {
            case ZERO:
            case OTHER:
                if (divisor.valueKind == DecimalValueKind.OTHER) {
                    return new BDecimal(this.decimalValue().remainder(divisor.decimalValue(), MathContext.DECIMAL128));
                }
                if (divisor.valueKind == DecimalValueKind.ZERO || divisor.valueKind == DecimalValueKind.NOT_A_NUMBER) {
                    return NaN;
                }
                return this;
            default:
                return NaN;
        }
    }

    public BDecimal negate() {
        switch (this.valueKind) {
            case OTHER:
                return new BDecimal(this.decimalValue().negate());
            case POSITIVE_INFINITY:
                return NEGATIVE_INF;
            case NEGATIVE_INFINITY:
                return POSITIVE_INF;
            default:
                return this;
        }
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

        BDecimal bDecimal = (BDecimal) obj;
        return ((value.compareTo(bDecimal.value) == 0) && (this.valueKind == bDecimal.valueKind));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
