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
import org.wso2.ballerina.core.model.Application;
import org.wso2.ballerina.core.nativeimpl.connectors.file.server.Constants;
import org.wso2.ballerina.core.runtime.ServerConnectorMessageHandler;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.StreamingCarbonMessage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Service/Resource dispatching test class for vfs.
 */
public class FileServiceTest {
    private Application application;
    private final InputStream fileContent = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

    @BeforeClass public void setup() {
        application = EnvironmentInitializer.setup("lang/service/fileService.bal");
    }

    @Test(description = "Test the exception when the service name is not provided with the file "
            + "streaming message")
    public void testServiceAvailabilityWithoutServiceName() {
        try {
            CarbonMessage cMsg = new StreamingCarbonMessage(fileContent);
            cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.PROTOCOL_FILE);
            ServerConnectorMessageHandler.handleInbound(cMsg, new TestCallback());
            Assert.fail("Expectation is not thrown when the service name is not provided in the file streaming "
                    + "message");
        } catch (BallerinaException ex) {
            Assert.assertEquals(ex.getCause().getMessage(),
                    "error in ballerina program: service name is not found " + "with the file input stream.",
                    "Expected error message is not received");
        }
    }

    @Test(description = "Test the exception when the wrong service name is provided")
    public void testServiceAvailabilityWithWrongServiceName() {
        try {
            CarbonMessage cMsg = new StreamingCarbonMessage(fileContent);
            cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.PROTOCOL_FILE);
            cMsg.setProperty(Constants.TRANSPORT_PROPERTY_SERVICE_NAME, "abc");
            ServerConnectorMessageHandler.handleInbound(cMsg, new TestCallback());
            Assert.fail("Expectation is not thrown when a wrong service name is not provided in the file streaming "
                     + "message");
        } catch (BallerinaException ex) {
            Assert.assertEquals(ex.getCause().getMessage(),
                    "error in ballerina program: no file service is " + "registered with the service name abc",
                    "Exception is not thrown when the service is not found " + "to serve the request");
        }
    }

    @Test(description = "Test the exception when a streaming message is received to a file service which does not "
            + "have any resources.")
    public void testResourceAvailabilityWithCorrectServiceName() {
        try {
            CarbonMessage cMsg = new StreamingCarbonMessage(fileContent);
            cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.PROTOCOL_FILE);
            cMsg.setProperty(Constants.TRANSPORT_PROPERTY_SERVICE_NAME, "fileServiceWithoutResource");
            ServerConnectorMessageHandler.handleInbound(cMsg, new TestCallback());
             Assert.fail("Expectation is not thrown when a request is send to a file service without any resources");
        } catch (BallerinaException ex) {
            Assert.assertEquals(ex.getCause().getMessage(),
                    "error in ballerina program: no resource found to handle the request to Service : "
                            + "fileServiceWithoutResource : A Service of type 'file' has to have only one resource "
                            + "associated to itself. Found 0 resources in Service: fileServiceWithoutResource",
                    "Exception is not thrown when there is no resource found to handle the incoming message");
        }
    }

    @Test(description = "Test whether the message delivery succeeds when there is a correct file service with a "
            + "resource")
    public void testServiceWithAResource() throws BallerinaException {
        CarbonMessage cMsg = new StreamingCarbonMessage(fileContent);
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.PROTOCOL_FILE);
        cMsg.setProperty(Constants.TRANSPORT_PROPERTY_SERVICE_NAME, "fileServiceWithResource");
        ServerConnectorMessageHandler.handleInbound(cMsg, new TestCallback());
    }

    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(application);
    }

}
