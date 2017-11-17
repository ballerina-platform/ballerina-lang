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

package org.ballerinalang.test.net.ws;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.net.ws.WebSocketServicesRegistry;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * Test case to check registration and un-registration of WebSocket services.
 */
public class WebSocketServiceRegistrationTestCase {

    @Test(priority = 1)
    public void testRegistration() {
        CompileResult compileResult1 =
                BServiceUtil.setupProgramFile(this,
                                              "test-src/net/ws/service-registering-unregistering-test.bal");
        WebSocketServicesRegistry.getInstance().deployServices();
        BServiceUtil.cleanup(compileResult1);
        CompileResult compileResult2 =
                BServiceUtil.setupProgramFile(this,
                                              "test-src/net/ws/service-registering-unregistering-test.bal");
        WebSocketServicesRegistry.getInstance().deployServices();
        BServiceUtil.cleanup(compileResult2);
    }

    @Test(priority = 2, expectedExceptions = BallerinaException.class)
    public void testRegistrationOfServiceWithoutUnregistering() {
        CompileResult compileResult1 =
                BServiceUtil.setupProgramFile(this,
                                              "test-src/net/ws/service-registering-unregistering-test.bal");
        WebSocketServicesRegistry.getInstance().deployServices();
        CompileResult compileResult2 =
                BServiceUtil.setupProgramFile(this,
                                              "test-src/net/ws/service-registering-unregistering-test.bal");
        WebSocketServicesRegistry.getInstance().deployServices();
        BServiceUtil.cleanup(compileResult1);
        BServiceUtil.cleanup(compileResult2);
    }

    @AfterClass
    public void cleanUp() {
        WebSocketServicesRegistry.getInstance().cleanRegistry();
    }

}
