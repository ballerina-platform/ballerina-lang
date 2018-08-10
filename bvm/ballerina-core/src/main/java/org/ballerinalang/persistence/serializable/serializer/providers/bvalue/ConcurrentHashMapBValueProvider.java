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
package org.ballerinalang.persistence.serializable.serializer.providers.bvalue;

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.serializable.serializer.BValueDeserializer;
import org.ballerinalang.persistence.serializable.serializer.BValueSerializer;
import org.ballerinalang.persistence.serializable.serializer.SerializationBValueProvider;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provide mapping between {@link ConcurrentHashMap} and {@link BValue} representation of it.
 */
public class ConcurrentHashMapBValueProvider implements SerializationBValueProvider {
    @Override
    public String typeName() {
        return getType().getName();
    }

    @Override
    public Class<?> getType() {
        return ConcurrentHashMap.class;
    }

    @Override
    public BValue toBValue(Object object, BValueSerializer serializer) {
        if (object instanceof ConcurrentMap) {
            ConcurrentMap map = (ConcurrentMap) object;
            HashMap<Object, Object> hashMap = new HashMap<Object, Object>(map);
            return BValueProviderHelper.wrap(typeName(), serializer.toBValue(hashMap, null));
        }
        throw BValueProviderHelper.incorrectObjectType(object, typeName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
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
