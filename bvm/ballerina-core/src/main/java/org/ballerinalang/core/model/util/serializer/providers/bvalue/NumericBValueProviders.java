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
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Host class for various Numeric BValue providers.
 *
 * @since 0.982.0
 */
public class NumericBValueProviders {
    private NumericBValueProviders() {

    }

    /**
     * Convert {@link BigInteger} into {@link BValue} object and back to facilitate serialization.
     */
    public static class BigIntegerBValueProvider implements SerializationBValueProvider<BigInteger> {
        static final String BIG_INT = BigInteger.class.getName();

        @Override
        public String typeName() {
            return BIG_INT;
        }

        @Override
        public Class<?> getType() {
            return BigInteger.class;
        }

        @Override
        public BPacket toBValue(BigInteger bigInteger, BValueSerializer serializer) {
            return BPacket.from(BIG_INT, new BString(bigInteger.toString(10)));
        }

        @Override
        public BigInteger toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
            String payload = packet.getValue().stringValue();
            return new BigInteger(payload, 10);
        }
    }

    /**
     * Provide mapping between {@link BigDecimal} and its {@link BValue} representation.
     */
    public static class BigDecimalBValueProvider implements SerializationBValueProvider<BigDecimal> {
        static final String BIG_DEC = BigDecimal.class.getName();

        @Override
        public String typeName() {
            return BIG_DEC;
        }

        @Override
        public Class<?> getType() {
            return BigDecimal.class;
        }

        @Override
        public BPacket toBValue(BigDecimal bigDecimal, BValueSerializer serializer) {
            return BPacket.from(BIG_DEC, new BString(bigDecimal.toString()));
        }

        @SuppressWarnings("unchecked")
        @Override
        public BigDecimal toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
            String payload = packet.getValue().stringValue();
            return new BigDecimal(payload);
        }
    }
}
