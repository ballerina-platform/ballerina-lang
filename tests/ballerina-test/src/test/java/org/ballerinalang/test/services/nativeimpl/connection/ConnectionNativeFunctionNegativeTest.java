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
package org.ballerinalang.test.services.nativeimpl.connection;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Test cases for ballerina.net.http.response negative native functions.
 */
public class ConnectionNativeFunctionNegativeTest {

    private CompileResult serviceResult;
    private String filePath =
            "test-src/statements/services/nativeimpl/connection/connection-native-function-negative.bal";
    private static final String MOCK_ENDPOINT_NAME = "mockEP";

    @BeforeClass
    public void setup() {
        serviceResult = BServiceUtil.setupProgramFile(this, filePath);
    }

    @Test(description = "Test respond with null parameter")
    public void testRespondWithNullParameter() {
        String path = "/hello/10";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getProperty(HttpConstants.HTTP_STATUS_CODE), 500);
        Assert.assertTrue(StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream())
                .contains("argument 1 is null"));
    }

    @Test(description = "Test respond with invalid connection struct", enabled = false)
    public void testRespondWithInvalidConnectionStruct() {
        String path = "/hello/11";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertTrue(StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream())
                .contains("operation not allowed:invalid Connection variable"));
    }

    @Test(description = "Test forward with null parameter")
    public void testForwardWithNullParameter() {
        String path = "/hello/20";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getProperty(HttpConstants.HTTP_STATUS_CODE), 500);
        Assert.assertTrue(StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream())
                .contains("argument 1 is null"));
    }

    @Test(description = "Test forward with invalid connection struct", enabled = false)
    public void testForwardWithInvalidConnectionStruct() {
        String path = "/hello/21";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        String msg = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(msg.contains("operation not allowed:invalid Connection variable"));
    }
}
