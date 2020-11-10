/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.services.dispatching;

import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.util.StringUtils;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Test class for Matrix parameters based dispatching.
 */
public class UriMatrixParametersMatchTest {

    private static final int TEST_EP = 9090;
    private CompileResult application;

    @BeforeClass()
    public void setup() {
        application = BCompileUtil.compile("test-src/services/dispatching/uri-matrix-param-matching.bal");
    }

    @Test
    public void testMatrixParamsAndQueryParamsMatching() {
        String path = "/hello/t1/john;age=10;color=white/bar/1991;month=may;day=12/foo;a=5;b=10?x=10&y=5";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("pathParams").stringValue(), "john, 1991");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("personMatrix").stringValue(), "age=10;color=white");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("yearMatrix").stringValue(), "month=may;day=12");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("fooMatrix").stringValue(), "a=5;b=10");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("queryParams").stringValue(), "x=10&y=5");
    }

    @Test
    public void testEncodedPathDispatching() {
        String path = "/hello/t2/john;age=2;color=white/foo%3Ba%3D5%3Bb%3D10"; // encoded URI
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("person").stringValue(), "john");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("personParamSize").stringValue(), "2");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("fooParamSize").stringValue(), "0");
    }

    @Test
    public void testEncodedPathParamDispatching() {
        String path = "/hello/t2/john%3Bage%3D2%3Bcolor%3Dwhite/foo%3Ba%3D5%3Bb%3D10"; // encoded URI
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());

        Assert.assertEquals(((BMap<String, BValue>) bJson).get("person").stringValue(), "john;age=2;color=white");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("personParamSize").stringValue(), "0");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("fooParamSize").stringValue(), "0");
    }

    @Test
    public void testNonEncodedUrlDispatching() {
        String path = "/hello/t2/john;age=2;color=white/foo;a=5;b=10"; // encoded URI
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 404, "Response code mismatch");
        //checking the exception message
        String errorMessage = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(errorMessage, "Message body null");
        Assert.assertTrue(errorMessage.contains("no matching resource found for path"),
                          "Expected error not found.");
    }

    @Test
    public void testErrorReportInURI() {
        String path = "/hello/t2/john;age;color=white/foo;a=5;b=10"; // encoded URI
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 500, "Response code mismatch");
        //checking the exception message
        String errorMessage = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(errorMessage, "Message body null");
        Assert.assertTrue(errorMessage.contains("found non-matrix parameter"),
                          "Expected error not found.");
    }
}
