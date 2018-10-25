/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.service.websocket;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.io.File;

/**
 * Facilitate the common functionality of WebSocket integration tests.
 */
public class WebSocketTestCommons extends BaseTest {
    protected static final int TIMEOUT_IN_SECS = 10;
    protected static final int REMOTE_SERVER_PORT = 15500;

    protected static BServerInstance serverInstance;

    /**
     * Initializes ans start Ballerina with the websocket services package.
     *
     * @throws BallerinaTestException on Ballerina related issues.
     */
    @BeforeGroups(value = "websocket-test", alwaysRun = true)
    public void start() throws BallerinaTestException {
        int[] requiredPorts = new int[]{
                9078, 9079, 9080, 9081, 9082, 9083, 9084, 9085, 9086, 9087, 9088, 9089, 9090, 9091, 9092, 9093, 9094,
                9095, 9096, 9097, 9098, 9099, 9100, 9200};
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "websocket").getAbsolutePath();
        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(balFile, "wsservices", requiredPorts);
    }

    @AfterGroups(value = "websocket-test", alwaysRun = true)
    public void stop() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }
}
