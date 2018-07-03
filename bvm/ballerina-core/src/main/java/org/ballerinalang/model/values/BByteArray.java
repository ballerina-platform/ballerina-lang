/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * {@code BByteArray} represents a byte array in Ballerina.
 *
 * @since 0.980
 */
public class BByteArray extends BNewArray {

    private static BType arrayType = new BArrayType(BTypes.typeByte);

    private byte[] values;

    public BByteArray(byte[] values) {
        this.values = values;
        this.size = values.length;
    }

    public BByteArray() {
        values = (byte[]) newArrayInstance(Byte.TYPE);
    }

    public void add(long index, byte value) {
        prepareForAdd(index, values.length);
        values[(int) index] = value;
    }

    public byte get(long index) {
        rangeCheckForGet(index, size);
        return values[(int) index];
    }

    public byte[] getBytes() {
        return values.clone();
    }

    @Override
    public BType getType() {
        return arrayType;
    }

    @Override
    public void grow(int newLength) {
        values = Arrays.copyOf(values, newLength);
    }

    @Override
    public BValue copy() {
        BByteArray byteArray = new BByteArray(Arrays.copyOf(values, values.length));
        byteArray.size = this.size;
        return byteArray;
    }

    @Override
    public String stringValue() {
        return new String(values, StandardCharsets.UTF_8);
    }

    @Override
    public BValue getBValue(long index) {
        return new BByte(get(index));
    }
}
