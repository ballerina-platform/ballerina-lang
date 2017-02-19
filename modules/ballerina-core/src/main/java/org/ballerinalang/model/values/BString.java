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

import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * The {@code BString} represents a string in Ballerina.
 *
 * @since 0.8.0
 */
public final class BString extends BValueType {

    private String value;

    public BString(String value) {
        this.value = value;
    }

    @Override
    public int intValue() {
        int result;
        try {
            result = Integer.parseInt(this.value);
        } catch (NumberFormatException e) {
            throw new BallerinaException("input value " + this.value + " cannot be cast to integer");
        }
        return result;
    }

    @Override
    public long longValue() {
        long result;
        try {
           result = Long.parseLong(this.value);
        } catch (NumberFormatException e) {
            throw new BallerinaException("input value " + this.value + " cannot be cast to long");
        }
        return result;
    }

    @Override
    public float floatValue() {
        float result;
        try {
            result = Float.parseFloat(this.value);
        } catch (NumberFormatException e) {
            throw new BallerinaException("input value " + this.value + " cannot be cast to float");
        }
        return result;
    }

    @Override
    public double doubleValue() {
        double result;
        try {
            result = Double.parseDouble(this.value);
        } catch (NumberFormatException e) {
            throw new BallerinaException("input value " + this.value + " cannot be cast to double");
        }
        return result;
    }

    @Override
    public boolean booleanValue() {
        return false;
    }

    @Override
    public String stringValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        return ((BString) obj).stringValue().equals(value);
    }
}
