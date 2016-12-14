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

import com.google.gson.JsonElement;
import org.wso2.ballerina.core.model.types.TypeC;

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

    public JsonElement getJSON() {
        return ((JSONValue) bValue).getValue();
    }

    // TODO How about storing these default values in hash map. This would avoid creating new objects for each
    // TODO every variable declaration.
    // TODO One option is to implement a pool of BValue objects
    public static BValueRef getDefaultValue(TypeC type) {
        if (type == TypeC.INT_TYPE) {
            return new BValueRef(new IntValue(0));

        } else if (type == TypeC.STRING_TYPE) {
            return new BValueRef(new StringValue(""));

        } else if (type == TypeC.LONG_TYPE) {
            return new BValueRef(new LongValue(0));

        } else if (type == TypeC.FLOAT_TYPE) {
            return new BValueRef(new FloatValue(0));

        } else if (type == TypeC.DOUBLE_TYPE) {
            return new BValueRef(new DoubleValue(0));

        } else if (type == TypeC.BOOLEAN_TYPE) {
            return new BValueRef(new BooleanValue(false));

        } else if (type == TypeC.JSON_TYPE) {
            return new BValueRef(new JSONValue("{}"));

        } else if (type == TypeC.MESSAGE_TYPE) {
            return new BValueRef(new MessageValue(null));
        } else {
            throw new RuntimeException("Unsupported type: " + type);
        }
    }
}
