package org.wso2.ballerinalang.compiler.semantics.model.types.util;

import java.math.BigDecimal;

import static java.math.MathContext.DECIMAL128;

public class Decimal extends BigDecimal {

    public static final Decimal ZERO = new Decimal("0");

    public Decimal(String val) {
        super(val, DECIMAL128);
    }

    public Decimal add(Decimal augend) {
        return new Decimal(super.add(augend, DECIMAL128).toString());
    }

    public Decimal subtract(Decimal subtrahend) {
        return new Decimal(super.subtract(subtrahend, DECIMAL128).toString());
    }

    public Decimal multiply(Decimal multiplicand) {
        return new Decimal(super.multiply(multiplicand, DECIMAL128).toString());
    }

    public Decimal divide(Decimal divisor) {
        return new Decimal(super.divide(divisor, DECIMAL128).toString());
    }

    public Decimal negate() {
        return new Decimal(super.negate().toString());
    }

    public Decimal reminder(Decimal divisor) {
        return new Decimal(super.remainder(divisor, DECIMAL128).toString());
    }
}
