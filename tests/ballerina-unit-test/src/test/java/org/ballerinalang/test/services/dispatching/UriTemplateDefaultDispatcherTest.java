/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.services.dispatching;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Test class for Uri Template default dispatchers.
 */
public class UriTemplateDefaultDispatcherTest {

    private static final String TEST_EP = "testEP";
    private CompileResult application;

    @BeforeClass()
    public void setup() {
        application = BServiceUtil
                .setupProgramFile(this, "test-src/services/dispatching/uri-template-default.bal");
    }

    @Test(description = "Test dispatching with Service name when basePath is not defined and resource path empty")
    public void testServiceNameDispatchingWhenBasePathUndefined() {
        String path = "/serviceName/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), "dispatched to service name"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching when resource annotation unavailable")
    public void testServiceNameDispatchingWithEmptyBasePath() {
        String path = "/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(),
                "dispatched to empty service name", "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with Service name when annotation is not available")
    public void testServiceNameDispatchingWhenAnnotationUnavailable() {
        String path = "/serviceWithNoAnnotation/test1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(),
                "dispatched to a service without an annotation", "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with Service name when annotation is not available")
    public void testPureProxyService() {
        String path = "/";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), "dispatched to a proxy service"
                , "Resource dispatched to wrong template");
    }

    @Test(description = "Test dispatching with default resource")
    public void testDispatchingToDefault() {
        String path = "/serviceEmptyName/hello";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(application, TEST_EP, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), "dispatched to a proxy service"
                , "Resource dispatched to wrong template");
    }
}
