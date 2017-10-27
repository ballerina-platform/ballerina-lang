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

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.net.ws.WebSocketServicesRegistry;
import org.testng.annotations.Test;

/**
 * Test Http Endpoint to WebSocket Endpoint Upgrade.
 *
 * TODO: This test case should be changed after service pointers are introduced.
 */
public class HttpToWebSocketUpgradeTestCase {

    @Test
    public void testSuccessfulUpgradeEndpointWithConfigAnnotation() {
        CompileResult compileResult =
                BServiceUtil.setupProgramFile(this,
                                              "test-src/net/ws/http-to-websocket-upgrade-without-base-path.bal");
        WebSocketServicesRegistry.getInstance().validateSeverEndpoints();
        BServiceUtil.cleanup(compileResult);
    }

    @Test
    public void testSuccessfulUpgradeEndpointWithoutConfigAnnotation() {
        CompileResult compileResult =
                BServiceUtil.setupProgramFile(this,
                                              "test-src/net/ws/http-to-websocket-upgrade-without-upgrade-ann.bal");
        WebSocketServicesRegistry.getInstance().validateSeverEndpoints();
        BServiceUtil.cleanup(compileResult);
    }

    @Test
    public void testSuccessfulUpgradeEndpointWithBasePathInBothEndpoints() {
        CompileResult compileResult =
                BServiceUtil.setupProgramFile(this, "test-src/net/ws/http-to-websocket-upgrade-both-base-paths.bal");
        WebSocketServicesRegistry.getInstance().validateSeverEndpoints();
        BServiceUtil.cleanup(compileResult);
    }

    @Test(expectedExceptions = BallerinaConnectorException.class,
          expectedExceptionsMessageRegExp = "Could not find a WebSocket service for the service name: wsServic")
    public void testWrongServiceName() {
        CompileResult compileResult = BServiceUtil.setupProgramFile(
                this, "test-src/net/ws/http-to-websocket-upgrade-service-name-negative.bal");
        WebSocketServicesRegistry.getInstance().validateSeverEndpoints();
        BServiceUtil.cleanup(compileResult);
    }

    @Test(expectedExceptions = BallerinaConnectorException.class,
          expectedExceptionsMessageRegExp =
                  "service wsService: cannot define host, port configurations without base path")
    public void testHostPortWithoutBasePath() {
        CompileResult compileResult = BServiceUtil.setupProgramFile(
                this, "test-src/net/ws/http-to-websocket-upgrade-host-port-without-basepath-negative.bal");
        WebSocketServicesRegistry.getInstance().validateSeverEndpoints();
        BServiceUtil.cleanup(compileResult);
    }
}
