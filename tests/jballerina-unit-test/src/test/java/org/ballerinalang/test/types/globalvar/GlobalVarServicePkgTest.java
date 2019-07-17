/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.globalvar;

import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Testing global variables in services with package.
 */
public class GlobalVarServicePkgTest {

    CompileResult result;
    private static final String MOCK_ENDPOINT_NAME = "globalVarEP";
    private static final int MOCK_ENDPOINT_PORT = 9090;
    private static final String PKG_NAME = "globalvar.pkg.srvc";
    private static final String ORG_PKG_NAME = "globalvar.pkg.main/" + PKG_NAME;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/variabledef/TestProj", PKG_NAME);
    }

    @Test(description = "Test accessing global variables in other packages")
    public void testAccessingGlobalVarInOtherPackages() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/defined", "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg);
        Assert.assertNotNull(response);
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap) bJson).get("glbVarInt").stringValue(), "22343");
        Assert.assertEquals(((BMap) bJson).get("glbVarString").stringValue(), "stringval");
        Assert.assertEquals(((BMap) bJson).get("glbVarFloat").stringValue(), "6342.234234");
    }

    @Test(description = "Test assigning global variable from other package when defining")
    public void testAssignGlobalVarFromOtherPkgWhenDefining() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/assign-from-other-pkg", "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarFloat1":6342.234234}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap) bJson).get("glbVarFloat1").stringValue(), "6342.234234");
    }

    @Test(description = "Test assigning function invocation from same package")
    public void testAssigningFuncInvFromSamePkg() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/func-inv-from-same-pkg", "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarFunc":423277.72343}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap) bJson).get("glbVarFunc").stringValue(), "423277.72343");
    }

    @Test(description = "Test assigning function invocation from different package")
    public void testAssigningFuncInvFromDiffPkg() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/func-inv-from-diff-pkg", "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarPkgFunc":8876}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap) bJson).get("glbVarPkgFunc").stringValue(), "8876");
    }

    @Test(description = "Test assigning global variable to service variable from different package")
    public void testAssigningGlobalVarToServiceVarFromDiffPkg() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/assign-to-service-var-from-diff-pkg",
                                                                  "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"serviceVarString":"stringval"}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap) bJson).get("serviceVarString").stringValue(), "stringval");
    }

    @Test(description = "Test change global var in different package and access it")
    public void testChangeAndAccessGlobalVarInDiffPkg() {
        HTTPTestRequest cMsgChange =
                MessageUtils.generateHTTPMessage("/globalvar-pkg/change-global-var-diff-pkg", "GET");
        Services.invoke(MOCK_ENDPOINT_PORT, cMsgChange);

        HTTPTestRequest cMsg =
                MessageUtils.generateHTTPMessage("/globalvar-second-pkg/get-changed-resource-level", "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"changeVarFloat":345432.454}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(((BMap) bJson).get("changeVarFloat").stringValue(), "345432.454");
    }

    @AfterClass
    public void tearDown() {
        // EnvironmentInitializer.cleanup(result);
    }
}
