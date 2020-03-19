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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.service.http.sample;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test whether the xml payload gets parsed properly, after the said payload has been retrieved as a byte array.
 */
@Test(groups = "http-test")
public class HttpPayloadTestCase extends HttpBaseTest {
    @Test
    public void testXmlPayload() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9118, "test/"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "W3Schools Home PageRSS Tutorial", "Message content mismatched");
    }

    @Test(description = "test getXmlPayload() for inbound response with parser errors. FullMessageListener is " +
            "notified via transport thread and thrown exception should be caught at ballerina space")
    public void testGetXmlPayloadReturnParserError() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(9118, "test/getPayloadForParseError"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(),
                            "Error occurred while extracting xml data from entity: " +
                                    "error failed to create xml: ParseError at [row,col]:[1,1]Message: " +
                                    "Content is not allowed in prolog.",
                            "Message content mismatched");
    }
}
