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

import org.apache.commons.text.StringEscapeUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.io.File;
import java.nio.file.Paths;

/**
 * Facilitate the common functionality of WebSocket integration tests.
 */
public class WebSocketTestCommons extends BaseTest {
    protected static final int TIMEOUT_IN_SECS = 10;

    protected static BServerInstance serverInstance;

    /**
     * Initializes ans start Ballerina with the websocket services package.
     *
     * @throws BallerinaTestException on Ballerina related issues.
     */
    @BeforeGroups(value = "websocket-test", alwaysRun = true)
    public void start() throws BallerinaTestException {
        int[] requiredPorts =
                new int[]{21001, 21002, 21003, 21004, 21005, 21006, 21007, 21008, 21009, 21010, 21011, 21022, 21021,
                        21012, 21013, 21014, 21015, 21016, 21017, 21018, 21019, 21020, 21023, 21024, 21025, 21026,
                        21027, 21028, 21029, 21030, 21031, 21032, 21033, 21034, 21035};
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                                          "websocket").getAbsolutePath();
        String keyStore = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "ballerinaKeystore.p12").toAbsolutePath()
                        .toString());
        String trustStore = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "ballerinaTruststore.p12").toAbsolutePath()
                        .toString());
        String[] args = new String[] { "--keystore=" + keyStore, "--truststore=" + trustStore };
        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(balFile, "wsservices", null, args, requiredPorts);
    }

    @AfterGroups(value = "websocket-test", alwaysRun = true)
    public void stop() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }
}
