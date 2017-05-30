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
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;

/**
 * The {@code BBlob} represents a byte array.
 * {@link BBlob} will be useful for storing byte values.
 *
 * @since 0.9.0
 */
public class BBlob extends BallerinaMessageDataSource implements BRefType<byte[]> {

    private byte[] value;

    public BBlob(byte[] value) {
        this.value = value;
    }

    @Override public byte[] value() {
        return value;
    }

    @Override public String stringValue() {
        return null;
    }

    @Override public BType getType() {
        return BTypes.typeBlob;
    }

    @Override public BValue copy() {
        return new BBlob(value);
    }

    @Override public byte[] getDataObject() {
        return value;
    }
}
