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
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.StringJoiner;

/**
 * {@code BByteArray} represents a byte array in Ballerina.
 *
 * @since 0.980
 */
public class BByteArray extends BNewArray {

    private byte[] values;

    public BByteArray(byte[] values) {
        this.values = values;
        this.size = values.length;
        super.arrayType = new BArrayType(BTypes.typeByte, size);
    }

    public BByteArray() {
        values = (byte[]) newArrayInstance(Byte.TYPE);
        super.arrayType = new BArrayType(BTypes.typeByte, size);
    }

    public BByteArray(int size) {
        if (size != -1) {
            this.size = maxArraySize = size;
        }
        values = (byte[]) newArrayInstance(Byte.TYPE);
        super.arrayType = new BArrayType(BTypes.typeByte, size);
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
    public int getTag() {
        return TypeTags.BYTE_ARRAY_TAG;
    }

    @Override
    public BValue copy() {
        BByteArray byteArray = new BByteArray(Arrays.copyOf(values, values.length));
        byteArray.size = this.size;
        return byteArray;
    }

    @Override
    public String stringValue() {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (int i = 0; i < size; i++) {
            sj.add(Integer.toString(Byte.toUnsignedInt(values[i])));
        }
        return sj.toString();
    }

    @Override
    public BValue getBValue(long index) {
        return new BByte(get(index));
    }

    @Override
    public void serialize(OutputStream outputStream) {
        try {
            outputStream.write(values);
        } catch (IOException e) {
            throw new BallerinaException("error occurred while writing the binary content to the output stream", e);
        }
    }
}
