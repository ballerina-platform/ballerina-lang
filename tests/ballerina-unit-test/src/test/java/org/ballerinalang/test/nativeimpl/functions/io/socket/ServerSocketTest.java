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
import org.ballerinalang.stdlib.io.socket.server.SelectorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Unit tests for server socket.
 */
public class ServerSocketTest {

    private static final Logger log = LoggerFactory.getLogger(ServerSocketTest.class);
    private CompileResult serverBal;

    @BeforeClass
    public void setup() {
        serverBal = BCompileUtil.compileAndSetup("test-src/io/server_socket_io.bal");
    }

    @Test(description = "Check server socket accept functionality.")
    public void testSeverSocketAccept() {
        int port = ThreadLocalRandom.current().nextInt(47000, 51000);
        String welcomeMsg = "Hello Ballerina\n";
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                BValue[] args = { new BInteger(port), new BString(welcomeMsg) };
                BRunUtil.invokeStateful(serverBal, "startServerSocket", args);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        });
        try {
            Thread.sleep(2500);
            final int numberOfRetryAttempts = 20;
            final int retryInterval = 1000;
            final String clientMsg = "This is the first type of message.";
            boolean connected = false;
            for (int retryCount = 0; retryCount < numberOfRetryAttempts; retryCount++) {
                try (Socket s = new Socket("localhost", port)) {
                    BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String answer = input.readLine();
                    Assert.assertEquals(welcomeMsg.trim(), answer, "Didn't get the expected response from server.");
                    try (OutputStreamWriter out = new OutputStreamWriter(s.getOutputStream(), "UTF-8")) {
                        out.write(clientMsg, 0, clientMsg.length());
                        out.flush();
                        out.close();
                        s.close();
                        connected = true;
                        break;
                    }
                } catch (IOException e) {
                    sleep(retryInterval);
                }
            }
            Assert.assertTrue(connected, "Unable to connect to remote server.");
            int i = 0;
            do {
                final BValue[] resultValues = BRunUtil.invokeStateful(serverBal, "getResultValue");
                BString result = (BString) resultValues[0];
                final String str = result.stringValue();
                if (str == null || str.isEmpty()) {
                    sleep(retryInterval);
                    continue;
                }
                Assert.assertEquals(clientMsg, str, "Client message incorrect.");
                break;
            } while (i++ < 10);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            SelectorManager.stop();
            executor.shutdownNow();
        }
    }

    private void sleep(int retryInterval) {
        try {
            Thread.sleep(retryInterval);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }
}
