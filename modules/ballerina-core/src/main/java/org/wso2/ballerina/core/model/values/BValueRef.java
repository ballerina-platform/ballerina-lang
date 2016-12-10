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
package org.wso2.ballerina.core.model.values;

/**
 * {@code BValueRef} is a container for a BValue
 *
 * @since 1.0.0
 */
public class BValueRef {
    private BValue<?> bValue;

    public BValueRef(BValue<?> bValue) {
        this.bValue = bValue;
    }

    public BValue<?> getBValue() {
        return this.bValue;
    }

    public void setBValue(BValue<?> bValue) {
        this.bValue = bValue;
    }

    public boolean getBoolean() {
        return ((BooleanValue) bValue).getValue();
    }

    public int getInt() {
        return ((IntValue) bValue).getValue();
    }

    public long getLong() {
        return ((LongValue) bValue).getValue();
    }

    public float getFloat() {
        return ((FloatValue) bValue).getValue();
    }

    public double getDouble() {
        return ((DoubleValue) bValue).getValue();
    }

    public String getString() {
        return ((StringValue) bValue).getValue();
    }
}
