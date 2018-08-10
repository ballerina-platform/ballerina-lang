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

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.ballerinalang.broker.BallerinaBrokerByteBuf;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.serializable.serializer.JsonSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ComplexObjectGraphSerializationTest {
    @Test(description = "Test serialize/deserialize HttpResponse Object")
    public void testJsonDeserializeHttpResponse() {
        BValue bTree = new BMap<String, BValue>();
        ((BMap) bTree).put("Item1", new BString("Value1"));
        ByteBuf byteBuf = new BallerinaBrokerByteBuf(bTree);
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);

        // NOTE: Serializing large object structures will produce humongous JSON payload.
        // Consider using SerializableBValueProviders to minimize output size.
        String serialize = new JsonSerializer().serialize(response);
        HttpResponse deserializedResponse = (HttpResponse) new JsonSerializer()
                .deserialize(serialize.getBytes(), DefaultFullHttpResponse.class);

        Assert.assertTrue(deserializedResponse.status().equals(HttpResponseStatus.OK));
    }
}
