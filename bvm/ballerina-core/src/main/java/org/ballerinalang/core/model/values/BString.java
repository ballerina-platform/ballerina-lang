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
import org.ballerinalang.core.util.exceptions.BallerinaException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

/**
 * The {@code BString} represents a string in Ballerina.
 *
 * @since 0.8.0
 */
public final class BString extends BValueType implements BRefType<String> {

    private String value;
    private BType type = BTypes.typeString;


    public BString(String value) {
        this.value = value;
    }

    @Override
    public long intValue() {
        long result;
        try {
            result = Long.parseLong(this.value);
        } catch (NumberFormatException e) {
            throw new BallerinaException("input value " + this.value + " cannot be cast to integer");
        }
        return result;
    }

    @Override
    public long byteValue() {
        long result;
        try {
            result = Long.parseLong(this.value);
        } catch (NumberFormatException e) {
            throw new BallerinaException("input value " + this.value + " cannot be cast to byte");
        }
        return result;
    }

    @Override
    public double floatValue() {
        double result;
        try {
            result = Double.parseDouble(this.value);
        } catch (NumberFormatException e) {
            throw new BallerinaException("input value " + this.value + " cannot be cast to float");
        }
        return result;
    }

    @Override
    public BigDecimal decimalValue() {
        BigDecimal result;
        try {
            result = new BigDecimal(this.value, MathContext.DECIMAL128);
        } catch (NumberFormatException e) {
            throw new BallerinaException("input value " + this.value + " cannot be cast to decimal");
        }
        return result;
    }

    @Override
    public boolean booleanValue() {
        return Boolean.parseBoolean(value);
    }

    @Override
    public String stringValue() {
        return this.value;
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

        BString bString = (BString) o;

        return value != null ? value.equals(bString.value) : bString.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        return this;
    }
}
