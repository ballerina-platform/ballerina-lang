/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.globalvar;

import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.testutils.EnvironmentInitializer;
import org.ballerinalang.testutils.MessageUtils;
import org.ballerinalang.testutils.Services;
import org.ballerinalang.util.codegen.ProgramFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Testing global variables in services with package.
 */
public class GlobalVarServicePkgTest {

    ProgramFile bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = EnvironmentInitializer.setupProgramFile("lang/globalvar/pkg/main");
    }

    @Test(description = "Test accessing global variables in other packages")
    public void testAccessingGlobalVarInOtherPackages() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/defined", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarInt":22343, "glbVarString":"stringval", "glbVarFloat":6342.234234}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("glbVarInt").asText(), "22343");
        Assert.assertEquals(bJson.value().get("glbVarString").asText(), "stringval");
        Assert.assertEquals(bJson.value().get("glbVarFloat").asText(), "6342.234234");
    }

    @Test(description = "Test assigning global variable from other package when defining")
    public void testAssignGlobalVarFromOtherPkgWhenDefining() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/assign-from-other-pkg", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarFloat1":6342.234234}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("glbVarFloat1").asText(), "6342.234234");
    }

    @Test(description = "Test assigning function invocation from same package")
    public void testAssigningFuncInvFromSamePkg() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/func-inv-from-same-pkg", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarFunc":423277.72343}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("glbVarFunc").asText(), "423277.72343");
    }

    @Test(description = "Test assigning function invocation from different package")
    public void testAssigningFuncInvFromDiffPkg() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/func-inv-from-diff-pkg", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarPkgFunc":8876}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("glbVarPkgFunc").asText(), "8876");
    }

    @Test(description = "Test assigning global variable to service variable from different package")
    public void testAssigningGlobalVarToServiceVarFromDiffPkg() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/globalvar-pkg/assign-to-service-var-from-diff-pkg",
                "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"serviceVarString":"stringval"}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("serviceVarString").asText(), "stringval");
    }

    @Test(description = "Test change global var in different package and access it")
    public void testChangeAndAccessGlobalVarInDiffPkg() {
        CarbonMessage cMsgChange = MessageUtils.generateHTTPMessage("/globalvar-pkg/change-global-var-diff-pkg",
                "GET");
        Services.invoke(cMsgChange);

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/globalvar-second-pkg/get-changed-resource-level",
                "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"changeVarFloat":345432.454}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("changeVarFloat").asText(), "345432.454");
    }



    @AfterClass
    public void tearDown() {
        // EnvironmentInitializer.cleanup(bLangProgram);
    }

}
