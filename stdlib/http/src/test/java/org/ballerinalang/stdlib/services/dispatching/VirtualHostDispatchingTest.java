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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BServiceUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service dispatching test cases for virtual hosting.
 */
public class VirtualHostDispatchingTest {

    private static final int EP_PORT = 9090;

    @BeforeClass
    public void setup() {
        BCompileUtil.compile("test-src/services/dispatching/virtual-host-test.bal");
    }

    @Test()
    public void testInvokingTwoServicesWithDifferentHostsAndSameBasePaths() {
        String hostName1 = "abc.com";
        String hostName2 = "xyz.org";
        HTTPTestRequest request = MessageUtils.generateHTTPMessage("/page/index", "GET");
        request.setHeader(HttpHeaderNames.HOST.toString(), hostName1);
        HttpCarbonMessage response = Services.invoke(EP_PORT, request);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), hostName1,
                "Incorrect resource invoked.");

        request = MessageUtils.generateHTTPMessage("/page/index", "GET");
        request.setHeader(HttpHeaderNames.HOST.toString(), hostName2);
        response = Services.invoke(EP_PORT, request);

        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), hostName2,
                "Incorrect resource invoked.");
    }

    @Test()
    public void testInvokingTwoServicesWithAndWithoutHosts() {
        String hostName1 = "abc.com";
        HTTPTestRequest request = MessageUtils.generateHTTPMessage("/page/index", "GET");
        request.setHeader(HttpHeaderNames.HOST.toString(), hostName1);
        HttpCarbonMessage response = Services.invoke(EP_PORT, request);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), hostName1,
                "Incorrect resource invoked.");

        request = MessageUtils.generateHTTPMessage("/page/index", "GET");
        response = Services.invoke(EP_PORT, request);

        Assert.assertNotNull(response, "Response message not found");
        bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("echo").stringValue(), "no host",
                "Incorrect resource invoked.");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    ".*two services have the same basePath : '/page' under host name : 'abc.com'.*")
    public void testTwoServicesWithSameHostandBasePath() {
        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "services", "dispatching");
        CompileResult compileResult = BCompileUtil.compile(
                sourceRoot.resolve("virtual-host-negative-test.bal").toString());
        BServiceUtil.runService(compileResult);
    }
}
