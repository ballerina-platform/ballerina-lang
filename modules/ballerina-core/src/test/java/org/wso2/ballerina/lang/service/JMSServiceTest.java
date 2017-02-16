/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.lang.service;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.EnvironmentInitializer;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.runtime.ServerConnectorMessageHandler;
import org.wso2.ballerina.core.runtime.dispatching.jms.Constants;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;

/**
 * Testing the JMS Service Dispatcher.
 */
public class JMSServiceTest {
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = EnvironmentInitializer.setup("lang/service/serviceLevelVariable.bal");
    }

    @Test(description = "Test for exceptions when a jms message does not have a service id")
    public void testJMSServiceAvailabilityCheckWithoutJmsServiceId() {
        try {
            CarbonMessage cMsg = new TextCarbonMessage("test");
            cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.PROTOCOL_JMS);
            ServerConnectorMessageHandler.handleInbound(cMsg, null);
            Assert.fail("Exception is not thrown when a message is passed without jms service id");
        } catch (BallerinaException ex) {
            Assert.assertEquals(ex.getCause().getMessage(), "org.wso2.ballerina.core.exception.BallerinaException: "
                            + "error in ballerina program: service Id is not found in JMS Message",
                    "Exception message does not match when the message is passed without service id");
        }
    }

    @Test(description = "est for exceptions when a jms message contains non-existing service id")
    public void testJMSServiceAvailabilityWithWrongServiceId() {
        try {
            CarbonMessage cMsg = new TextCarbonMessage("test");
            cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.PROTOCOL_JMS);
            cMsg.setProperty(Constants.JMS_SERVICE_ID, "testabc");
            ServerConnectorMessageHandler.handleInbound(cMsg, null);
            Assert.fail("Exception is not thrown when a non-existing service is called");
        } catch (BallerinaException ex) {
            Assert.assertEquals(ex.getCause().getMessage(), "org.wso2.ballerina.core.exception.BallerinaException: "
                    + "error in ballerina program: no jms service is registered with the service id testabc",
                    "Exception message does not match when the message is passed is dispatched to non-existing "
                    + "service");
        }
    }

    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(bLangProgram);
    }
}
