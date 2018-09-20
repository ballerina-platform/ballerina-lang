/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.unitfunction;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * A unit test class for common/Util functions.
 */
public class CommonUtilTestCase {

    @Test(description = "Test setting headers to Http request with duplicate header keys")
    public void testCreateHttpRequest() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("aaa", "123");
        headers.add("aaa", "xyz");
        HttpCarbonMessage outboundRequestMsg = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        HttpRequest outboundNettyRequest = Util.createHttpRequest(outboundRequestMsg);

        Assert.assertEquals(outboundNettyRequest.method(), HttpMethod.POST);
        Assert.assertEquals(outboundNettyRequest.protocolVersion(), HttpVersion.HTTP_1_1);
        Assert.assertEquals(outboundNettyRequest.uri(), "/hello");
        Assert.assertEquals(outboundNettyRequest.headers().getAll("aaa").size(), 2);
        Assert.assertEquals(outboundNettyRequest.headers().getAll("aaa").get(0), "123");
        Assert.assertEquals(outboundNettyRequest.headers().getAll("aaa").get(1), "xyz");
    }

    @Test(description = "Test setting headers to Http response with duplicate header keys")
    public void testCreateHttpResponse() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("aaa", "123");
        headers.add("aaa", "xyz");
        HttpCarbonMessage outboundResponseMsg = new HttpCarbonMessage(
                new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, headers));
        HttpResponse outboundNettyResponse = Util.createHttpResponse(outboundResponseMsg, "1.1", "test-server", true);

        Assert.assertEquals(outboundNettyResponse.protocolVersion(), HttpVersion.HTTP_1_1);
        Assert.assertEquals(outboundNettyResponse.status(), HttpResponseStatus.OK);
        Assert.assertEquals(outboundNettyResponse.headers().getAll("aaa").size(), 2);
        Assert.assertEquals(outboundNettyResponse.headers().getAll("aaa").get(0), "123");
        Assert.assertEquals(outboundNettyResponse.headers().getAll("aaa").get(1), "xyz");
    }
}
