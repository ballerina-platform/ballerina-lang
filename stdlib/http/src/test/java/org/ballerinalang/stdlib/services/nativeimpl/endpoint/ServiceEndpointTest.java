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

package org.ballerinalang.stdlib.services.nativeimpl.endpoint;

import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
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

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Test cases for ballerina/http.ServiceEndpoint.
 */
public class ServiceEndpointTest {
    private static final String LOCAL_ADDRESS = "LOCAL_ADDRESS";
    private static final int SERVICE_EP_PORT = 9090;

    @BeforeClass
    public void setup() {
        String filePath = "test-src/services/nativeimpl/endpoint/service-endpoint-test.bal";
        BCompileUtil.compile(filePath);
    }

    @Test(description = "Test the protocol value of ServiceEndpoint struct within a service")
    public void testGetProtocolConnectionStruct() {
        String protocolValue = "http";
        String path = "/hello/protocol";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(SERVICE_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 200);

        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("protocol").stringValue(), protocolValue);
    }

    @Test(description = "Test the local struct values of the ServiceEndpoint struct within a service")
    public void testLocalStructInConnection() throws UnknownHostException {
        String path = "/hello/local";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        String expectedHost = ((InetSocketAddress) cMsg.getProperty(LOCAL_ADDRESS)).getHostName();
        String expectedPort = "9090";
        String expectedMessage = "{\"local\":{\"host\":\"" + expectedHost + "\", \"port\":9090}}";

        HttpCarbonMessage response = Services.invoke(SERVICE_EP_PORT, cMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 200);

        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.stringValue(), expectedMessage, "Local address does not populated correctly.");

        BMap<String, BValue> local = (BMap<String, BValue>) ((BMap<String, BValue>) bJson).get("local");
        String host = local.get("host").stringValue();
        String port = local.get("port").stringValue();

        Assert.assertEquals(host, expectedHost, "Host does not populated correctly.");
        Assert.assertEquals(port, expectedPort, "Port does not populated correctly.");
    }
}
