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
package org.ballerinalang.test.services.configuration;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

public class CompressionConfigurationTest {

    private CompileResult serviceResult;
    private static final String MOCK_ENDPOINT_NAME = "mockEP";

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/services/configuration/compression-annotation-test.bal";
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test
    public void testOptionDefaultCompression() {
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("/defaultCompressionValue",
                HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString()),
                Constants.HTTP_TRANSFER_ENCODING_IDENTITY,
                "The content-encoding header should be identity");
    }

    @Test
    public void testOptionCompressionEnabled() {
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("/explicitlyCompressionEnabled",
                HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString()),
                Constants.HTTP_TRANSFER_ENCODING_IDENTITY,
                "The content-encoding header should be identity");
    }

    @Test
    public void testOptionCompressionDisabled() {
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("/explicitlyCompressionDisabled",
                HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertNull(response.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString()),
                "The content-encoding header should be null");
    }
}
