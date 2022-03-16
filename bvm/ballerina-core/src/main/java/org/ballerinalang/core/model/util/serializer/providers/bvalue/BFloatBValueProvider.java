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
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BValue;

/**
 * Class implements @{@link SerializationBValueProvider} to provide the mapping between {@link BFloat} and
 * {@link BValue} representation of it.
 *
 * @since 0.983.0
 */
public class BFloatBValueProvider implements SerializationBValueProvider<BFloat> {
    @Override
    public String typeName() {
        return getType().getSimpleName();
    }

    @Override
    public Class<?> getType() {
        return BFloat.class;
    }

    @Override
    public BPacket toBValue(BFloat bFloat, BValueSerializer serializer) {
        return BPacket.from(typeName(), bFloat);
    }

    @Override
    public BFloat toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
        return (BFloat) packet.getValue();
    }
}
