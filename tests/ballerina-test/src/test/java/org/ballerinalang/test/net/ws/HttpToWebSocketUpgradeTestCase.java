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
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.net.http.BallerinaHttpServerConnector;
import org.ballerinalang.net.ws.Constants;
import org.testng.annotations.Test;

/**
 * Test Http Endpoint to WebSocket Endpoint Upgrade.
 *
 * TODO: This test case should be changed after service pointers are introduced.
 */
public class HttpToWebSocketUpgradeTestCase {

    @Test(priority = 0)
    public void testSuccessfulUpgradeEndpointWithConfigAnnotation() {
        CompileResult compileResult =
                BServiceUtil.setupProgramFile(this,
                                              "test-src/net/ws/http-to-websocket-upgrade-without-base-path.bal");
        validateSeverEndpoints(compileResult);
    }

    @Test(priority = 1)
    public void testSuccessfulUpgradeEndpointWithoutConfigAnnotation() {
        CompileResult compileResult =
                BServiceUtil.setupProgramFile(this,
                                              "test-src/net/ws/http-to-websocket-upgrade-without-upgrade-ann.bal");
        validateSeverEndpoints(compileResult);
    }

    @Test(priority = 2)
    public void testSuccessfulUpgradeEndpointWithBasePathInBothEndpoints() {
        CompileResult compileResult =
                BServiceUtil.setupProgramFile(this, "test-src/net/ws/http-to-websocket-upgrade-both-base-paths.bal");
        validateSeverEndpoints(compileResult);
    }

    @Test(priority = 3, expectedExceptions = BallerinaConnectorException.class,
          expectedExceptionsMessageRegExp = "Cannot find a WebSocket service for service name wsServic")
    public void testWrongServiceName() {
        CompileResult compileResult = BServiceUtil.setupProgramFile(
                this, "test-src/net/ws/http-to-websocket-upgrade-service-name-negative.bal");
        validateSeverEndpoints(compileResult);
    }

    @Test(priority = 4, expectedExceptions = BallerinaConnectorException.class,
          expectedExceptionsMessageRegExp =
                  "service wsService: cannot define host, port configurations without base path")
    public void testHostPortWithoutBasePath() {
        CompileResult compileResult = BServiceUtil.setupProgramFile(
                this, "test-src/net/ws/http-to-websocket-upgrade-host-port-without-basepath-negative.bal");
        validateSeverEndpoints(compileResult);
    }

    private void validateSeverEndpoints(CompileResult compileResult) throws BallerinaConnectorException {
        BallerinaHttpServerConnector httpServerConnector = (BallerinaHttpServerConnector) ConnectorUtils.
                getBallerinaServerConnector(compileResult.getProgFile(), Constants.HTTP_PACKAGE_PATH);
        httpServerConnector.getWebSocketServicesRegistry().completeDeployment();
    }
}
