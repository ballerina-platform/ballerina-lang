/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.service.http2.sample;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.HTTP2IntegrationTestCase;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Testing the Service Chaining sample located in
 * ballerina_home/samples/serviceChaining/ATMLocatorService.bal.
 */
public class ServiceChainingSampleTestCase extends HTTP2IntegrationTestCase {
    private static final String requestMessage = "{\"ATMLocator\": {\"ZipCode\": \"95999\"}}";
    private static final String responseMessage = "{\"ABC Bank\":{\"Address\":\"111 River Oaks Pkwy" +
            ", San Jose, CA 95999\"}}";

    @Test(description = "Test service chaining sample")
    public void testEchoServiceByBasePath() throws Exception {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, "/ABCBank/locator");
        request.headers().set(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        ByteBuf buffer = request.content().clear();
        int p0 = buffer.writerIndex();
        buffer.writeBytes(requestMessage.getBytes());
        int p1 = buffer.writerIndex();
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(p1 - p0));
        int send = http2Client.send(request);
        FullHttpResponse response = http2Client.getResponse(send);
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE.toLowerCase()), TestConstant
                        .CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(getResponse(response), responseMessage, "Message content mismatched");
    }
}
