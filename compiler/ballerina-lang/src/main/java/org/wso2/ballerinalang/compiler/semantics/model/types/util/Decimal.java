package org.wso2.ballerinalang.compiler.semantics.model.types.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class Decimal extends BigDecimal {

    public Decimal(char[] in, int offset, int len) {
        super(in, offset, len);
    }

    public Decimal(char[] in, int offset, int len, MathContext mc) {
        super(in, offset, len, mc);
    }

    public Decimal(char[] in) {
        super(in);
    }

    public Decimal(char[] in, MathContext mc) {
        super(in, mc);
    }

    public Decimal(String val) {
        super(val);
    }

    public Decimal(String val, MathContext mc) {
        super(val, mc);
    }

    public Decimal(double val) {
        super(val);
    }

    public Decimal(double val, MathContext mc) {
        super(val, mc);
    }

    public Decimal(BigInteger val) {
        super(val);
    }

    public Decimal(BigInteger val, MathContext mc) {
        super(val, mc);
    }

    public Decimal(BigInteger unscaledVal, int scale) {
        super(unscaledVal, scale);
    }

    public Decimal(BigInteger unscaledVal, int scale, MathContext mc) {
        super(unscaledVal, scale, mc);
    }

    public Decimal(int val) {
        super(val);
    }

    public Decimal(int val, MathContext mc) {
        super(val, mc);
    }

    public Decimal(long val) {
        super(val);
    }

    public Decimal(long val, MathContext mc) {
        super(val, mc);
    }
}
