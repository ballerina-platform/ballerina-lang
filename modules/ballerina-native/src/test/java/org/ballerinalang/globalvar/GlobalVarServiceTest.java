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

import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.testutils.EnvironmentInitializer;
import org.ballerinalang.testutils.MessageUtils;
import org.ballerinalang.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Service/Resource dispatchers test class.
 */
public class GlobalVarServiceTest {

    BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = EnvironmentInitializer.setup("lang/globalvar/global-var-service.bal");
    }

    @Test(description = "Test defining global variables in services")
    public void testDefiningGlobalVarInService() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/globalvar/defined", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarInt":800, "glbVarString":"value", "glbVarFloat":99.34323}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("glbVarInt").asText(), "800");
        Assert.assertEquals(bJson.value().get("glbVarString").asText(), "value");
        Assert.assertEquals(bJson.value().get("glbVarFloat").asText(), "99.34323");
    }

    @Test(description = "Test accessing global variables in service level")
    public void testAccessingGlobalVarInServiceLevel() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/globalvar/access-service-level", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"serviceVarFloat":99.34323}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("serviceVarFloat").asText(), "99.34323");
    }

    @Test(description = "Test changing global variables in resource level")
    public void testChangingGlobalVarInResourceLevel() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/globalvar/change-resource-level", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarFloatChange":77.87}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("glbVarFloatChange").asText(), "77.87");
    }

    @Test(description = "Test accessing changed global var in another resource in same service")
    public void testAccessingChangedGlobalVarInAnotherResource() {
        CarbonMessage cMsgChange = MessageUtils.generateHTTPMessage("/globalvar/change-resource-level", "GET");
        Services.invoke(cMsgChange);

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/globalvar/get-changed-resource-level", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarFloatChange":77.87}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("glbVarFloatChange").asText(), "77.87");
    }

    @Test(description = "Test accessing changed global var in another resource in different service")
    public void testAccessingChangedGlobalVarInAnotherResourceInAnotherService() {
        CarbonMessage cMsgChange = MessageUtils.generateHTTPMessage("/globalvar/change-resource-level", "GET");
        Services.invoke(cMsgChange);

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/globalvar-second/get-changed-resource-level", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);
        //Expected Json message : {"glbVarFloatChange":77.87}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("glbVarFloatChange").asText(), "77.87");
    }

//    @Test(description = "Test for protocol availability check", expectedExceptions = {BallerinaException.class},
//            expectedExceptionsMessageRegExp = ".* protocol not defined .*")
//    public void testProtocolAvailabilityCheck() {
//        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "GET");
//        cMsg.removeProperty(org.wso2.carbon.messaging.Constants.PROTOCOL);
//        Services.invoke(cMsg);
//    }
//
//    @Test(description = "Test for service dispatcher availability check",
//            expectedExceptions = {BallerinaException.class},
//            expectedExceptionsMessageRegExp = ".* no service dispatcher available .*")
//    public void testServiceDispatcherAvailabilityCheck() {
//        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "GET");
//        cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, "FOO");   // setting incorrect protocol
//        Services.invoke(cMsg);
//    }
//
//    @Test(description = "Test for service availability check",
//            expectedExceptions = {BallerinaException.class},
//            expectedExceptionsMessageRegExp = ".* no service found .*")
//    public void testServiceAvailabilityCheck() {
//        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/foo/message", "GET");
//        Services.invoke(cMsg);
//    }
//
//    @Test(description = "Test for resource dispatcher availability check",
//            expectedExceptions = {BallerinaException.class},
//            expectedExceptionsMessageRegExp = ".* no resource dispatcher available .*")
//    public void testResourceDispatcherAvailabilityCheck() {
//        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "GET");
//        DispatcherRegistry.getInstance().unregisterResourceDispatcher("http"); // Remove http resource dispatcher
//        try {
//            Services.invoke(cMsg);
//        } finally {
//            DispatcherRegistry.getInstance().registerResourceDispatcher(new HTTPResourceDispatcher()); // Add back
//        }
//    }
//
//    @Test(description = "Test for resource availability check",
//            expectedExceptions = {BallerinaException.class},
//            expectedExceptionsMessageRegExp = ".* no resource found .*")
//    public void testResourceAvailabilityCheck() {
//        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/bar", "GET");
//        Services.invoke(cMsg);
//    }
//
//    @Test
//    public void testSetString() {
//        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/setString", "POST");
//        cMsg.addMessageBody(ByteBuffer.wrap("hello".getBytes()));
//        cMsg.setEndOfMsgAdded(true);
//        CarbonMessage response = Services.invoke(cMsg);
//
//        Assert.assertNotNull(response);
//    }
//
//    @Test
//    public void testGetString() {
//        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/getString", "GET");
//        CarbonMessage response = Services.invoke(cMsg);
//        Assert.assertNotNull(response);
//
//        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
//        Assert.assertNotNull(stringDataSource);
//        Assert.assertEquals(stringDataSource.getValue(), "");
//    }
//
//    @Test
//    public void testGetStringAfterSetString() {
//        CarbonMessage setStringCMsg = MessageUtils.generateHTTPMessage("/echo/setString", "POST");
//        String stringPayload = "hello";
//        setStringCMsg.addMessageBody(ByteBuffer.wrap(stringPayload.getBytes()));
//        setStringCMsg.setEndOfMsgAdded(true);
//        Services.invoke(setStringCMsg);
//
//        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/getString", "GET");
//        CarbonMessage response = Services.invoke(cMsg);
//        Assert.assertNotNull(response);
//
//        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
//        Assert.assertNotNull(stringDataSource);
//        Assert.assertEquals(stringDataSource.getValue(), stringPayload);
//    }


    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(bLangProgram);
    }

    //TODO: add more test cases

}
