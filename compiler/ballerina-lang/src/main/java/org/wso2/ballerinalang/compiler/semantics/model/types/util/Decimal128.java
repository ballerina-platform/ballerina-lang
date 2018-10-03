package org.wso2.ballerinalang.compiler.semantics.model.types.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class Decimal128 extends BigDecimal {

    public Decimal128(char[] in, int offset, int len) {
        super(in, offset, len);
    }

    public Decimal128(char[] in, int offset, int len, MathContext mc) {
        super(in, offset, len, mc);
    }

    public Decimal128(char[] in) {
        super(in);
    }

    public Decimal128(char[] in, MathContext mc) {
        super(in, mc);
    }

    public Decimal128(String val) {
        super(val);
    }

    public Decimal128(String val, MathContext mc) {
        super(val, mc);
    }

    public Decimal128(double val) {
        super(val);
    }

    public Decimal128(double val, MathContext mc) {
        super(val, mc);
    }

    public Decimal128(BigInteger val) {
        super(val);
    }

    public Decimal128(BigInteger val, MathContext mc) {
        super(val, mc);
    }

    public Decimal128(BigInteger unscaledVal, int scale) {
        super(unscaledVal, scale);
    }

    public Decimal128(BigInteger unscaledVal, int scale, MathContext mc) {
        super(unscaledVal, scale, mc);
    }

    public Decimal128(int val) {
        super(val);
    }

    public Decimal128(int val, MathContext mc) {
        super(val, mc);
    }

    public Decimal128(long val) {
        super(val);
    }

    public Decimal128(long val, MathContext mc) {
        super(val, mc);
    }
}
