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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.EnvironmentInitializer;
import org.wso2.ballerina.core.nativeimpl.connectors.http.server.HTTPResourceDispatcher;
import org.wso2.ballerina.core.runtime.registry.DispatcherRegistry;
import org.wso2.ballerina.core.utils.MessageUtils;
import org.wso2.ballerina.lang.util.Services;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Service/Resource dispatching test class
 */
public class ServiceTest {

    @BeforeClass
    public void setup() {
        EnvironmentInitializer.initialize("lang/service/echoService.bal");
    }

    @Test
    public void testServiceDispatching() {

        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);
        // TODO: Improve with more assets
    }

    @Test
    public void testProtocolAvailabilityCheck() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "GET");
        cMsg.removeProperty(org.wso2.carbon.messaging.Constants.PROTOCOL);
        String errorMsg = "Check for protocol logic absent";
        try {
            Services.invoke(cMsg);
            Assert.fail(errorMsg);
        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage().contains("Protocol not defined"), errorMsg);
        }
    }

    @Test
    public void testServiceDispatcherAvailabilityCheck() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "GET");
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, "FOO");   // setting incorrect protocol
        String errorMsg = "Check for service dispatcher not found";
        try {
            Services.invoke(cMsg);
            Assert.fail(errorMsg);
        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage().contains("No service dispatcher available"), errorMsg);
        }
    }

    @Test
    public void testServiceAvailabilityCheck() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/foo/message", "GET");
        String errorMsg = "Check for service not found logic absent";
        try {
            Services.invoke(cMsg);
            Assert.fail(errorMsg);
        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage().contains("No Service found"), errorMsg);
        }
    }

    @Test
    public void testResourceDispatcherAvailabilityCheck() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "GET");
        DispatcherRegistry.getInstance().unregisterResourceDispatcher("http"); // Remove http resource dispatcher
        String errorMsg = "Check for not resource dispatcher logic absent";
        try {
            Services.invoke(cMsg);
            Assert.fail(errorMsg);
        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage().contains("No resource dispatcher available"), errorMsg);
        }
        DispatcherRegistry.getInstance().registerResourceDispatcher(new HTTPResourceDispatcher()); // Add back
    }

    @Test
    public void testResourceAvailabilityCheck() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/bar", "GET");
        String errMessage = "Check for resource not found logic absent";
        try {
            Services.invoke(cMsg);
            Assert.fail(errMessage);
        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage().contains("No Resource found"),
                              errMessage);
        }
    }

    //TODO: add more test cases

}
