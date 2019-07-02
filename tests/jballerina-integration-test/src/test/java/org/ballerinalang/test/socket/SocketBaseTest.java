/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.socket;

import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base test class for Socket integration test.
 */
public class SocketBaseTest extends BaseTest {
    protected static BServerInstance serverInstance;
    private ExecutorService executor;

    @BeforeGroups(value = "socket-test",
                  alwaysRun = true)
    public void start() throws BallerinaTestException {
        executor = Executors.newSingleThreadExecutor();
        MockSocketServer mockSocketServer = new MockSocketServer();
        String privateKey = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "private.key").toAbsolutePath().toString());
        String publicCert = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "public.crt").toAbsolutePath().toString());
        int[] requiredPorts = new int[] { 58291, MockSocketServer.SERVER_PORT, 61598, 61599 };

        String balFile = new File(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "socket")
                .getAbsolutePath();
        String[] args = new String[] { "-e", "certificate.key=" + privateKey, "-e", "public.cert=" + publicCert };
        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(balFile, "services", args, requiredPorts);
        executor.execute(mockSocketServer);
    }

    @AfterGroups(value = "socket-test",
                 alwaysRun = true)
    public void cleanup() throws Exception {
        executor.shutdownNow();
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }
}
