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
package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

/**
 * The {@code BFloat} represents a float value in Ballerina.
 *
 * @since 0.8.0
 */
public final class BFloat extends BValueType implements BRefType<Double> {

    private double value;

    public BFloat(double value) {
        this.value = value;
    }

    @Override
    public long intValue() {
        return (long) this.value;
    }

    @Override
    public double floatValue() {
        return this.value;
    }

    @Override
    public boolean booleanValue() {
        return false;
    }

    @Override
    public byte[] blobValue() {
        return null;
    }

    @Override
    public String stringValue() {
        return Double.toString(value);
    }

    @Override
    public BType getType() {
        return BTypes.typeFloat;
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
    public BValue copy() {
        return new BFloat(value);
    }
}
