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

package org.ballerinalang.test.nativeimpl.functions.io.socket;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Unit tests for server socket.
 */
@Test(groups = { "broken" })
public class ServerSocketTest {

    private static final Logger log = LoggerFactory.getLogger(ServerSocketTest.class);
    private CompileResult serverBal;

    @BeforeClass
    public void setup() {
        serverBal = BCompileUtil.compileAndSetup("test-src/io/server_socket_io.bal");
    }

    //@Test(description = "Check server socket accept functionality.")
    public void testSeverSocketAccept() {
        int port = ThreadLocalRandom.current().nextInt(47000, 51000);
        String welcomeMsg = "Hello Ballerina";
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            BValue[] args = { new BInteger(port), new BString(welcomeMsg) };
            BRunUtil.invokeStateful(serverBal, "startServerSocket", args);
        });
        try {
            boolean connectionStatus;
            int numberOfRetryAttempts = 10;
            connectionStatus = isConnected("localhost", port, numberOfRetryAttempts);
            if (!connectionStatus) {
                Assert.fail("Unable to open connection with the test TCP server");
            }
            executor.shutdownNow();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private boolean isConnected(String hostName, int port, int numberOfRetries) {
        Socket temporarySocketConnection = null;
        boolean isConnected = false;
        final int retryInterval = 1000;
        final int initialRetryCount = 0;
        for (int retryCount = initialRetryCount; retryCount < numberOfRetries && !isConnected; retryCount++) {
            try {
                temporarySocketConnection = new Socket(hostName, port);
                isConnected = true;
            } catch (IOException e) {
                log.error("Error occurred while establishing a connection with test server", e);
                sleep(retryInterval);
            } finally {
                if (null != temporarySocketConnection) {
                    close(temporarySocketConnection);
                }
            }
        }
        return isConnected;
    }

    private void sleep(int retryInterval) {
        try {
            Thread.sleep(retryInterval);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }

    private void close(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            log.error("Error occurred while closing the Socket connection", e);
        }
    }
}
