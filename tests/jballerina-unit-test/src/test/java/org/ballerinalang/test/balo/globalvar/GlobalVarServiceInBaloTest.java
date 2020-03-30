/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.balo.globalvar;

import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.balo.BaloCreator;
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

import java.io.IOException;

/**
 * Tests for global variables in a BALO usage within services test cases.
 * 
 * @since 0.975.0
 */
public class GlobalVarServiceInBaloTest {

    CompileResult result;
    private static final int MOCK_ENDPOINT_PORT = 9090;

    @BeforeClass
    public void setup() throws IOException {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "foo");
        result = BCompileUtil.compile("test-src/balo/test_balo/globalvar/test_global_var_service.bal");
    }

    @Test(description = "Test defining global variables in services")
    public void testDefiningGlobalVarInService() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar/defined", "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarInt":800, "glbVarString":"value", "glbVarFloat":99.34323}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        BMap<String, BValue> jsonObject = (BMap<String, BValue>) bJson;
        Assert.assertEquals(jsonObject.get("glbVarInt").stringValue(), "800");
        Assert.assertEquals(jsonObject.get("glbVarString").stringValue(), "value");
        Assert.assertEquals(jsonObject.get("glbVarFloat").stringValue(), "99.34323");
    }

    @Test(description = "Test accessing global arrays in resource level")
    public void testGlobalArraysInResourceLevel() {
        HTTPTestRequest cMsgChange = MessageUtils.generateHTTPMessage("/globalvar/arrays", "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsgChange);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbArrayElement":1, "glbSealedArrayElement":0,
        // "glbSealedArray2Element":3, "glbSealed2DArrayElement":0, "glbSealed2DArray2Element":2}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        BMap<String, BValue> jsonObject = (BMap<String, BValue>) bJson;
        Assert.assertEquals(jsonObject.get("glbArrayElement").stringValue(), "1");
        Assert.assertEquals(jsonObject.get("glbSealedArrayElement").stringValue(), "0");
        Assert.assertEquals(jsonObject.get("glbSealedArray2Element").stringValue(), "3");
        Assert.assertEquals(jsonObject.get("glbSealed2DArrayElement").stringValue(), "0");
        Assert.assertEquals(jsonObject.get("glbSealed2DArray2Element").stringValue(), "2");
    }

    @Test(description = "Test changing global variables in resource level")
    public void testChangingGlobalVarInResourceLevel() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar/change-resource-level", "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarFloatChange":77.87}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("glbVarFloatChange").stringValue(), "77.87");
    }

    @Test(description = "Test accessing changed global var in another resource in same service")
    public void testAccessingChangedGlobalVarInAnotherResource() {
        HTTPTestRequest cMsgChange = MessageUtils.generateHTTPMessage("/globalvar/change-resource-level", "GET");
        Services.invoke(MOCK_ENDPOINT_PORT, cMsgChange);

        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar/get-changed-resource-level", "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarFloatChange":77.87}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("glbVarFloatChange").stringValue(), "77.87");
    }

    @Test(description = "Test accessing changed global var in another resource in different service")
    public void testAccessingChangedGlobalVarInAnotherResourceInAnotherService() {
        HTTPTestRequest cMsgChange = MessageUtils.generateHTTPMessage("/globalvar/change-resource-level", "GET");
        Services.invoke(MOCK_ENDPOINT_PORT, cMsgChange);

        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/globalvar-second/get-changed-resource-level",
                                                                  "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarFloatChange":77.87}
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("glbVarFloatChange").stringValue(), "77.87");
    }

    @AfterClass
    public void tearDown() {
        BaloCreator.clearPackageFromRepository("test-src/balo/test_projects/test_project", "testorg", "foo");
    }
}
