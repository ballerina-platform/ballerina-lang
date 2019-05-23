/*
 * Copyright (c) 2019, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.services.nativeimpl.endpoint;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.net.UnknownHostException;

/**
 * Test cases for ballerina/http.ServiceEndpoint with sever name.
 */
public class ServiceEndpointTestwithConfig {
    private CompileResult serviceResult;
    private static final String MOCK_ENDPOINT_NAME = "mockEP";

    @BeforeClass
    public void setup() {
        String filePath = "test-src/services/nativeimpl/endpoint/service-endpoint-test-with-config.bal";
        serviceResult = BServiceUtil.setupProgramFile(this, filePath);
    }

    @Test(description = "Test the sever header name values for success response")
    public void testGetServerNameInHeader() throws UnknownHostException {
        String path = "/hello/echo";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertEquals(response.getProperty(HttpConstants.HTTP_STATUS_CODE), 200);
        Assert.assertEquals(response.getHeader("server"), "Apache");
    }

    @Test(description = "Test the sever header name values for unsuccessful request")
    public void testGetServerNameInHeader1() throws UnknownHostException {
        String path = "/hello/echo";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST);
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertEquals(response.getProperty(HttpConstants.HTTP_STATUS_CODE), 405);
        Assert.assertEquals(response.getHeader("server"), "Apache");
    }

    @Test(description = "Test the sever header name values for unsuccessful request")
    public void testGetServerNameInHeader2() throws UnknownHostException {
        String path = "";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertEquals(response.getProperty(HttpConstants.HTTP_STATUS_CODE), 404);
        Assert.assertEquals(response.getHeader("server"), "Apache");
    }

    @Test(description = "Test the server header name values which added manually")
    public void testGetServerNameInHeader3() throws UnknownHostException {
        String path = "/hello/echo1";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertEquals(response.getProperty(HttpConstants.HTTP_STATUS_CODE), 200);
        Assert.assertEquals(response.getHeader("server"), "Mysql");
    }
}
