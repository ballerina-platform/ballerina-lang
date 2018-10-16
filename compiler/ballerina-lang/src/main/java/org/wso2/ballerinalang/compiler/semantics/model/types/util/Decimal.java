/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerinalang.compiler.semantics.model.types.util;

import java.math.BigDecimal;

import static java.math.MathContext.DECIMAL128;

/**
 * {@code Decimal} contains the implementation of IEEE 754R standard based
 * Decimal128 floating point number representation.
 *
 * @since 0.982
 */
public class Decimal extends BigDecimal {

    public static final Decimal ZERO = new Decimal("0.0");
    public static final Decimal ONE = new Decimal("1.0");

    public Decimal(String val) {
        super(val, DECIMAL128);
    }

    private Decimal(BigDecimal val) {
        this(val.toString());
    }

    public Decimal add(Decimal augend) {
        return new Decimal(super.add(augend, DECIMAL128));
    }

    public Decimal subtract(Decimal subtrahend) {
        return new Decimal(super.subtract(subtrahend, DECIMAL128));
    }

    public Decimal multiply(Decimal multiplicand) {
        return new Decimal(super.multiply(multiplicand, DECIMAL128));
    }

    public Decimal divide(Decimal divisor) {
        return new Decimal(super.divide(divisor, DECIMAL128));
    }

    public Decimal negate() {
        return new Decimal(super.negate(DECIMAL128));
    }

    public Decimal reminder(Decimal divisor) {
        return new Decimal(super.remainder(divisor, DECIMAL128));
    }
}
