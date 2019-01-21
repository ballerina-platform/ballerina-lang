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

package org.ballerinalang.model.values;

import org.ballerinalang.bre.bvm.BVM;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.util.DecimalValueKind;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.math.BigDecimal;
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

    public BDecimal(String value, DecimalValueKind valueKind) {
        this.value = new BigDecimal(value, MathContext.DECIMAL128);
        this.valueKind = valueKind;
    }

    @Override
    public BigDecimal decimalValue() {
        return this.value;
    }

    @Override
    public long intValue() {
        if (this.valueKind == DecimalValueKind.NOT_A_NUMBER || !BVM.isDecimalWithinIntRange(value)) {
            throw new BallerinaException(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                    "out of range 'decimal' value '" + this.stringValue() + "' cannot be converted to 'int'");
        }
        return Math.round(value.doubleValue());
    }

    @Override
    public byte byteValue() {
        return value.byteValue();
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
