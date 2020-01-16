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

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;

/**
 * Test the use case when the pushText action fails.
 * The same test applies for ping, pong and pushBinary methods since the same callback is used.
 *
 * @since 0.982.0
 */
@Test(groups = {"websocket-test"})
public class PushTextFailureTest extends WebSocketTestCommons {

    private static final String URL = "ws://localhost:21008/pushTextFailure";
    private LogLeecher logLeecher;

    // Related file 08_pushText_failure.bal
    @BeforeClass(description = "Initializes the Ballerina server and client")
    public void setup() {
        String expectingErrorLog = "Close frame already sent. Cannot push text data!";
        logLeecher = new LogLeecher(expectingErrorLog);
        serverInstance.addLogLeecher(logLeecher);
    }

    @Test(description = "Checks for the log that is printed when pushText fails.")
    public void pushTextFailure() throws BallerinaTestException, URISyntaxException, InterruptedException {
        WebSocketTestClient client = new WebSocketTestClient(URL);
        client.handshake();
        logLeecher.waitForText(TIMEOUT_IN_SECS * 1000);
        client.shutDown();
    }
}
