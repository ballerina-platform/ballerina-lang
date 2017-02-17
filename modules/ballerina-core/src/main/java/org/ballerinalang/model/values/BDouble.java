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

/**
 * The {@code BDouble} represents a double value in Ballerina.
 *
 * @since 0.8.0
 */
public final class BDouble extends BValueType {

    private double value;

    public BDouble(double value) {
        this.value = value;
    }

    @Override
    public int intValue() {
        return (int) this.value;
    }

    @Override
    public long longValue() {
        return (long) this.value;
    }

    @Override
    public float floatValue() {
        return (float) this.value;
    }

    @Override
    public double doubleValue() {
        return this.value;
    }

    @Override
    public boolean booleanValue() {
        return false;
    }

    @Override
    public String stringValue() {
        return Double.toString(value);
    }

    @Override
    public boolean equals(Object obj) {
        return ((BDouble) obj).doubleValue() == value;
    }
}
