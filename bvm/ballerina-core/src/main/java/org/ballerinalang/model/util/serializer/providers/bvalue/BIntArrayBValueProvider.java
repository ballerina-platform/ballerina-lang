/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.util.serializer.providers.bvalue;

import org.ballerinalang.model.util.serializer.BPacket;
import org.ballerinalang.model.util.serializer.BValueDeserializer;
import org.ballerinalang.model.util.serializer.BValueSerializer;
import org.ballerinalang.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;

/**
 * Provide mapping between {@link BIntArray} and {@link BValue} representation of it.
 *
 * @since 0.983.0
 */
public class BIntArrayBValueProvider implements SerializationBValueProvider<BIntArray> {

    @Override
    public String typeName() {
        return getType().getSimpleName();
    }

    @Override
    public Class<?> getType() {
        return BIntArray.class;
    }

    @Override
    public BPacket toBValue(BIntArray array, BValueSerializer serializer) {
        long size = array.size();
        long[] ar = new long[(int) size];

        for (int i = 0; i < array.size(); i++) {
            ar[i] = array.get(i);
        }

        BValue value = serializer.toBValue(ar, long[].class);
        return BPacket.from(typeName(), value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public BIntArray toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
        BRefValueArray array = (BRefValueArray) packet.getValue();
        BIntArray target = new BIntArray(5);
        for (int i = 0; i < array.size(); i++) {
            target.add(i, (long) bValueDeserializer.deserialize(array.get(i), long.class));
        }

        return target;
    }
}
