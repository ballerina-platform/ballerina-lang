/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.reference;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;

/**
 * Test re-ordering global variable initializations to satisfy dependency order.
 */
public class GlobalForwardReferenceTestCase extends BaseTest {
    private static BServerInstance serverInstance;
    private final int initiatorServicePort = 8080;

    @BeforeClass(alwaysRun = true)
    public void start() throws BallerinaTestException {
        int[] requiredPorts = new int[]{initiatorServicePort};
        String basePath = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "reference").getAbsolutePath();
        String[] args = new String[]{};

        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(basePath, "svc", args, requiredPorts);
    }

    @AfterClass(alwaysRun = true)
    public void stop() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }

    @Test(description = "Test global variable initialized in different files")
    public void testMultipleGlobalVariableInitialization() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(initiatorServicePort, "s/k"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        assertEquals(response.getData(), "hello Sumedha", "payload mismatched");
    }
}
