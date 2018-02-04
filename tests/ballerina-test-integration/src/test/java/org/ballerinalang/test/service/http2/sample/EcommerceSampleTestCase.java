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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Testing the E-Commerce sample located in
 * ballerina_home/samples/restfulService/ecommerceService.bal.
 */
public class EcommerceSampleTestCase extends HTTP2IntegrationTestCase {

    private static final Logger log = LoggerFactory.getLogger(EchoServiceSampleTestCase.class);

    @Test(description = "Test resource GET products in E-Commerce sample")
    public void testGetProducts() throws Exception {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, "/ecommerceservice/products/123001");
        int send = http2Client.send(request);
        FullHttpResponse response = http2Client.getResponse(send);
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE.toLowerCase()), TestConstant
                        .CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(getResponse(response),
                "{\"Product\":{\"ID\":\"123001\",\"Name\":\"ABC_2\",\"Description\":\"Sample product.\"}}",
                "Message content mismatched");
    }

    @Test(description = "Test resource GET orders in E-Commerce sample")
    public void testGetOrders() throws Exception {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, "/ecommerceservice/orders");
        int send = http2Client.send(request);
        FullHttpResponse response = http2Client.getResponse(send);
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(getResponse(response), "{\"Order\":{\"ID\":\"111999\",\"Name\":\"ABC123\"," +
                "\"Description\":\"Sample order.\"}}", "Message content mismatched");
    }

    @Test(description = "Test resource GET customers in E-Commerce sample")
    public void testGetCustomers() throws Exception {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, "/ecommerceservice/customers");
        int send = http2Client.send(request);
        FullHttpResponse response = http2Client.getResponse(send);
        //request should be returned as response
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(getResponse(response), "{\"Customer\":{\"ID\":\"987654\",\"Name\":\"ABC PQR\"," +
                "\"Description\":\"Sample Customer.\"}}", "Message content mismatched");
    }

    @Test(description = "Test resource POST orders in E-Commerce sample")
    public void testPostOrder() throws Exception {
        String requestMessage = "{\"Order\":{\"ID\":\"111222\",\"Name\":\"XYZ123\"}}";
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST,
                "ecommerceservice/orders");
        request.headers().set(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        ByteBuf buffer = request.content().clear();
        int p0 = buffer.writerIndex();
        buffer.writeBytes(requestMessage.getBytes());
        int p1 = buffer.writerIndex();
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(p1 - p0));
        int send = http2Client.send(request);
        FullHttpResponse response = http2Client.getResponse(send);
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE), TestConstant.CONTENT_TYPE_JSON,
                "Content-Type mismatched");
        Assert.assertEquals(getResponse(response), "{\"Status\":\"Order is successfully added.\"}"
                , "Message content mismatched");
    }

    @Test(description = "Test resource POST products in E-Commerce sample")
    public void testPostProduct() throws Exception {
        String requestMessage = "{\"Product\":{\"ID\":\"123345\",\"Name\":\"PQR\"}}";
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, "/ecommerceservice/products");
        request.headers().set(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        ByteBuf buffer = request.content().clear();
        int p0 = buffer.writerIndex();
        buffer.writeBytes(requestMessage.getBytes());
        int p1 = buffer.writerIndex();
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(p1 - p0));
        int send = http2Client.send(request);
        FullHttpResponse response = http2Client.getResponse(send);
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(getResponse(response), "{\"Status\":\"Product is successfully added.\"}"
                , "Message content mismatched");
    }

    @Test(description = "Test resource POST customers in E-Commerce sample")
    public void testPostCustomers() throws Exception {
        String requestMessage = "{\"Customer\":{\"ID\":\"97453\",\"Name\":\"ABC XYZ\"}}";
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, "/ecommerceservice/customers");
        request.headers().set(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        ByteBuf buffer = request.content().clear();
        int p0 = buffer.writerIndex();
        buffer.writeBytes(requestMessage.getBytes());
        int p1 = buffer.writerIndex();
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(p1 - p0));
        int send = http2Client.send(request);
        FullHttpResponse response = http2Client.getResponse(send);
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE), TestConstant.CONTENT_TYPE_JSON,
                "Content-Type mismatched");
        Assert.assertEquals(getResponse(response), "{\"Status\":\"Customer is successfully added.\"}"
                , "Message content mismatched");
    }
}
