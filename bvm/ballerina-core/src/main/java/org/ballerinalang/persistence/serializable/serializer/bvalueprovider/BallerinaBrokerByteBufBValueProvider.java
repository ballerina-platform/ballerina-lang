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
package org.ballerinalang.persistence.serializable.serializer.bvalueprovider;

import org.ballerinalang.broker.BallerinaBrokerByteBuf;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.serializable.serializer.BValueDeserializer;
import org.ballerinalang.persistence.serializable.serializer.BValueSerializer;
import org.ballerinalang.persistence.serializable.serializer.SerializationBValueProvider;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.lang.reflect.Field;

/**
 * Provide mapping between {@link BallerinaBrokerByteBuf} and {@link BValue} representation of it.
 */
public class BallerinaBrokerByteBufBValueProvider implements SerializationBValueProvider {
    @Override
    public String typeName() {
        return getType().getName();
    }

    @Override
    public Class<?> getType() {
        return BallerinaBrokerByteBuf.class;
    }

    @Override
    public BValue toBValue(Object object, BValueSerializer serializer) {
        if (object instanceof BallerinaBrokerByteBuf) {
            BallerinaBrokerByteBuf byteBuf = (BallerinaBrokerByteBuf) object;
            try {
                Field field = BallerinaBrokerByteBuf.class.getDeclaredField("value");
                field.setAccessible(true);
                Object value = field.get(byteBuf);
                BValue serialized = serializer.toBValue(value, null);
                BValue wrap = BValueProviderHelper.wrap(typeName(), serialized);
                return BValueProviderHelper.wrap(typeName(), wrap);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new BallerinaException("Can not serialize: " + typeName());
            }
        }
        throw BValueProviderHelper.incorrectObjectType(object, typeName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
        if (bValue instanceof BMap) {
            @SuppressWarnings("unchecked")
            BMap<String, BValue> wrapper = (BMap<String, BValue>) bValue;
            BValue payload = BValueProviderHelper.getPayload(wrapper);
            BValue innerPayload = BValueProviderHelper.getPayload((BMap<String, BValue>) payload);
            BValue obj = (BValue) bValueDeserializer.deserialize(innerPayload, BValue.class);
            return new BallerinaBrokerByteBuf(obj);
        }
        throw BValueProviderHelper.deserializationIncorrectType(bValue, typeName());
    }
}
