/*
 * Copyright (c) 2020, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specif ic language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.services.dispatching;

import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Test case for path parameter in the Resource config.
 */
public class ResourceConfigTest {

    private static final int SERVICE_EP_PORT = 9090;
    @BeforeClass
    public void setup() {
        BCompileUtil.compile("test-src/services/dispatching/defined_constant.bal");
    }

    @Test(description = "Test the path parameter in the Resource config which supports defined constant.")
    public void testConfigParameter() {
        String value = "Hi";
        String path = "/hello/id";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(SERVICE_EP_PORT, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 200);
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("message").stringValue(), value);
    }
}
