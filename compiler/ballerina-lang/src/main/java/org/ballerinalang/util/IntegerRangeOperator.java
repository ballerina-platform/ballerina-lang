package org.ballerinalang.util;

/**
 * Enum indicating possible integer range operators.
 *
 * Currently include:
 * 1. CLOSED_RANGE (...)
 * 2. HALF_OPEN_RANGE (..<)
 *
 * @since 0.963.0
 */
public enum IntegerRangeOperator {
    CLOSED_RANGE(0),
    HALF_OPEN_RANGE(1);

    private int operator;

    IntegerRangeOperator(int value) {
        operator = value;
    }

    public int value() {
        return operator;
    }
}
