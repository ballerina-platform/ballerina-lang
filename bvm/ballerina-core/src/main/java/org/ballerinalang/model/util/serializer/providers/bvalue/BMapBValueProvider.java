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

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.util.serializer.BValueDeserializer;
import org.ballerinalang.model.util.serializer.BValueSerializer;
import org.ballerinalang.model.util.serializer.JsonSerializerConst;
import org.ballerinalang.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Provide mapping between {@link BMap} and {@link BValue} representation of it.
 *
 * @since 0.98.1
 */
public class BMapBValueProvider implements SerializationBValueProvider<BMap> {
    @Override
    public String typeName() {
        return BMap.class.getSimpleName();
    }

    @Override
    public Class<?> getType() {
        return BMap.class;
    }

    @Override
    public BValue toBValue(BMap bMap, BValueSerializer serializer) {
        LinkedHashMap implMap = bMap.getMap();
        BType type = bMap.getType();
        BValue serialized = serializer.toBValue(implMap, implMap.getClass());
        BMap<String, BValue> wrap = BValueProviderHelper.wrap(typeName(), serialized);

        BValue serializedType = serializer.toBValue(type, null);
        wrap.put(JsonSerializerConst.MAP_TYPE_TAG, serializedType);
        return wrap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BMap toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
        if (bValue instanceof BMap) {
            BMap<String, BValue> wrapper = (BMap<String, BValue>) bValue;
            if (BValueProviderHelper.isWrapperOfType(wrapper, typeName())) {
                BMap payload = (BMap) BValueProviderHelper.getPayload(wrapper);
                HashMap deserializedMap = (HashMap) bValueDeserializer.deserialize(payload, HashMap.class);
                BValue mapTypeTag = wrapper.get(JsonSerializerConst.MAP_TYPE_TAG);
                bValueDeserializer.deserialize(mapTypeTag, BType.class);
                BMap bMap = new BMap();
                bMap.getMap().putAll(deserializedMap);
                return bMap;
            }
        }
        throw BValueProviderHelper.deserializationIncorrectType(bValue, typeName());
    }
}
