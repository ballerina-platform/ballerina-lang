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

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.serializable.serializer.BValueDeserializer;
import org.ballerinalang.persistence.serializable.serializer.BValueSerializer;
import org.ballerinalang.persistence.serializable.serializer.SerializationBValueProvider;

import java.time.Instant;
import java.util.Date;

/**
 * Default SerializationBValueProviders for Date and Time classes.
 */
public class DateTimeBValueProviders {
    /**
     * Convert {@link Date} into {@link BValue} object and back to facilitate serialization.
     */
    public static class DateBValueProvider implements SerializationBValueProvider<Date> {

        @Override
        public Class<?> getType() {
            return Date.class;
        }

        @Override
        public BValue toBValue(Date date, BValueSerializer serializer) {
            long time = date.getTime();
            return BValueProviderHelper.wrap(typeName(), new BInteger(time));
        }

        @Override
        @SuppressWarnings("unchecked")
        public Date toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
            BInteger time = (BInteger) BValueProviderHelper.getPayload((BMap<String, BValue>) bValue);
            return new Date(time.intValue());
        }
    }

    public static class InstantBValueProvider implements SerializationBValueProvider<Instant> {

        @Override
        public Class<?> getType() {
            return Instant.class;
        }

        @Override
        public BValue toBValue(Instant object, BValueSerializer serializer) {
            long l = object.toEpochMilli();
            return BValueProviderHelper.wrap(typeName(), new BInteger(l));
        }

        @Override
        public Instant toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
            BInteger epochMilli = (BInteger) BValueProviderHelper.getPayload((BMap<String, BValue>) bValue);
            return Instant.ofEpochMilli(epochMilli.intValue());
        }
    }
}
