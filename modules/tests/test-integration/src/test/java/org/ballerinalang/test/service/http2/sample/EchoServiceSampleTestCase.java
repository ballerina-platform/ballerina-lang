/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.service.http2.sample;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.HTTP2IntegrationTestCase;
import org.ballerinalang.test.util.TestConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class EchoServiceSampleTestCase extends HTTP2IntegrationTestCase {
    private final String requestMessage = "{\"exchange\":\"nyse\",\"name\":\"WSO2\",\"value\":\"127.50\"}";
    private static final Logger log = LoggerFactory.getLogger(EchoServiceSampleTestCase.class);

    @Test(description = "Test echo service sample test case invoking base path")
    public void testEchoServiceByBasePath() throws Exception {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, "/echo");
        request.headers().set(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        ByteBuf buffer = request.content().clear();
        int p0 = buffer.writerIndex();
        buffer.writeBytes(requestMessage.getBytes());
        int p1 = buffer.writerIndex();
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(p1 - p0));
        int send = http2Client.send(request);
        FullHttpResponse response = http2Client.getResponse(send);
        //request should be returned as response
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(getResponse(response), requestMessage, "Message content mismatched");
    }

    @Test(description = "Test echo service sample test case")
    public void testEchoServiceByResourcePath() throws Exception {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, "/echo/resource");
        request.headers().set(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        request.headers().set(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        ByteBuf buffer = request.content().clear();
        int p0 = buffer.writerIndex();
        buffer.writeBytes(requestMessage.getBytes());
        int p1 = buffer.writerIndex();
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(p1 - p0));
        int send = http2Client.send(request);
        FullHttpResponse response = http2Client.getResponse(send);
        //request should be returned as response
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(getResponse(response),  requestMessage, "Message content mismatched");
    }


}
