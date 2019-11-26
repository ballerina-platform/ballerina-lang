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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.socket;

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.ballerinalang.stdlib.socket.MockSocketServer.SERVER_HOST;
import static org.ballerinalang.stdlib.socket.MockSocketServer.SERVER_PORT;

/**
 * Unit tests for client socket.
 */
@Test(timeOut = 120000,
      singleThreaded = true)
public class ClientSocketTest {

    private static final Logger log = LoggerFactory.getLogger(ClientSocketTest.class);

    private CompileResult socketClient;
    private ExecutorService executor;
    private MockSocketServer mockSocketServer;

    @BeforeClass
    public void setup() {
        boolean connectionStatus;
        int numberOfRetryAttempts = 20;
        try {
            executor = Executors.newSingleThreadExecutor();
            mockSocketServer = new MockSocketServer();
            executor.execute(mockSocketServer);
            Thread.sleep(2000);
            connectionStatus = TestSocketUtils.isConnected(SERVER_HOST, SERVER_PORT, numberOfRetryAttempts);
            if (!connectionStatus) {
                Assert.fail("Unable to open connection with the test TCP server");
            }
        } catch (InterruptedException e) {
            log.error("Unable to open Socket Server: " + e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        Path testResourceRoot = Paths.get(resourceRoot, "test-src");
        socketClient = BCompileUtil.compileOffline(testResourceRoot.resolve("client_socket.bal").toString());
    }

    @AfterClass
    public void cleanup() {
        mockSocketServer.stop();
        executor.shutdownNow();
    }

    @Test(description = "Open client socket connection to the remote server and write content")
    public void testOneWayWrite() {
        String msg = "Hello Ballerina";
        BValue[] args = { new BString(msg) };
        BRunUtil.invoke(socketClient, "oneWayWrite", args);
        Assert.assertEquals(mockSocketServer.getReceivedString(), msg);
    }

    @Test(description = "Write some content, then shutdown the write and try to write it again",
          dependsOnMethods = "testOneWayWrite")
    public void testShutdownWrite() {
        String firstMsg = "Hello Ballerina1";
        String secondMsg = "Hello Ballerina2";
        BValue[] args = { new BString(firstMsg), new BString(secondMsg) };
        final BValue[] shutdownWritesResult = BRunUtil.invoke(socketClient, "shutdownWrite", args);
        BError error = (BError) shutdownWritesResult[0];
        Assert.assertEquals(((BMap) error.getDetails()).getMap().get("message").toString(),
                "client socket close already.");
        Assert.assertEquals(mockSocketServer.getReceivedString(), firstMsg);
    }

    @Test(description = "Test echo behavior",
          dependsOnMethods = "testShutdownWrite")
    public void testClientEcho() {
        String msg = "Hello Ballerina Echo";
        BValue[] args = { new BString(msg) };
        final BValue[] echoResult = BRunUtil.invoke(socketClient, "echo", args);
        String echo = echoResult[0].stringValue();
        Assert.assertEquals(echo, msg, "Client did not receive expected echoed message");
        Assert.assertEquals(mockSocketServer.getReceivedString(), msg, "Server didn't get expected msg");
    }

    @Test(description = "Test invalid read param", dependsOnMethods = "testClientEcho")
    public void testInvalidReadParam() {
        final BValue[] result = BRunUtil.invoke(socketClient, "invalidReadParam");
        BError error = (BError) result[0];
        Assert.assertEquals(((BMap) error.getDetails()).getMap().get("message").toString(),
                "requested byte length need to be 1 or more");
    }

    @Test(description = "Test invalid port", dependsOnMethods = "testInvalidReadParam")
    public void testInvalidAddress() {
        final BValue[] result = BRunUtil.invoke(socketClient, "invalidAddress");
        BError error = (BError) result[0];
        Assert.assertTrue(((BMap) error.getDetails()).getMap().get("message").toString()
                .matches("^unable to start the client socket: Connection refused.*"));
    }
}
