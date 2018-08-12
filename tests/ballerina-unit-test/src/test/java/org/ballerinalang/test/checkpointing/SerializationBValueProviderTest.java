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
package org.ballerinalang.test.checkpointing;

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.serializable.serializer.JsonSerializer;
import org.ballerinalang.persistence.serializable.serializer.providers.bvalue.NumericBValueProviders;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.ballerinalang.persistence.serializable.serializer.JsonSerializerConst.PAYLOAD_TAG;

public class SerializationBValueProviderTest {

    @Test(description = "test BigInt to BValue conversion")
    public void testBigIntBValueProviderToBValue() {
        NumericBValueProviders.BigIntegerBValueProvider provider =
                new NumericBValueProviders.BigIntegerBValueProvider();
        String num = "2345232323";
        BValue value = provider.toBValue(new BigInteger(num), new JsonSerializer());

        Assert.assertTrue(value instanceof BMap);

        BMap<String, BValue> map = (BMap<String, BValue>) value;
        BValue payload = map.get(PAYLOAD_TAG);
        Assert.assertTrue(payload.stringValue().equals(num));
    }

    @Test(description = "test BValue to BigInt conversion")
    public void testBigIntBValueProviderToBigInt() {
        NumericBValueProviders.BigIntegerBValueProvider provider =
                new NumericBValueProviders.BigIntegerBValueProvider();
        String num = "2345232323";
        BValue value = provider.toBValue(new BigInteger(num), new JsonSerializer());

        Object object = provider.toObject(value, null);
        Assert.assertTrue(object instanceof BigInteger);
        Assert.assertTrue(((BigInteger) object).toString(10).equals(num));
    }

    @Test(description = "test BValue to/from BigDecimal conversion")
    public void testBigIntBValueProviderBigDecimal() {
        NumericBValueProviders.BigDecimalBValueProvider provider =
                new NumericBValueProviders.BigDecimalBValueProvider();
        String num = "2345232323";
        BValue value = provider.toBValue(new BigDecimal(num), new JsonSerializer());

        Object object = provider.toObject(value, null);
        Assert.assertTrue(object instanceof BigDecimal);
        Assert.assertTrue(((BigDecimal) object).toString().equals(num));
    }
}
