/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.core.model.values;

import org.ballerinalang.core.model.types.BType;
import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.core.util.exceptions.BallerinaException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

/**
 * The {@code BFloat} represents a float value in Ballerina.
 *
 * @since 0.8.0
 */
public final class BFloat extends BValueType implements BRefType<Double> {

    private double value;
    private BType type = BTypes.typeFloat;

    public BFloat(double value) {
        this.value = value;
    }

    @Override
    public long intValue() {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new BallerinaException(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                                         "'float' value '" + value + "' cannot be converted to 'int'");
        }

        return Math.round(value);
    }

    @Override
    public long byteValue() {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new BallerinaException(BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                                         "'float' value '" + value + "' cannot be converted to 'byte'");
        }

        return Math.round(value);
    }

    @Override
    public double floatValue() {
        return this.value;
    }

    @Override
    public BigDecimal decimalValue() {
        return new BigDecimal(value, MathContext.DECIMAL128);
    }

    @Override
    public boolean booleanValue() {
        return value != 0.0;
    }

    @Override
    public String stringValue() {
        return Double.toString(value);
    }

    @Override
    public BType getType() {
        return type;
    }

    @Override
    public void setType(BType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BFloat bFloat = (BFloat) o;

        return Double.compare(bFloat.value, value) == 0;

    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(value);
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public Double value() {
        return value;
    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        return this;
    }
}
