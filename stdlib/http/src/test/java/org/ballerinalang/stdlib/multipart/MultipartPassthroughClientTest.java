/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.multipart;

import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.ResponseReader;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * Test cases for multipart passthrough client.
 */
public class MultipartPassthroughClientTest {

    private static final int TEST_CLIENT_PORT = 9090;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/multipart/multipart-passthrough-client.bal";
        CompileResult result = BCompileUtil.compile(sourceFilePath);
        if (result.getErrorCount() > 0) {
            Assert.fail("Compilation errors");
        }
    }

    @Test(description = "Test multipart passthrough without consuming")
    public void testMultipartPassthroughWithoutConsuming() {
        String path = "/test/test1";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(TEST_CLIENT_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "This is a text part");
    }

    @Test(description = "Test multipart passthrough with consuming")
    public void testMultipartPassthroughWithConsuming() {
        String path = "/test/test2";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(TEST_CLIENT_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "This is a text part");
    }

    @Test(description = "Test multipart passthrough with type parameter")
    public void testMultipartPassthroughWithTypeParam() {
        String path = "/test/test3";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(TEST_CLIENT_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "This is a text part, " +
                "<name>This is an xml part</name>");
    }
}
