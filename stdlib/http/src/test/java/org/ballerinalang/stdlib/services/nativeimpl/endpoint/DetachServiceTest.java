/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.services.nativeimpl.endpoint;

import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.ResponseReader;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * Test cases for service detach method.
 */
public class DetachServiceTest {
    private static final int SERVICE_EP_PORT = 9090;

    @BeforeClass
    public void setup() {
        BCompileUtil.compile("test-src/services/nativeimpl/endpoint/service-detach-test.bal");
    }

    @Test(description = "Test the detach method with multiple services attachments")
    public void testServiceDetach() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/mock1", HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(SERVICE_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 200);
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Mock1 invoked. Mock2 attached. Mock3 attached");

        //Invoke recently attached mock2 services. Test detaching and re attaching mock3 service
        cMsg = MessageUtils.generateHTTPMessage("/mock2", HttpConstants.HTTP_METHOD_GET);
        response = Services.invoke(SERVICE_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 200);
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Mock2 resource was invoked");

        //Invoke recently attached mock3 service. That detached the mock2 service and attach mock3
        cMsg = MessageUtils.generateHTTPMessage("/mock3", HttpConstants.HTTP_METHOD_GET);
        response = Services.invoke(SERVICE_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 200);
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Mock3 invoked. Mock2 detached. Mock4 attached");

        //Invoke detached mock2 services expecting a 404
        cMsg = MessageUtils.generateHTTPMessage("/mock2", HttpConstants.HTTP_METHOD_GET);
        response = Services.invoke(SERVICE_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 404);
        Assert.assertEquals(ResponseReader.getReturnValue(response), "no matching service found for path : /mock2");

        //Invoke mock3 services again expecting a error for re-attaching already available service
        cMsg = MessageUtils.generateHTTPMessage("/mock3", HttpConstants.HTTP_METHOD_GET);
        response = Services.invoke(SERVICE_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 500);
        Assert.assertEquals(ResponseReader.getReturnValue(response),
                            "service registration failed: two services have the same basePath : '/mock4'");

        //Invoke mock4 service. mock2 service is re attached
        cMsg = MessageUtils.generateHTTPMessage("/mock4", HttpConstants.HTTP_METHOD_GET);
        response = Services.invoke(SERVICE_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 200);
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Mock4 invoked. Mock2 attached");

        //Invoke recently re-attached mock2 services
        cMsg = MessageUtils.generateHTTPMessage("/mock2", HttpConstants.HTTP_METHOD_GET);
        response = Services.invoke(SERVICE_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 200);
        Assert.assertEquals(ResponseReader.getReturnValue(response), "Mock2 resource was invoked");
    }
}
