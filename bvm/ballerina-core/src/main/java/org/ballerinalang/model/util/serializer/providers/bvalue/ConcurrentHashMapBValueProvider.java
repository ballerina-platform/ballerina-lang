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

import org.ballerinalang.model.util.serializer.BValueDeserializer;
import org.ballerinalang.model.util.serializer.BValueSerializer;
import org.ballerinalang.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provide mapping between {@link ConcurrentHashMap} and {@link BValue} representation of it.
 *
 * @since 0.982.0
 */
public class ConcurrentHashMapBValueProvider implements SerializationBValueProvider<ConcurrentHashMap> {
    @Override
    public String typeName() {
        return getType().getName();
    }

    @Override
    public Class<?> getType() {
        return ConcurrentHashMap.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BValue toBValue(ConcurrentHashMap map, BValueSerializer serializer) {
        HashMap<Object, Object> hashMap = new HashMap<>(map);
        return BValueProviderHelper.wrap(typeName(), serializer.toBValue(hashMap, null));
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConcurrentHashMap toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
        if (bValue instanceof BMap) {
            BMap<String, BValue> wrapper = (BMap<String, BValue>) bValue;
            BMap<String, BValue> payload = (BMap<String, BValue>) BValueProviderHelper.getPayload(wrapper);
            HashMap<Object, Object> hashMap =
                    (HashMap<Object, Object>) bValueDeserializer.deserialize(payload, HashMap.class);
            return new ConcurrentHashMap<>(hashMap);
        }
        throw BValueProviderHelper.deserializationIncorrectType(bValue, typeName());
    }
}
