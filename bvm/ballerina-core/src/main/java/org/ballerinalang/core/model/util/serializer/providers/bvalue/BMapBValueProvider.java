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
package org.ballerinalang.core.model.util.serializer.providers.bvalue;

import org.ballerinalang.core.model.types.BType;
import org.ballerinalang.core.model.util.serializer.BPacket;
import org.ballerinalang.core.model.util.serializer.BValueDeserializer;
import org.ballerinalang.core.model.util.serializer.BValueSerializer;
import org.ballerinalang.core.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Provide mapping between {@link BMap} and {@link BValue} representation of it.
 *
 * @since 0.982.0
 */
public class BMapBValueProvider implements SerializationBValueProvider<BMap> {

    private static final String MAP_STORAGE_TYPE = "#MAP_TYPE";

    @Override
    public String typeName() {
        return BMap.class.getSimpleName();
    }

    @Override
    public Class<?> getType() {
        return BMap.class;
    }

    @Override
    public BPacket toBValue(BMap bMap, BValueSerializer serializer) {
        BValue value = serializer.toBValue(bMap.getType(), null);
        LinkedHashMap implMap = bMap.getMap();
        BValue serialized = serializer.toBValue(implMap, implMap.getClass());
        return BPacket.from(typeName(), serialized).put(MAP_STORAGE_TYPE, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public BMap toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
        BMap payload = (BMap<String, BValue>) packet.getValue();
        BType type = (BType) bValueDeserializer.deserialize(packet.get(MAP_STORAGE_TYPE), BType.class);
        bValueDeserializer.addObjReference((BMap<String, BValue>) packet.get(MAP_STORAGE_TYPE), type);
        BMap bMap = new BMap(type);
        bValueDeserializer.addObjReference(packet.toBMap(), bMap);
        HashMap deserializedMap = (HashMap) bValueDeserializer.deserialize(payload, HashMap.class);
        bMap.getMap().putAll(deserializedMap);
        return bMap;
    }
}
