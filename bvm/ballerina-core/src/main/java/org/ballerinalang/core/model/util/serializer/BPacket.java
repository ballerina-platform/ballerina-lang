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
package org.ballerinalang.core.model.util.serializer;

import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;

/**
 * Wrapper class that wrap {@link BValue}s for use with {@link SerializationBValueProvider}.
 *
 * @since 0.982.0
 */
public class BPacket {
    private final BMap<String, BValue> map;

    private BPacket() {
        this(new BMap<>());
    }

    private BPacket(BMap<String, BValue> map) {
        this.map = map;
    }

    static BPacket toPacket(BMap<String, BValue> map) {
        return new BPacket(map);
    }

    /**
     * Create a {@link BPacket} instance, using the type that represent this BPacket and the main value represented by
     * this BPacket.
     *
     * @param type    Represented by this {@link BPacket}
     * @param payload Main value contain in this {@link BPacket}
     * @return new {@link BPacket}
     */
    public static BPacket from(String type, BValue payload) {
        BPacket bPacket = new BPacket();
        bPacket.put(JsonSerializerConst.VALUE_TAG, payload);
        bPacket.put(JsonSerializerConst.TYPE_TAG, BTreeHelper.createBString(type));
        return bPacket;
    }

    /**
     * Create a {@link BPacket} instance to represent a null object (null value) of a particular type.
     * <p>
     * This is particularly useful when an SerializationBValueSerializer doesn't actually want to serialize it's type.
     *
     * @param typeName Type name of null value object.
     * @return BPacket representing null.
     */
    public static BPacket nullObject(String typeName) {
        return from(typeName, null).put(JsonSerializerConst.NULL_OBJ, null);
    }

    public BMap<String, BValue> toBMap() {
        return map;
    }

    public BPacket put(String key, BValue item) {
        map.put(key, item);
        return this;
    }

    public BPacket putString(String key, String val) {
        map.put(key, BTreeHelper.createBString(val));
        return this;
    }

    public BValue getValue() {
        return map.get(JsonSerializerConst.VALUE_TAG);
    }

    public BValue get(String key) {
        return map.get(key);
    }
}
