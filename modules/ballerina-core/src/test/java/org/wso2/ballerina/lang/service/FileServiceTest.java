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

package org.wso2.ballerina.lang.service;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.EnvironmentInitializer;
import org.wso2.ballerina.core.TestCallback;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.runtime.ServerConnectorMessageHandler;
import org.wso2.ballerina.core.runtime.dispatching.file.Constants;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.StreamingCarbonMessage;

/**
 * Service dispatching test class for vfs.
 */
public class FileServiceTest {
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = EnvironmentInitializer.setup("lang/service/serviceLevelVariable.bal");
    }

    @Test(description = "Test the exception when the service name is not provided with the file "
            + "streaming message")
    public void testServiceAvailabilityWithoutServiceName() {
        try {
            CarbonMessage cMsg = new StreamingCarbonMessage(null);
            cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.PROTOCOL_FILE);
            ServerConnectorMessageHandler.handleInbound(cMsg, new TestCallback());
            Assert.fail("Expectation is not thrown when the service name is not provided in the file streaming "
                    + "message");
        } catch (BallerinaException ex) {
            Assert.assertEquals(ex.getCause().getMessage(),
                    "org.wso2.ballerina.core.exception.BallerinaException: error in ballerina program: service name "
                        + "is not found " + "with the file input stream.", "Expected error message is not received");
        }
    }

    @Test(description = "Test the exception when the wrong service name is provided")
    public void testServiceAvailabilityWithWrongServiceName() {
        try {
            CarbonMessage cMsg = new StreamingCarbonMessage(null);
            cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.PROTOCOL_FILE);
            cMsg.setProperty(Constants.TRANSPORT_PROPERTY_SERVICE_NAME, "abc");
            ServerConnectorMessageHandler.handleInbound(cMsg, new TestCallback());
            Assert.fail("Expectation is not thrown when a wrong service name is not provided in the file streaming "
                    + "message");
        } catch (BallerinaException ex) {
            Assert.assertEquals(ex.getCause().getMessage(),
                    "org.wso2.ballerina.core.exception.BallerinaException: error in ballerina program: no file "
                            + "service is " + "registered with the service name abc", "Exception is not thrown when "
                            + "the service is not found to serve the request");
        }
    }

    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(bLangProgram);
    }

}
