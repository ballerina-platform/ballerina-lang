/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

import java.nio.charset.StandardCharsets;

/**
 * The {@code BBlob} represents a byte array.
 * {@link BBlob} will be useful for storing byte values.
 */
public class BBlob extends BValueType implements BRefType<byte[]> {

    private byte[] value;

    public BBlob(byte[] value) {
        this.value = value;
    }

    @Override
    public String stringValue() {
        return new String(value, StandardCharsets.UTF_8);
    }

    @Override
    public BType getType() {
        return BTypes.typeBlob;
    }

    @Override
    public BValue copy() {
        return new BBlob(value);
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
        return false;
    }

    @Override
    public byte[] blobValue() {
        return value;
    }

    @Override
    public byte[] value() {
        return value;
    }
}
