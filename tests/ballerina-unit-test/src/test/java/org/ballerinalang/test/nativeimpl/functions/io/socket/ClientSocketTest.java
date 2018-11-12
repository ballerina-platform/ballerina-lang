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
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Unit tests for client socket.
 */
public class ClientSocketTest {

    private static final Logger log = LoggerFactory.getLogger(ClientSocketTest.class);

    private CompileResult socketClient;
    private Process server;
    private BMap<String, BValue> socket;

    @BeforeClass
    public void setup() {
        socketClient = BCompileUtil.compileAndSetup("test-src/io/client_socket_io.bal");
        boolean connectionStatus;
        int numberOfRetryAttempts = 20;
        try {
            server = MockSocketServer.start();
            Thread.sleep(2000);
            connectionStatus = isConnected(MockSocketServer.SERVER_HOST, numberOfRetryAttempts);
            if (!connectionStatus) {
                Assert.fail("Unable to open connection with the test TCP server");
            }
        } catch (IOException | InterruptedException e) {
            log.error("Unable to open Socket Server: " + e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Closes a provided socket connection.
     *
     * @param socket socket which should be closed.
     */
    private void close(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            log.error("Error occurred while closing the Socket connection", e);
        }
    }

    /**
     * Will enforce to sleep the thread for the provided time.
     *
     * @param retryInterval the time in milliseconds the thread should sleep
     */
    private void sleep(int retryInterval) {
        try {
            Thread.sleep(retryInterval);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Attempts to establish a connection with the test server.
     *
     * @param hostName        hostname of the server.
     * @param numberOfRetries number of retry attempts.
     * @return true if the connection is established successfully.
     */
    private boolean isConnected(String hostName, int numberOfRetries) {
        Socket temporarySocketConnection = null;
        boolean isConnected = false;
        final int retryInterval = 1000;
        final int initialRetryCount = 0;
        for (int retryCount = initialRetryCount; retryCount < numberOfRetries && !isConnected; retryCount++) {
            try {
                //Attempts to establish a connection with the server
                temporarySocketConnection = new Socket(hostName, MockSocketServer.SERVER_PORT);
                isConnected = true;
            } catch (IOException e) {
                log.error("Error occurred while establishing a connection with test server", e);
                sleep(retryInterval);
            } finally {
                if (null != temporarySocketConnection) {
                    //We close the connection once completed.
                    close(temporarySocketConnection);
                }
            }
        }
        return isConnected;
    }

    @AfterClass
    public void cleanup() {
        server.destroy();
    }

    @Test(description = "Open client socket connection to the remote server")
    public void testOpenClientSocket() {
        BValue[] args = { new BString("localhost"), new BInteger(MockSocketServer.SERVER_PORT) };
        final BValue[] returns = BRunUtil.invokeStateful(socketClient, "openSocketConnection", args);
        socket = (BMap<String, BValue>) returns[0];
        Assert.assertNotNull(socket, "Socket instance is null.");
    }

    @Test(dependsOnMethods = "testOpenClientSocket",
          description = "Test content read/write records")
    public void testReadRecords() {
        String content = "Ballerina,122\r\nC++,12";
        byte[] contentBytes = content.getBytes();
        BValue[] args = { socket, new BByteArray(contentBytes) };
        final BValue[] writeReturns = BRunUtil.invokeStateful(socketClient, "write", args);
        BInteger returnedSize = (BInteger) writeReturns[0];
        Assert.assertEquals(returnedSize.intValue(), content.length(), "Write content size is not match.");
        args = new BValue[] { socket };
        final BValue[] readReturns = BRunUtil.invokeStateful(socketClient, "readRecord", args);
        BStringArray fields = (BStringArray) readReturns[0];
        Assert.assertEquals(fields.get(0), "Ballerina");
    }

    @Test(dependsOnMethods = "testReadRecords",
          description = "Test content read/write")
    public void testWriteReadContent() {
        String content = "Hello World\n";
        byte[] contentBytes = content.getBytes();
        BValue[] args = { socket, new BByteArray(contentBytes) };
        final BValue[] writeReturns = BRunUtil.invokeStateful(socketClient, "write", args);
        BInteger returnedSize = (BInteger) writeReturns[0];
        Assert.assertEquals(returnedSize.intValue(), content.length(), "Write content size is not match.");
        args = new BValue[] { socket, new BInteger(content.length()) };
        final BValue[] readReturns = BRunUtil.invokeStateful(socketClient, "read", args);
        returnedSize = (BInteger) readReturns[1];
        Assert.assertEquals(returnedSize.intValue(), content.length(), "Read size not match with the request size");

        content = MockSocketServer.POISON_PILL;
        contentBytes = content.getBytes();
        BRunUtil.invokeStateful(socketClient, "write", new BValue[] { socket, new BByteArray(contentBytes) });
        args = new BValue[] { socket };
        BRunUtil.invokeStateful(socketClient, "closeSocket", args);
    }

    @Test(dependsOnMethods = "testWriteReadContent",
          description = "Test connection open with properties")
    public void testOpenWithProperties() {
        int port = ThreadLocalRandom.current().nextInt(33000, 46000);
        BValue[] args = { new BString("localhost"), new BInteger(MockSocketServer.SERVER_PORT), new BInteger(port) };
        final BValue[] returns = BRunUtil.invokeStateful(socketClient, "openSocketConnectionWithProps", args);
        final BMap<String, BValue> socket = (BMap<String, BValue>) returns[0];
        Assert.assertNotNull(socket, "Socket instance is null.");
        Assert.assertEquals(((BInteger) socket.get("localPort")).intValue(), port,
                "Client port didn't bind to assign port.");
        args = new BValue[] { socket };
        BRunUtil.invokeStateful(socketClient, "closeSocket", args);
    }

    @Test(dependsOnMethods = "testOpenWithProperties",
          description = "Try to bind two socket for same port")
    public void testBindSocketForSamePort() {
        int port = ThreadLocalRandom.current().nextInt(33000, 46000);
        BValue[] args = { new BInteger(port) };
        final BValue[] returns = BRunUtil.invokeStateful(socketClient, "bindSocketForSamePort", args);
        BMap error = (BMap) ((BError) returns[0]).getDetails();
        Assert.assertEquals(error.getMap().get("message").toString(), "Address already in use",
                "Didn't get the expected error message for duplicate port open.");
    }

    @Test(dependsOnMethods = "testBindSocketForSamePort",
          description = "Check write shutdown")
    public void testWriteShutdown() {
        // Create new socket
        BValue[] args = { new BString("localhost"), new BInteger(MockSocketServer.SERVER_PORT) };
        final BValue[] returns = BRunUtil.invokeStateful(socketClient, "openSocketConnection", args);
        BMap<String, BValue> socket = (BMap<String, BValue>) returns[0];
        // Write content
        String content = "Hello World\n";
        byte[] contentBytes = content.getBytes();
        args = new BValue[] { socket, new BByteArray(contentBytes) };
        BValue[] writeReturns = BRunUtil.invokeStateful(socketClient, "write", args);
        BInteger returnedSize = (BInteger) writeReturns[0];
        Assert.assertEquals(returnedSize.intValue(), content.length(), "Write content size is not match.");
        // Verify Echo
        args = new BValue[] { socket, new BInteger(content.length()) };
        BValue[] readReturns = BRunUtil.invokeStateful(socketClient, "read", args);
        returnedSize = (BInteger) readReturns[1];
        Assert.assertEquals(returnedSize.intValue(), content.length(), "Read size not match with the request size");
        // Write shutdown
        BRunUtil.invokeStateful(socketClient, "writeShutDown", new BValue[] { socket });

        content = "New Content\n";
        contentBytes = content.getBytes();
        args = new BValue[] { socket, new BByteArray(contentBytes) };
        writeReturns = BRunUtil.invokeStateful(socketClient, "write", args);
        BMap error = (BMap) ((BError) writeReturns[0]).getDetails();
        Assert.assertEquals(error.getMap().get("message").toString(), "Error occurred while writing to channel ",
                "Didn't get expected error message");

        args = new BValue[] { socket };
        BRunUtil.invokeStateful(socketClient, "closeSocket", args);
    }
}
