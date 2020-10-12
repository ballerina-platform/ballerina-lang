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

package org.ballerinalang.stdlib.services.dispatching;

import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.util.StringUtils;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service versioning dispatching related test cases.
 */
public class VersioningDispatchingTest {

    private static final int MOCK_ENDPOINT_1_PORT = 9090;
    private static final String PKG_NAME = "abc.xyz";

    @BeforeClass
    public void setup() {
        Path sourceRoot = Paths.get("test-src", "services", "dispatching", "versioning");
        BCompileUtil.compile(sourceRoot.resolve("successcase1").toString(), PKG_NAME);
    }

    @Test(description = "Test dispatching with version template, no version allow and match major configs",
          dataProvider = "defaultService")
    public void testWithVersionTemplateNoVersionAllowMatchMajor(String path) {
        HTTPTestRequest request = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_1_PORT, request);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("hello").stringValue(), "common service"
                , "Incorrect resource invoked.");
    }

    @DataProvider(name = "defaultService")
    public static Object[][] defaultService() {
        return new Object[][]{
                {"/hello1/v1.5/go"},
                {"/hello1/go"},
                {"/hello1/v1/go"}
        };
    }

    @Test(description = "Test dispatching with form param without annotation")
    public void testWithVersionTemplate() {
        String path = "/1.5/bar/go";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_1_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("hello").stringValue(), "Only template"
                , "Incorrect resource invoked.");
    }

    @Test(description = "Test dispatching with form param without annotation", dataProvider = "onlyTemplate")
    public void testWithVersionTemplateNegative(String path) {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_1_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 404, "Response code mismatch");
        String errorMessage = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(errorMessage, "Message body null");
        Assert.assertTrue(errorMessage.contains("no matching service found for path"),
                          "Expected error not found.");
    }

    @DataProvider(name = "onlyTemplate")
    public static Object[][] onlyTemplate() {
        return new Object[][]{
                {"/var/go"},
                {"/v1/bar/go"}
        };
    }

    @Test(description = "Test dispatching with form param without annotation", dataProvider = "allowNoVersion")
    public void testWithAllowNoVersion(String path) {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_1_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("hello").stringValue(), "only allow no version"
                , "Incorrect resource invoked.");
    }

    @DataProvider(name = "allowNoVersion")
    public static Object[][] allowNoVersion() {
        return new Object[][]{
                {"/hello3/v1.5/bar/go"},
                {"/hello3/bar/go"}
        };
    }

    @Test(description = "Test dispatching with form param without annotation")
    public void testWithAllowNoVersionNegative() {
        String path = "/hello3/v1/bar/go";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_1_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 404, "Response code mismatch");
        String errorMessage = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(errorMessage, "Message body null");
        Assert.assertTrue(errorMessage.contains("no matching service found for path"),
                          "Expected error not found.");
    }

    @Test(description = "Test dispatching with form param without annotation", dataProvider = "matchMajor")
    public void testWithmatchMajorVersion(String path) {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_1_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("hello").stringValue(), "only match major"
                , "Incorrect resource invoked.");
    }

    @DataProvider(name = "matchMajor")
    public static Object[][] matchMajor() {
        return new Object[][]{
                {"/hello4/v1.5/bar/go"},
                {"/hello4/v1/bar/go"}
        };
    }

    @Test(description = "Test dispatching with form param without annotation")
    public void testWithmatchMajorVersionNegative() {
        String path = "/hello4/bar/go";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_1_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 404, "Response code mismatch");
        String errorMessage = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(errorMessage, "Message body null");
        Assert.assertTrue(errorMessage.contains("no matching service found for path"),
                          "Expected error not found.");
    }

    @Test(description = "Test dispatching with form param without annotation")
    public void testWithoutVersionSegmentNegative() {
        String path = "/hello5/v0.0/bar/go";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_1_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 404, "Response code mismatch");
        String errorMessage = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(errorMessage, "Message body null");
        Assert.assertTrue(errorMessage.contains("no matching service found for path"),
                          "Expected error not found.");
    }
}
