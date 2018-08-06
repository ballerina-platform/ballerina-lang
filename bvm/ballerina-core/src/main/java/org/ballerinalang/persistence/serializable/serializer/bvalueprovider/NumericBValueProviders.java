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
package org.ballerinalang.persistence.serializable.serializer.bvalueprovider;

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.serializable.serializer.SerializationBValueProvider;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumericBValueProviders {
    /**
     * Convert {@link BigInteger} into {@link BValue} object and back to facilitate serialization.
     */
    public static class BigIntegerBValueProvider implements SerializationBValueProvider {
        public static final String BIG_INT = BigInteger.class.getName();

        @Override
        public String typeName() {
            return BIG_INT;
        }

        @Override
        public Class<?> getType() {
            return BigInteger.class;
        }

        @Override
        public BValue toBValue(Object object) {
            if (object instanceof BigInteger) {
                BigInteger bigInteger = (BigInteger) object;
                return BValueProviderHelper.wrap(BIG_INT, new BString(bigInteger.toString(10)));
            }
            throw BValueProviderHelper.incorrectObjectType(object, BIG_INT);
        }

        @Override
        public Object toObject(BValue bValue) {
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

    public static class BigDecimalBValueProvider implements SerializationBValueProvider {
        public static final String BIG_DEC = BigDecimal.class.getName();

        @Override
        public String typeName() {
            return BIG_DEC;
        }

        @Override
        public Class<?> getType() {
            return BigDecimal.class;
        }

        @Override
        public BValue toBValue(Object object) {
            if (object instanceof BigDecimal) {
                BigDecimal bigDecimal = (BigDecimal) object;
                return BValueProviderHelper.wrap(BIG_DEC, new BString(bigDecimal.toString()));
            }
            throw BValueProviderHelper.incorrectObjectType(object, BIG_DEC);
        }

        @Override
        public Object toObject(BValue bValue) {
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
