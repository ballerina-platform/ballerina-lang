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

package org.ballerinalang.test.services.dispatching;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Service versioning dispatching related test cases.
 */
public class VersioningDispatchingTest {

    private CompileResult result1, result2, result3;
    private static final String MOCK_ENDPOINT_NAME = "passthruEP";
    private static final String PKG_NAME = "abc.xyz";
    private static final String PATH = "test-src/services/dispatching/versioning/";

    @BeforeClass
    public void setup() {
        result1 = BServiceUtil.setupProgramFile(this, PATH + "successcase1", PKG_NAME);
        result2 = BServiceUtil.setupProgramFile(this, PATH + "successcase2", PKG_NAME);
        result3 = BServiceUtil.setupProgramFile(this, PATH + "successcase3", PKG_NAME);

    }

    @Test(description = "Test dispatching with version template, no version allow and match major configs",
          dataProvider = "defaultService")
    public void testWithVersionTemplateNoVersionAllowMatchMajor(String path) {
        HTTPTestRequest request = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(result1, PKG_NAME, MOCK_ENDPOINT_NAME, request);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("hello").asText(), "common service"
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
        HTTPCarbonMessage response = Services.invokeNew(result1, PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("hello").asText(), "Only template"
                , "Incorrect resource invoked.");
    }

    @Test(description = "Test dispatching with form param without annotation", dataProvider = "onlyTemplate")
    public void testWithVersionTemplateNegative(String path) {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(result1, PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                response.getProperty(HttpConstants.HTTP_STATUS_CODE), 404, "Response code mismatch");
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
        HTTPCarbonMessage response = Services.invokeNew(result1, PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("hello").asText(), "only allow no version"
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
        HTTPCarbonMessage response = Services.invokeNew(result1, PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                response.getProperty(HttpConstants.HTTP_STATUS_CODE), 404, "Response code mismatch");
        String errorMessage = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(errorMessage, "Message body null");
        Assert.assertTrue(errorMessage.contains("no matching service found for path"),
                          "Expected error not found.");
    }

    @Test(description = "Test dispatching with form param without annotation", dataProvider = "matchMajor")
    public void testWithmatchMajorVersion(String path) {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(result1, PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("hello").asText(), "only match major"
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
        HTTPCarbonMessage response = Services.invokeNew(result1, PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                response.getProperty(HttpConstants.HTTP_STATUS_CODE), 404, "Response code mismatch");
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
        HTTPCarbonMessage response = Services.invokeNew(result1, PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                response.getProperty(HttpConstants.HTTP_STATUS_CODE), 404, "Response code mismatch");
        String errorMessage = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertNotNull(errorMessage, "Message body null");
        Assert.assertTrue(errorMessage.contains("no matching service found for path"),
                          "Expected error not found.");
    }

    @Test(description = "Test dispatching with empty version", dataProvider = "emptyVersionService")
    public void testWithEmptyVersion(String path) {
        HTTPTestRequest request = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(result2, PKG_NAME, MOCK_ENDPOINT_NAME, request);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("hello").asText(), "common service"
                , "Incorrect resource invoked.");
    }

    @DataProvider(name = "emptyVersionService")
    public static Object[][] emptyVersionService() {
        return new Object[][]{
                {"/echo/v0.0/go"},
                {"/echo/go"},
                {"/echo/v0/go"}
        };
    }

    @Test(description = "Test dispatching with single version", dataProvider = "singleVersionService")
    public void testWithOnlyMajorPackageVersion(String path) {
        HTTPTestRequest request = MessageUtils.generateHTTPMessage(path, "GET");
        HTTPCarbonMessage response = Services.invokeNew(result3, PKG_NAME, MOCK_ENDPOINT_NAME, request);

        Assert.assertNotNull(response, "Response message not found");
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("hello").asText(), "common service"
                , "Incorrect resource invoked.");
    }

    @DataProvider(name = "singleVersionService")
    public static Object[][] singleVersionService() {
        return new Object[][]{
                {"/sample/vxxx/go"},
                {"/sample/go"},
                {"/sample/vxxx/go"}
        };
    }
}
