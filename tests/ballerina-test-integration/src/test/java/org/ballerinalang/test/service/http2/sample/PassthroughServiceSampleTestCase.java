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

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.ballerinalang.test.HTTP2IntegrationTestCase;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Testing the passthrough service sample located in
 * ballerina_home/samples/passthroughService/passthroughService.bal.
 */
public class PassthroughServiceSampleTestCase extends HTTP2IntegrationTestCase {
    private final String responseMessage = "{\"exchange\":\"nyse\",\"name\":\"IBM\",\"value\":\"127.50\"}";

    @Test(description = "Test Passthrough sample test case invoking base path")
    public void testPassthroughServiceByBasePath() throws Exception {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, "/passthrough");
        int send = http2Client.send(request);
        FullHttpResponse response = http2Client.getResponse(send);
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(getResponse(response), responseMessage, "Message content mismatched");
    }

    @Test(description = "Test Passthrough sample test case")
    public void testPassthroughServiceByResourcePath() throws Exception {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, "/passthrough/resource");
        int send = http2Client.send(request);
        FullHttpResponse response = http2Client.getResponse(send);
        Assert.assertEquals(response.getStatus().code(), 200, "Response code mismatched");
        Assert.assertEquals(response.headers().get(TestConstant.HEADER_CONTENT_TYPE.toLowerCase())
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(getResponse(response), responseMessage, "Message content mismatched");
    }
}
