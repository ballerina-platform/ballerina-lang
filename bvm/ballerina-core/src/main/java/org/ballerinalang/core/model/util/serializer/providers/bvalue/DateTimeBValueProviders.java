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
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;

import java.time.Instant;
import java.util.Date;

/**
 * Default SerializationBValueProviders for Date and Time classes.
 *
 * @since 0.982.0
 */
public class DateTimeBValueProviders {

    private DateTimeBValueProviders() {
    }

    /**
     * Convert {@link Date} into {@link BValue} object and back to facilitate serialization.
     */
    public static class DateBValueProvider implements SerializationBValueProvider<Date> {

        @Override
        public Class<?> getType() {
            return Date.class;
        }

        @Override
        public BPacket toBValue(Date date, BValueSerializer serializer) {
            long time = date.getTime();
            return BPacket.from(typeName(), new BInteger(time));
        }

        @Override
        public Date toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
            BInteger time = (BInteger) packet.getValue();
            return new Date(time.intValue());
        }
    }

    /**
     * Convert {@link Instant} into {@link BValue} object and back to facilitate serialization.
     */
    public static class InstantBValueProvider implements SerializationBValueProvider<Instant> {

        @Override
        public Class<?> getType() {
            return Instant.class;
        }

        @Override
        public BPacket toBValue(Instant object, BValueSerializer serializer) {
            long l = object.toEpochMilli();
            return BPacket.from(typeName(), new BInteger(l));
        }

        @SuppressWarnings("unchecked")
        @Override
        public Instant toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
            BInteger epochMilli = (BInteger) packet.getValue();
            return Instant.ofEpochMilli(epochMilli.intValue());
        }
    }
}
