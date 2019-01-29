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

import org.ballerinalang.broker.BallerinaBrokerByteBuf;
import org.ballerinalang.model.util.serializer.BPacket;
import org.ballerinalang.model.util.serializer.BValueDeserializer;
import org.ballerinalang.model.util.serializer.BValueSerializer;
import org.ballerinalang.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.lang.reflect.Field;

/**
 * Provide mapping between {@link BallerinaBrokerByteBuf} and {@link BValue} representation of it.
 *
 * @since 0.982.0
 */
public class BallerinaBrokerByteBufBValueProvider implements SerializationBValueProvider<BallerinaBrokerByteBuf> {
    @Override
    public String typeName() {
        return getType().getName();
    }

    @Override
    public Class<?> getType() {
        return BallerinaBrokerByteBuf.class;
    }

    @Override
    public BPacket toBValue(BallerinaBrokerByteBuf object, BValueSerializer serializer) {
        try {
            Field field = BallerinaBrokerByteBuf.class.getDeclaredField("value");
            field.setAccessible(true);
            Object value = field.get(object);
            BValue serialized = serializer.toBValue(value, null);
            return BPacket.from(typeName(), serialized);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new BallerinaException("Can not serialize: " + typeName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public BallerinaBrokerByteBuf toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
        BValue obj = (BValue) bValueDeserializer.deserialize(packet.getValue(), BValue.class);
        return new BallerinaBrokerByteBuf(obj);
    }
}
