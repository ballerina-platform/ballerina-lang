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

import org.ballerinalang.core.model.util.serializer.BPacket;
import org.ballerinalang.core.model.util.serializer.BValueDeserializer;
import org.ballerinalang.core.model.util.serializer.BValueSerializer;
import org.ballerinalang.core.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.core.model.values.BValue;

import java.net.InetSocketAddress;

/**
 * Convert {@link InetSocketAddress} into {@link BValue} object and back to facilitate serialization.
 *
 * @since 0.982.0
 */
public class InetSocketAddressBValueProvider implements SerializationBValueProvider<InetSocketAddress> {
    @Override
    public Class<?> getType() {
        return InetSocketAddress.class;
    }

    @Override
    public BPacket toBValue(InetSocketAddress object, BValueSerializer serializer) {
        return BPacket.nullObject(typeName());
    }

    @Override
    public InetSocketAddress toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
        // return null, we can't serialize the socket address.
        return null;
    }
}
