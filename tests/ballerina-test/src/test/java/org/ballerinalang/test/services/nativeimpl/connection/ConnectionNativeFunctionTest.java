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

package org.ballerinalang.test.services.nativeimpl.connection;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Test cases for ballerina/http.response native functions.
 */
public class ConnectionNativeFunctionTest {

    private CompileResult serviceResult;
    private static final String MOCK_ENDPOINT_NAME = "mockEP";

    @BeforeClass
    public void setup() {
        String filePath = "test-src/statements/services/nativeimpl/connection/connection-native-function.bal";
        serviceResult = BServiceUtil.setupProgramFile(this, filePath);
    }

    @Test(description = "Test whether the headers and status codes are set correctly.")
    public void testRedirect() {
        String path = "/hello/redirect";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getProperty(HttpConstants.HTTP_STATUS_CODE), 301);
        Assert.assertEquals(response.getHeader("Location"), "location1");
    }
}
