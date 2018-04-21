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

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Testing global variables in services with package.
 */
public class GlobalVarServicePkgTest {

    CompileResult result;
    private static final String MOCK_ENDPOINT_NAME = "globalVarEP";
    private static final String PKG_NAME = "globalvar.pkg.srvc";
    private static final String ORG_PKG_NAME = "globalvar.pkg.main." + PKG_NAME;

    @BeforeClass
    public void setup() {
        result = BServiceUtil.setupProgramFile(this, "test-src/statements/variabledef/", PKG_NAME);
    }

    @Test(description = "Test accessing global variables in other packages")
    public void testAccessingGlobalVarInOtherPackages() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/defined", "GET");
        HTTPCarbonMessage response = Services.invokeNew(result, ORG_PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("glbVarInt").asText(), "22343");
        Assert.assertEquals(bJson.value().get("glbVarString").asText(), "stringval");
        Assert.assertEquals(bJson.value().get("glbVarFloat").asText(), "6342.234234");
    }

    @Test(description = "Test assigning global variable from other package when defining")
    public void testAssignGlobalVarFromOtherPkgWhenDefining() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/assign-from-other-pkg", "GET");
        HTTPCarbonMessage response = Services.invokeNew(result, ORG_PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarFloat1":6342.234234}
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("glbVarFloat1").asText(), "6342.234234");
    }

    @Test(description = "Test assigning function invocation from same package")
    public void testAssigningFuncInvFromSamePkg() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/func-inv-from-same-pkg", "GET");
        HTTPCarbonMessage response = Services.invokeNew(result, ORG_PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarFunc":423277.72343}
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("glbVarFunc").asText(), "423277.72343");
    }

    @Test(description = "Test assigning function invocation from different package")
    public void testAssigningFuncInvFromDiffPkg() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/func-inv-from-diff-pkg", "GET");
        HTTPCarbonMessage response = Services.invokeNew(result, ORG_PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarPkgFunc":8876}
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("glbVarPkgFunc").asText(), "8876");
    }

    @Test(description = "Test assigning global variable to service variable from different package")
    public void testAssigningGlobalVarToServiceVarFromDiffPkg() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/assign-to-service-var-from-diff-pkg",
                                                                  "GET");
        HTTPCarbonMessage response = Services.invokeNew(result, ORG_PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"serviceVarString":"stringval"}
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("serviceVarString").asText(), "stringval");
    }

    @Test(description = "Test change global var in different package and access it")
    public void testChangeAndAccessGlobalVarInDiffPkg() {
        HTTPTestRequest cMsgChange = MessageUtils.generateHTTPMessage("/globalvar-pkg/change-global-var-diff-pkg",
                                                                        "GET");
        Services.invokeNew(result, ORG_PKG_NAME, MOCK_ENDPOINT_NAME, cMsgChange);

        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar-second-pkg/get-changed-resource-level",
                                                                  "GET");
        HTTPCarbonMessage response = Services.invokeNew(result, ORG_PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"changeVarFloat":345432.454}
        BJSON bJson = new BJSON(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(bJson.value().get("changeVarFloat").asText(), "345432.454");
    }

    @AfterClass
    public void tearDown() {
        // EnvironmentInitializer.cleanup(result);
    }
}
