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

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Testing the Routing service sample located in
 * ballerina_home/samples/routingServices/routingServices.bal.
 */
public class RoutingServiceSampleTestCase extends HTTP2IntegrationTestCase {
    private final String requestNyseMessage = "{\"name\":\"nyse\"}";
    private final String responseNyseMessage = "{\"exchange\":\"nyse\",\"name\":\"IBM\",\"value\":\"127.50\"}";
    private final String requestNasdaqMessage = "{\"name\":\"nasdaq\"}";
    private final String responseNasdaqMessage = "{\"exchange\":\"nasdaq\",\"name\":\"IBM\",\"value\":\"127.50\"}";

    @Test(description = "Test Content base routing sample")
    public void testContentBaseRouting() throws Exception {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, "/cbr");
        request.headers().set(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        ByteBuf buffer = request.content().clear();
        int p0 = buffer.writerIndex();
        buffer.writeBytes(requestNyseMessage.getBytes());
        int p1 = buffer.writerIndex();
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(p1 - p0));
        int send = http2Client.send(request);
        FullHttpResponse response = http2Client.getResponse(send);
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE.toLowerCase()), TestConstant
                        .CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(getResponse(response), responseNyseMessage, "Message content mismatched. " +
                "Routing failed for nyse");
        //sending nasdaq as name
        request = new DefaultFullHttpRequest(HTTP_1_1, POST, "/cbr");
        request.headers().set(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        buffer = request.content().clear();
        p0 = buffer.writerIndex();
        buffer.writeBytes(requestNasdaqMessage.getBytes());
        p1 = buffer.writerIndex();
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(p1 - p0));
        send = http2Client.send(request);
        response = http2Client.getResponse(send);
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE.toLowerCase()), TestConstant
                        .CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(getResponse(response), responseNasdaqMessage, "Message content mismatched. " +
                "Routing failed for nasdaq");
    }

    @Test(description = "Test Header base routing sample")
    public void testHeaderBaseRouting() throws Exception {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, "/hbr");
        request.headers().set("name", "nyse");
        //sending nyse as name header
        int send = http2Client.send(request);
        FullHttpResponse response = http2Client.getResponse(send);
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE.toLowerCase()), TestConstant
                        .CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(getResponse(response), responseNyseMessage, "Message content mismatched. Routing failed " +
                "for nyse");
        //sending nasdaq as http header
        request = new DefaultFullHttpRequest(HTTP_1_1, GET, "/hbr");
        request.headers().set("name", "nasdaq");
        send = http2Client.send(request);
        response = http2Client.getResponse(send);
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE.toLowerCase()), TestConstant
                .CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(getResponse(response), responseNasdaqMessage, "Message content mismatched. Routing failed" +
                " for nasdaq");
    }
}
