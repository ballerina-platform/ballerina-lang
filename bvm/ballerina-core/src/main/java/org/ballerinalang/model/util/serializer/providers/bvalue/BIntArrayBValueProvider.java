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

import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.util.serializer.BPacket;
import org.ballerinalang.model.util.serializer.BValueDeserializer;
import org.ballerinalang.model.util.serializer.BValueSerializer;
import org.ballerinalang.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;

/**
 * Provide mapping between {@link BValueArray} and {@link BValue} representation of it.
 *
 * @since 0.983.0
 */
public class BIntArrayBValueProvider implements SerializationBValueProvider<BValueArray> {

    @Override
    public String typeName() {
        return getType().getSimpleName();
    }

    @Override
    public Class<?> getType() {
        return BValueArray.class;
    }

    @Override
    public BPacket toBValue(BValueArray array, BValueSerializer serializer) {
        long size = array.size();
        long[] ar = new long[(int) size];

        for (int i = 0; i < array.size(); i++) {
            ar[i] = array.getInt(i);
        }

        BValue value = serializer.toBValue(ar, long[].class);
        return BPacket.from(typeName(), value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public BValueArray toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
        BValueArray array = (BValueArray) packet.getValue();
        BValueArray target = new BValueArray(BTypes.typeInt, 5);
        for (int i = 0; i < array.size(); i++) {
            target.add(i, (long) bValueDeserializer.deserialize(array.getRefValue(i), long.class));
        }

        return target;
    }
}
