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

/**
 * A unit test class for message/HttpCarbonMessage functions.
 */
public class HttpCarbonMessageTestCase {

    // TODO: Will remove this completely later. For now it is there as a reference.
//    @Test(description = "Test cloneCarbonMessageWithData to Http request with duplicate header keys")
//    public void cloneCarbonMessageWithData() {
//        HttpHeaders headers = new DefaultHttpHeaders();
//        headers.set("aaa", "123");
//        headers.add("aaa", "xyz");
//        HttpCarbonMessage outboundRequestMsg = new HttpCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
//                HttpMethod.POST, "", headers));
//        outboundRequestMsg.setProperty(Constants.TO, "/hello");
//        outboundRequestMsg.addHttpContent(new DefaultLastHttpContent(
//                Unpooled.wrappedBuffer("testRequestHeaders".getBytes(Charset.defaultCharset()))));
//        HttpCarbonMessage clonedMsg = outboundRequestMsg.cloneCarbonMessageWithData();
//
//        Assert.assertEquals(clonedMsg.getNettyHttpRequest().method(), HttpMethod.POST);
//        Assert.assertEquals(clonedMsg.getNettyHttpRequest().protocolVersion(), HttpVersion.HTTP_1_1);
//        Assert.assertEquals(clonedMsg.getProperty(Constants.TO), "/hello");
//        Assert.assertEquals(clonedMsg.getHeaders().getAll("aaa").size(), 2);
//        Assert.assertEquals(clonedMsg.getHeaders().getAll("aaa").get(0), "123");
//        Assert.assertEquals(clonedMsg.getHeaders().getAll("aaa").get(1), "xyz");
//    }
//
//    @Test(description = "Test cloneCarbonMessageWithOutData to Http response with duplicate header keys")
//    public void cloneCarbonMessageWithOutData() {
//        HttpHeaders headers = new DefaultHttpHeaders();
//        headers.set("aaa", "123");
//        headers.add("aaa", "xyz");
//        HttpCarbonMessage outboundResponseMsg = new HttpCarbonMessage(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
//                HttpResponseStatus.OK, headers));
//        HttpCarbonMessage clonedMsg = outboundResponseMsg.cloneCarbonMessageWithOutData();
//
//        Assert.assertEquals(clonedMsg.getNettyHttpResponse().protocolVersion(), HttpVersion.HTTP_1_1);
//        Assert.assertEquals(clonedMsg.getNettyHttpResponse().status(), HttpResponseStatus.OK);
//        Assert.assertEquals(clonedMsg.getHeaders().getAll("aaa").size(), 2);
//        Assert.assertEquals(clonedMsg.getHeaders().getAll("aaa").get(0), "123");
//        Assert.assertEquals(clonedMsg.getHeaders().getAll("aaa").get(1), "xyz");
//    }
}
