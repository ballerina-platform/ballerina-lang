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

import java.math.BigDecimal;

/**
 * The {@code BValueType} represents a value type value in Ballerina.
 *
 * @since 0.8.0
 */
public abstract class BValueType implements BValue {

    /**
     * Returns the value of the specified number as an {@code int},
     * which may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type {@code int}.
     */
    public abstract long intValue();

    /**
     * Returns the value of the specified number as a {@code byte},
     * which may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type {@code byte}.
     */
    public abstract long byteValue();

    /**
     * Returns the value of the specified number as a {@code float},
     * which may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type {@code float}.
     */
    public abstract double floatValue();

    /**
     * Returns the value of the specified number as a {@code decimal},
     * which may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type {@code decimal}.
     */
    public abstract BigDecimal decimalValue();

    /**
     * Returns the value of the specified number as a {@code boolean}.
     *
     * @return the boolean value.
     */
    public abstract boolean booleanValue();

    /**
     * {@inheritDoc}
     */
    public boolean isFrozen() {
        return true;
    }

    /**
     * Default BValueType toString implementation.
     *
     * @return The string representation of this object
     */
    @Override
    public String toString() {
        return this.stringValue();
    }


    public abstract void setType(BType type);
}
