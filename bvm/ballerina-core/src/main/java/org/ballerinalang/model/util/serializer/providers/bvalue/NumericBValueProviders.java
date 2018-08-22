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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Host class for various Numeric BValue providers.
 */
public class NumericBValueProviders {
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
        public BValue toBValue(BigInteger bigInteger, BValueSerializer serializer) {
            return BValueProviderHelper.wrap(BIG_INT, new BString(bigInteger.toString(10)));
        }

        @Override
        public BigInteger toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
            if (bValue instanceof BMap) {
                @SuppressWarnings("unchecked")
                BMap<String, BValue> wrapper = (BMap<String, BValue>) bValue;
                if (BValueProviderHelper.isWrapperOfType(wrapper, BIG_INT)) {
                    String payload = BValueProviderHelper.getPayload(wrapper).stringValue();
                    return new BigInteger(payload, 10);
                }
            }
            throw BValueProviderHelper.deserializationIncorrectType(bValue, BIG_INT);
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
        public BValue toBValue(BigDecimal bigDecimal, BValueSerializer serializer) {
            return BValueProviderHelper.wrap(BIG_DEC, new BString(bigDecimal.toString()));
        }

        @Override
        public BigDecimal toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
            if (bValue instanceof BMap) {
                BMap<String, BValue> wrapper = (BMap<String, BValue>) bValue;
                if (BValueProviderHelper.isWrapperOfType(wrapper, BIG_DEC)) {
                    String payload = BValueProviderHelper.getPayload(wrapper).stringValue();
                    return new BigDecimal(payload);
                }
            }
            throw BValueProviderHelper.deserializationIncorrectType(bValue, BIG_DEC);
        }
    }
}
