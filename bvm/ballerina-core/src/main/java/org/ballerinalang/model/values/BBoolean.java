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
 * The {@code BBoolean} represents a boolean value in Ballerina.
 *
 * @since 0.8.0
 */
public final class BBoolean extends BValueType implements BRefType<Boolean> {

    /**
     * The {@code BBoolean} object corresponding to the primitive.
     * value {@code true}.
     */
    public static final BBoolean TRUE = new BBoolean(true);

    /**
     * The {@code BBoolean} object corresponding to the primitive.
     * value {@code true}.
     */
    public static final BBoolean FALSE = new BBoolean(false);

    private boolean value;

    public BBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public long intValue() {
        return 0;
    }

    @Override
    public double floatValue() {
        return 0;
    }

    @Override
    public boolean booleanValue() {
        return this.value;
    }

    @Override
    public byte[] blobValue() {
        return null;
    }

    @Override
    public String stringValue() {
        return Boolean.toString(value);
    }

    @Override
    public BType getType() {
        return BTypes.typeBoolean;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BBoolean bBoolean = (BBoolean) o;

        return value == bBoolean.value;

    }

    @Override
    public int hashCode() {
        return (value ? 1 : 0);
    }

    @Override
    public Boolean value() {
        return value;
    }

    public BValue copy() {
        return new BBoolean(value);
    }
}
